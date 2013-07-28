package gvpl.common.ifclasses;

import gvpl.common.BaseScope;
import gvpl.common.ScopeManager;
import gvpl.common.Value;
import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.HashSet;
import java.util.Set;

import debug.ExecTreeLogger;

public class IfCondition {


	public static void createIfNodes(BaseScope trueScope, BaseScope falseScope, 
			GraphNode conditionNode, Graph graph) {
		Set<Var> trueWrittenVars;
		Set<Var> falseWrittenVars;
		
		if( trueScope != null )
			trueWrittenVars = trueScope.getWrittenVars();
		else
			trueWrittenVars = new HashSet<Var>();
		
		if( falseScope != null )
			falseWrittenVars = falseScope.getWrittenVars();
		else 
			falseWrittenVars = new HashSet<Var>();
			
		// set containing the vars written in both true and false block
		Set<Var> writtenVarsBoth = new HashSet<Var>(trueWrittenVars);
		writtenVarsBoth.retainAll(falseWrittenVars);

		// set containing the vars written only in true block
		Set<Var> writtenOnlyInTrue = new HashSet<Var>(trueWrittenVars);
		writtenOnlyInTrue.removeAll(falseWrittenVars);

		// set containing the vars written only in false block
		Set<Var> writtenOnlyInFalse = new HashSet<Var>(falseWrittenVars);
		writtenOnlyInFalse.removeAll(trueWrittenVars);
		
		BaseScope currentScope = ScopeManager.getCurrentScope();
		iterateWrittenVariables(writtenVarsBoth, trueScope, falseScope, conditionNode, graph);
		iterateWrittenVariables(writtenOnlyInTrue, trueScope, currentScope, conditionNode, graph);
		iterateWrittenVariables(writtenOnlyInFalse, currentScope, falseScope, conditionNode, graph);
	}
	
	private static void iterateWrittenVariables(Set<Var> writtenVariables, BaseScope trueScope, 
			BaseScope falseScope, GraphNode conditionNode, Graph graph) {
		for( Var var : writtenVariables ) {
			GraphNode trueNode = trueScope.getLastNode(var);
			GraphNode falseNode = falseScope.getLastNode(var);
			
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
