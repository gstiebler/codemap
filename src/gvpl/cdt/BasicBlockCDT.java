package gvpl.cdt;

import gvpl.common.BasicBlock;
import gvpl.common.IVar;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class BasicBlockCDT extends AstLoaderCDT {

	public BasicBlockCDT(AstLoaderCDT parent, AstInterpreterCDT astInterpreter) {
		super(new Graph(), parent, astInterpreter);
		_gvplGraph.setLabel("BasicBlockGraph");
	}

	public void load(IASTStatement baseStatement) {
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
		
		lostScope();
	}
	
	/**
	 * Add the nodes of the internal graph to the external graph
	 * @param startingLine
	 * @return Maps the nodes that were merged with others. The nodes in the key
	 *         of the map no longer exists.
	 */
	public Map<GraphNode, GraphNode> addToExtGraph() {
		return BasicBlock.addToExtGraph(_parent.getGraph(), this);
	}
	
	public void bindSettedPointers() {
		Graph extGraph = _parent.getGraph();
		List<InExtMAVarPair> addressVars = getAccessedMemAddressVar();
		for (InExtMAVarPair pair : addressVars) {
			IVar pointedVar = pair._in.getPointedVar();
			pointedVar.setGraph(extGraph);
			pair._ext.setPointedVar(pointedVar);
		}
	}

}
