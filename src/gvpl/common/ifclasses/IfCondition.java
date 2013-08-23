package gvpl.common.ifclasses;

import gvpl.common.BaseScope;
import gvpl.common.ScopeManager;
import gvpl.common.Value;
import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.LinkedHashSet;
import java.util.Set;

import debug.ExecTreeLogger;

public class IfCondition {


	public static void createIfNodes(BaseScope trueScope, BaseScope falseScope, 
			GraphNode conditionNode, Graph graph) {
		Set<Var> trueWrittenVars;
		Set<Var> falseWrittenVars;
		
		if( trueScope != null ) {
			trueWrittenVars = trueScope.getWrittenVars();
			Set<Var> createdVars = ((IfScope)trueScope.getParent()).getCreatedVars();
			trueWrittenVars.removeAll(createdVars);
		} else
			trueWrittenVars = new LinkedHashSet<Var>();
		
		if( falseScope != null ) {
			falseWrittenVars = falseScope.getWrittenVars();
			Set<Var> createdVars = ((IfScope)falseScope.getParent()).getCreatedVars();
			falseWrittenVars.removeAll(createdVars);
		} else 
			falseWrittenVars = new LinkedHashSet<Var>();
			
		// set containing the vars written in both true and false block
		Set<Var> writtenVarsBoth = new LinkedHashSet<Var>(trueWrittenVars);
		writtenVarsBoth.retainAll(falseWrittenVars);

		// set containing the vars written only in true block
		Set<Var> writtenOnlyInTrue = new LinkedHashSet<Var>(trueWrittenVars);
		writtenOnlyInTrue.removeAll(falseWrittenVars);

		// set containing the vars written only in false block
		Set<Var> writtenOnlyInFalse = new LinkedHashSet<Var>(falseWrittenVars);
		writtenOnlyInFalse.removeAll(trueWrittenVars);
		
		BaseScope currentScope = ScopeManager.getCurrentScope();
		iterateWrittenVariables(writtenVarsBoth, trueScope, falseScope, conditionNode, graph);
		iterateWrittenVariables(writtenOnlyInTrue, trueScope, currentScope, conditionNode, graph);
		iterateWrittenVariables(writtenOnlyInFalse, currentScope, falseScope, conditionNode, graph);
	}
	
	private static GraphNode createGarbageIfNecessary(GraphNode node, BaseScope baseScope) {
		if(node != null)
			return node;
		else
			return baseScope.getGraph().addGraphNode("GARBAGE_NODE", NodeType.E_GARBAGE);
	}
	
	private static void iterateWrittenVariables(Set<Var> writtenVariables, BaseScope trueScope, 
			BaseScope falseScope, GraphNode conditionNode, Graph graph) {
		for( Var var : writtenVariables ) {
			GraphNode trueNode = trueScope.getLastNode(var);
			GraphNode falseNode = falseScope.getLastNode(var);
			
			trueNode = createGarbageIfNecessary(trueNode, trueScope);
			falseNode = createGarbageIfNecessary(falseNode, falseScope);
			
			GraphNode ifOpNode = IfCondition.createIfNode(graph, conditionNode, trueNode, falseNode);
			var.receiveAssign(NodeType.E_VARIABLE, new Value(ifOpNode), graph);
		}
	}

	private static GraphNode createIfNode(Graph graph, GraphNode conditionNode, GraphNode trueNode,
			GraphNode falseNode) {
		ExecTreeLogger.log(graph.getName());
		GraphNode ifOpNode = graph.addGraphNode("If", NodeType.E_OPERATION);

		trueNode.addDependentNode(ifOpNode);
		falseNode.addDependentNode(ifOpNode);
		conditionNode.addDependentNode(ifOpNode);

		return ifOpNode;
	}
	
}
