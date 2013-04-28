package gvpl.cdt;

import gvpl.common.InExtVarPair;
import gvpl.common.InToExtVar;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

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
		
		mergeScopes(gvplGraph, _parent);
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
