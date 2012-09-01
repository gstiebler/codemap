package gvpl.cdt;

import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;

import java.util.LinkedList;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class BasicBlock extends AstLoader {
	
	private GraphNode _conditionNode;
	
	public BasicBlock(AstLoader parent, AstInterpreter astInterpreter, GraphNode conditionNode) {
		super(parent._graphBuilder, parent, parent._cppMaps, astInterpreter);
		_conditionNode = conditionNode;
	}

	public void load(IASTStatement baseStatement) {
		int startingLine = baseStatement.getFileLocation().getStartingLineNumber();
		
		IASTStatement[] statements = null;
		if(baseStatement instanceof IASTCompoundStatement)
			statements = ((IASTCompoundStatement)baseStatement).getStatements();
		else if (baseStatement instanceof IASTExpressionStatement)
		{
			statements = new IASTStatement [1];
			statements[0] = baseStatement;
		}

		for (IASTStatement statement : statements) {
			InstructionLine instructionLine = new InstructionLine(_graphBuilder, this, _cppMaps, _astInterpreter);
			instructionLine.load(statement);
		}
		
		LinkedList<VarNodePair> writtenVars = getWrittenVars(_graphBuilder._gvplGraph);
		
		if(_conditionNode != null) {
			for (VarNodePair varNodePair : writtenVars) {
				Var var = varNodePair._var;
				_graphBuilder.addIf(var, var.getCurrentNode(startingLine), varNodePair._graphNode, _conditionNode, startingLine);
			}
		}
	}
	
	private static LinkedList<VarNodePair> getWrittenVars(Graph graph) {
		LinkedList<VarNodePair> result = new LinkedList<VarNodePair>();
		for(GraphNode graphNode : graph.getNodes()) {
			if(!graphNode.hasSourceNodes())
				continue;
			
			Var var = graphNode.getParentVar();
			result.add(new VarNodePair(var, graphNode));
		}
		
		for(Graph subGraph : graph.getSubgraphs()) {
			LinkedList<VarNodePair> writtenFromSubgraph = getWrittenVars(subGraph);
			result.addAll(writtenFromSubgraph);
		}
		
		return result;
	}

}
