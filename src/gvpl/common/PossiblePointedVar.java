package gvpl.common;

import gvpl.graph.Graph;
import gvpl.graph.GraphNode;
import gvpl.graph.Graph.NodeType;

class PossiblePointedVar {
	PossiblePointedVar _varTrue = null;
	PossiblePointedVar _varFalse = null;
	GraphNode _conditionNode = null;
	IVar _finalVar = null;

	PossiblePointedVar() {

	}

	PossiblePointedVar(IVar finalVar) {
		setVar(finalVar);

	}

	void setVar(IVar finalVar) {
		_finalVar = finalVar;
		_conditionNode = null;
	}

	void setPossibleVars(GraphNode conditionNode, PossiblePointedVar varTrue,
			PossiblePointedVar varFalse) {
		_conditionNode = conditionNode;
		_varTrue = varTrue;
		_varFalse = varFalse;
		_finalVar = null;
	}
	
	GraphNode getIfNode(Graph graph, int startingLine) {
		if(_conditionNode == null)
			return _finalVar.getCurrentNode(startingLine);
			
		GraphNode ifOpNode = graph.addGraphNode("If", NodeType.E_OPERATION, startingLine);

		_varTrue.getIfNode(graph, startingLine).addDependentNode(ifOpNode, startingLine);
		_varFalse.getIfNode(graph, startingLine).addDependentNode(ifOpNode, startingLine);
		_conditionNode.addDependentNode(ifOpNode, startingLine);
		
		return ifOpNode;
	}
}