package gvpl.graphviz;

import gvpl.graph.Graph;
import gvpl.graph.GraphNode;

public class Visualizer {

	IGraphOutput _graphOutput;
	
	public Visualizer(IGraphOutput graphOutput) {
		_graphOutput = graphOutput;
	}
	
	public void print_graph(Graph graph) {
		printNodes(graph, "root");
		printEdges(graph);
	}
	
	private void printNodes(Graph graph, String parentName) {
		int size = graph.getNumNodes();
		for (int i = 0; i < size; ++i) {
			printNode(graph.getNode(i), _graphOutput);
		}
		
		for(Graph subgraph : graph._subgraphs) {
			_graphOutput.insertSubGraphStart(subgraph.getName(), parentName, subgraph.getStartingLine());
			printNodes(subgraph, subgraph.getName());
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
	
	private static String debugStr(GraphNode graph_node) {
		return " (" + graph_node.getStartingLine() + ")";
		//return "";
	}
	
	public static void printNode(GraphNode graph_node, IGraphOutput graphOutput) {
		if (graph_node._type == Graph.NodeType.E_OPERATION)
			graphOutput.insertOperation(graph_node.getId(), graph_node._name + debugStr(graph_node), graph_node.getStartingLine());
		else if (graph_node._type == Graph.NodeType.E_DIRECT_VALUE)
			graphOutput.insertValueNode(graph_node.getId(), graph_node._name + debugStr(graph_node), graph_node.getStartingLine());
		else if (graph_node._type == Graph.NodeType.E_DECLARED_PARAMETER)
			graphOutput.insertDeclaredParameter(graph_node.getId(), graph_node._name + debugStr(graph_node), graph_node.getStartingLine());
		else if (graph_node._type == Graph.NodeType.E_RETURN_VALUE)
			graphOutput.insertReturnValue(graph_node.getId(), graph_node._name + debugStr(graph_node), graph_node.getStartingLine());
		else
			graphOutput.insertVariable(graph_node.getId(), graph_node._name + debugStr(graph_node), graph_node.getStartingLine());
	}
}
