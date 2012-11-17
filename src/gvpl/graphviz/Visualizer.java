package gvpl.graphviz;

import gvpl.graph.Graph;
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
			printNode(graph.getNode(i), _graphOutput);
		}
		
		for(Graph subgraph : graph._subgraphs) {
			String clusterName = _graphOutput.insertSubGraphStart(subgraph.getName(), parentName, subgraph.getStartingLine());
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
	
	public static void printNode(GraphNode graphNode, IGraphOutput graphOutput) {
		if(graphNode.getNumDependentNodes() == 0 && graphNode.getNumSourceNodes() == 0)
			return;
		
		if (graphNode._type == Graph.NodeType.E_OPERATION)
			graphOutput.insertOperation(graphNode, graphNode._name + debugStr(graphNode), graphNode.getStartingLine());
		else if (graphNode._type == Graph.NodeType.E_DIRECT_VALUE)
			graphOutput.insertValueNode(graphNode.getId(), graphNode._name + debugStr(graphNode), graphNode.getStartingLine());
		else if (graphNode._type == Graph.NodeType.E_GARBAGE)
			graphOutput.insertGarbageNode(graphNode.getId(), graphNode._name + debugStr(graphNode), graphNode.getStartingLine());
		else if (graphNode._type == Graph.NodeType.E_DECLARED_PARAMETER)
			graphOutput.insertDeclaredParameter(graphNode.getId(), graphNode._name + debugStr(graphNode), graphNode.getStartingLine());
		else if (graphNode._type == Graph.NodeType.E_RETURN_VALUE)
			graphOutput.insertReturnValue(graphNode.getId(), graphNode._name + debugStr(graphNode), graphNode.getStartingLine());
		else
			graphOutput.insertVariable(graphNode, graphNode._name + debugStr(graphNode), graphNode.getStartingLine());
	}
}
