package gvpl.cdt;

import gvpl.common.IVar;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class BasicBlock extends AstLoader {

	public BasicBlock(AstLoader parent, AstInterpreter astInterpreter) {
		super(new Graph(-1), parent, astInterpreter);
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
	}
	
	/**
	 * Add the nodes of the internal graph to the external graph
	 * @param startingLine
	 * @return Maps the nodes that were merged with others. The nodes in the key
	 *         of the map no longer exists.
	 */
	public Map<GraphNode, GraphNode> addToExtGraph(int startingLine) {
		Map<GraphNode, GraphNode> mergedNodes = new LinkedHashMap<GraphNode, GraphNode>();
		
		List<InExtVarPair> readVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> writtenVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> ignoredVars = new ArrayList<InExtVarPair>();
		getAccessedVars(readVars, writtenVars, ignoredVars, new LinkedHashMap<IVar, IVar>(), startingLine);

		Graph extGraph = _parent._gvplGraph;
		extGraph.merge(_gvplGraph);

		// bind the vars from calling block to the internal read vars
		for(InExtVarPair readPair : readVars) {
			GraphNode intVarFirstNode = readPair._in.getFirstNode();
			// if someone read from internal var
			GraphNode extVarCurrNode = readPair._ext.getCurrentNode(startingLine);
			extGraph.mergeNodes(extVarCurrNode, intVarFirstNode, startingLine);
			// connect the var from the calling block to the correspodent var in this block
			mergedNodes.put(intVarFirstNode, extVarCurrNode);
		}
		
		// bind the vars from calling block to the internal written vars
		for (InExtVarPair writtenPair : writtenVars) {
			GraphNode intVarCurrNode = writtenPair._in.getCurrentNode(startingLine);
			// if someone has written in the internal var

			writtenPair._ext.initializeGraphNode(NodeType.E_VARIABLE, extGraph,
					this, _astInterpreter, startingLine);
			GraphNode extVarCurrNode = writtenPair._ext
					.getCurrentNode(startingLine);
			// connect the var from the calling block to the correspodent var in this block
			extGraph.mergeNodes(extVarCurrNode, intVarCurrNode, startingLine);
			mergedNodes.put(intVarCurrNode, extVarCurrNode);
		}
		
		for(InExtVarPair ignoredPair : ignoredVars) {
			extGraph.removeNode(ignoredPair._in.getFirstNode());
		}
		
		return mergedNodes;
	}
	
	public void bindSettedPointers() {
		Graph extGraph = _parent._gvplGraph;
		List<InExtMAVarPair> addressVars = getAccessedMemAddressVar();
		for (InExtMAVarPair pair : addressVars) {
			IVar pointedVar = pair._in.getPointedVar();
			pointedVar.setGraph(extGraph);
			pair._ext.setPointedVar(pointedVar);
		}
	}

}
