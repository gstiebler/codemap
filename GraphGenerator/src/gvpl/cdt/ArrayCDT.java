package gvpl.cdt;

import gvpl.common.IVar;
import gvpl.common.Value;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;
import gvpl.graph.Graph.NodeType;

public class ArrayCDT {
	
	public static GraphNode writeToArray( IVar lhsVar, Value rhsValue, Value indexValue, Graph graph ) {
		GraphNode opNode = graph.addGraphNode("[]", NodeType.E_OPERATION);
		
		rhsValue.getNode().addDependentNode(opNode);
		indexValue.getNode().addDependentNode(opNode);
		return lhsVar.receiveAssign(NodeType.E_VARIABLE, new Value(opNode), graph);
	}
	
	public static GraphNode readFromArray( IVar rhsVar, Value indexValue, Graph graph ) {
		GraphNode opNode = graph.addGraphNode("[]", NodeType.E_OPERATION);
		
		rhsVar.getCurrentNode().addDependentNode(opNode);
		indexValue.getNode().addDependentNode(opNode);
		return opNode;
	}

}
