package gvpl.graphviz;

import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

public class Visualizer {

	IGraphOutput _graphOutput;
	
	public Visualizer(IGraphOutput graphOutput) {
		_graphOutput = graphOutput;
	}
	
	public void print_graph(Graph graph) {
		printNodes(graph, "G");
		printEdges(graph);
	}
	
	private void printNodes(Graph graph, String parentName) {
		int size = graph.getNumNodes();
		for (int i = 0; i < size; ++i) {
			printNode(graph.getNode(i));
		}
		
		for (Graph subgraph : graph._subgraphs) {
			String clusterName = _graphOutput.insertSubGraphStart(subgraph.getName(), parentName,
					subgraph.getLinesHistory());
			printNodes(subgraph, clusterName);
			_graphOutput.insertSubGraphEnd();
		}
	}
	
	private void printEdges(Graph graph) {
		GraphNode graph_node;
		int size = graph.getNumNodes();

		for (int i = 0; i < size; ++i) {
			graph_node = graph.getNode(i);
			
			for(GraphNode dependentNode : graph_node.getDependentNodes()) {
				if(!shouldPrintNode(graph_node) || !shouldPrintNode(dependentNode))
					continue;
				
				_graphOutput.insertDependency(graph_node.getId(), dependentNode.getId());
			}
		}
		
		for(Graph subgraph : graph._subgraphs) {
			printEdges(subgraph);
		}
	}
	
	private static String debugStr(GraphNode graphNode) {
		//return " (" + graphNode.getStartingLine() + ")";
		//return "\\n(" + graphNode.getId() + ")";
		return "";
	}
	
	protected boolean hasNeighbours(GraphNode graphNode) {
		return graphNode.getNumDependentNodes() != 0 || graphNode.getNumSourceNodes() != 0;
	}
	
	protected boolean shouldPrintNode(GraphNode graphNode) {
		return hasNeighbours(graphNode);
	}
	
	public void printNode(GraphNode graphNode) {
		if(!shouldPrintNode(graphNode))
			return;
		
		if (graphNode._type == NodeType.E_OPERATION)
			_graphOutput.insertOperation(graphNode, graphNode._name + debugStr(graphNode), graphNode.getStartingLine());
		else if (graphNode._type == NodeType.E_DIRECT_VALUE)
			_graphOutput.insertValueNode(graphNode.getId(), graphNode._name + debugStr(graphNode), graphNode.getStartingLine());
		else if (graphNode._type == NodeType.E_GARBAGE)
			_graphOutput.insertGarbageNode(graphNode.getId(), graphNode._name + debugStr(graphNode), graphNode.getStartingLine());
		else if (graphNode._type == NodeType.E_DECLARED_PARAMETER)
			_graphOutput.insertDeclaredParameter(graphNode.getId(), graphNode._name + debugStr(graphNode), graphNode.getStartingLine());
		else if (graphNode._type == NodeType.E_RETURN_VALUE)
			_graphOutput.insertReturnValue(graphNode.getId(), graphNode._name + debugStr(graphNode), graphNode.getStartingLine());
		else if (graphNode._type == NodeType.E_INVALID_NODE_TYPE)
			_graphOutput.insertInvalidValue(graphNode.getId(), graphNode._name + debugStr(graphNode), graphNode.getStartingLine());
		else
			_graphOutput.insertVariable(graphNode, graphNode._name + debugStr(graphNode), graphNode.getStartingLine());
	}
}
