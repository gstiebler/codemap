package gvpl.cdt;

import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class BasicBlock extends AstLoader {
	
	private GraphNode _conditionNode;
	
	public BasicBlock(AstLoader parent, AstInterpreter astInterpreter, GraphNode conditionNode) {
		super(new GraphBuilder(parent._cppMaps), parent, parent._cppMaps, astInterpreter);
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
		
		List<VarNodePair> writtenVars = getWrittenVars(_graphBuilder._gvplGraph);
		
		if(_conditionNode != null) {
			for (VarNodePair varNodePair : writtenVars) {
				Var var = varNodePair._var;
				_graphBuilder.addIf(var, var.getCurrentNode(startingLine), varNodePair._graphNode, _conditionNode, startingLine);
			}
		}
		
		_parent._graphBuilder._gvplGraph.append(_graphBuilder._gvplGraph);
	}
	
	private static List<VarNodePair> getWrittenVars(Graph graph) {
		Set<Var> writtenVarSet = new HashSet<Var>();
		List<VarNodePair> result = new ArrayList<VarNodePair>();
		for(GraphNode graphNode : graph.getNodes()) {
			if(!graphNode.hasSourceNodes())
				continue;
			
			Var var = graphNode.getParentVar();
			if(var == null)
				continue;
			if(writtenVarSet.contains(var))
				continue;
			writtenVarSet.add(var);
			result.add(new VarNodePair(var, graphNode));
		}
		
		for(Graph subGraph : graph.getSubgraphs()) {
			List<VarNodePair> writtenFromSubgraph = getWrittenVars(subGraph);
			result.addAll(writtenFromSubgraph);
		}
		
		return result;
	}

}
