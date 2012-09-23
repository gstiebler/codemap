package gvpl.cdt;

import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class BasicBlock extends AstLoader {

	public BasicBlock(AstLoader parent, AstInterpreter astInterpreter) {
		super(new Graph(-1), parent, astInterpreter);
	}

	public void load(IASTStatement baseStatement) {
		int startingLine = baseStatement.getFileLocation().getStartingLineNumber();
		IASTStatement[] statements = null;
		if (baseStatement instanceof IASTCompoundStatement)
			statements = ((IASTCompoundStatement) baseStatement).getStatements();
		else if (baseStatement instanceof IASTExpressionStatement) {
			statements = new IASTStatement[1];
			statements[0] = baseStatement;
		}

		for (IASTStatement statement : statements) {
			InstructionLine instructionLine = new InstructionLine(_gvplGraph, this, _astInterpreter);
			instructionLine.load(statement);
		}
		
		addToExtGraph(startingLine);
	}
	
	void addIf(GraphNode conditionNode, int startingLine) {
		/*if (conditionNode != null) {
			for (VarNodePair varNodePair : _writtenVar) {
				Var var = varNodePair._varDecl;
				_gvplGraph.addIf(var, var.getCurrentNode(startingLine), varNodePair._graphNode,
						conditionNode, null, startingLine);
			}
		}*/
	}
	
	void addToExtGraph(int startingLine) {
		List<InExtVarPair> readVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> writtenVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> ignoredVars = new ArrayList<InExtVarPair>();
		getAccessedVars(readVars, writtenVars, ignoredVars, startingLine);

		Graph extGraph = _parent._gvplGraph;
		extGraph.merge(_gvplGraph);

		for(InExtVarPair readPair : readVars) {
			GraphNode intVarFirstNode = readPair._in.getFirstNode();
			// if someone read from internal var
			//if (intVarFirstNode.getNumDependentNodes() > 0) {
				GraphNode extVarCurrNode = readPair._ext.getCurrentNode(startingLine);
				extGraph.mergeNodes(extVarCurrNode, intVarFirstNode, startingLine);
			//}
		}
		
		for(InExtVarPair writtenPair : writtenVars) {
			GraphNode intVarCurrNode = writtenPair._in.getCurrentNode(startingLine);
			//if someone has written in the internal var
			//if(intVarCurrNode.getNumSourceNodes() > 0) {
				writtenPair._ext.initializeGraphNode(NodeType.E_VARIABLE, extGraph, this, _astInterpreter, startingLine);
				GraphNode extVarCurrNode = writtenPair._ext.getCurrentNode(startingLine);
				extGraph.mergeNodes(extVarCurrNode, intVarCurrNode, startingLine);
			//}
		}
		
		for(InExtVarPair ignoredPair : ignoredVars) {
			extGraph.removeNode(ignoredPair._in.getFirstNode());
		}
	}

}
