package gvpl.cdt;

import java.util.ArrayList;
import java.util.List;

import gvpl.common.IVar;
import gvpl.common.InExtVarPair;
import gvpl.common.InToExtVar;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;
import gvpl.graph.Graph.NodeType;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;

import debug.ExecTreeLogger;

public class ForLoop extends BasicBlockCDT {

	BaseScopeCDT _typeSource;
	
	public ForLoop(BaseScopeCDT parent, AstInterpreterCDT astInterpreter) {
		super(parent, astInterpreter);
		_typeSource = parent;
		_gvplGraph.setLabel("ForLoop");
	}

	public void load(IASTForStatement node, Graph gvplGraph, BaseScopeCDT astLoader) {
		IASTStatement body = node.getBody();

		loadHeader(node);

		load(body);
		
		gvplGraph.addSubGraph(_gvplGraph);
		
		List<InExtVarPair> readVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> writtenVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> ignoredVars = new ArrayList<InExtVarPair>();
		getAccessedVars(readVars, writtenVars, ignoredVars, new InToExtVar(gvplGraph), _parent);
		
		// bind the vars from calling block to the internal read vars
		for(InExtVarPair readPair : readVars) {
			GraphNode intVarFirstNode = readPair._in.getFirstNode();
			// if someone read from internal var
			GraphNode extVarCurrNode = readPair._ext.getCurrentNode();
			gvplGraph.mergeNodes(extVarCurrNode, intVarFirstNode);
		}		
		// bind the vars from calling block to the internal written vars
		for (InExtVarPair writtenPair : writtenVars) {
			GraphNode intVarCurrNode = writtenPair._in.getCurrentNode();
			// if someone has written in the internal var

			writtenPair._ext.initializeVar(NodeType.E_VARIABLE, gvplGraph, _astInterpreter);
			GraphNode extVarCurrNode = writtenPair._ext.getCurrentNode();
			// connect the var from the calling block to the correspodent var in this block
			gvplGraph.mergeNodes(extVarCurrNode, intVarCurrNode);
		}
		
	}

	private void loadHeader(IASTForStatement node) {
		IASTStatement initializer = node.getInitializerStatement();
		IASTExpression condition = node.getConditionExpression();
		
		Graph headerGraph = new Graph();
		headerGraph.setLabel("ForLoopHeader");
		
		InstructionLine instructionLine = new InstructionLine(headerGraph, this, _astInterpreter);
		instructionLine.load(initializer);
		instructionLine.loadValue(condition);
		
		_gvplGraph.addSubGraph(headerGraph);
	}

}
