package gvpl.graphviz;

import gvpl.graph.Graph;
import gvpl.graph.GraphNode;

public abstract class Visualizer {

	public void print_graph(Graph graph) {

		GraphNode graph_node;
		int size = graph.getNumNodes();
		for (int i = 0; i < size; ++i) {
			graph_node = graph.getNode(i);
			if (graph_node._type == Graph.NodeType.E_OPERATION)
				insertOperation(graph_node.getId(), graph_node._name);
			else if (graph_node._type == Graph.NodeType.E_DIRECT_VALUE)
				insertValueNode(graph_node.getId(), graph_node._name);
			else if (graph_node._type == Graph.NodeType.E_DECLARED_PARAMETER)
				insertDeclaredParameter(graph_node.getId(), graph_node._name);
			else if (graph_node._type == Graph.NodeType.E_RETURN_VALUE)
				insertReturnValue(graph_node.getId(), graph_node._name);
			else
				insertVariable(graph_node.getId(), graph_node._name);
		}
		
		int dependents_size;
		for (int i = 0; i < size; ++i) {
			graph_node = graph.getNode(i);

			dependents_size = graph_node._dependent_nodes.size();
			for (int j = 0; j < dependents_size; ++j)
				insertDependency(graph_node.getId(), graph_node._dependent_nodes.get(j).getId());
		}
	}

	abstract void insertOperation(int node_id, String node_name);
	abstract void insertValueNode(int node_id, String node_name);
	abstract void insertDeclaredParameter(int node_id, String node_name);
	abstract void insertReturnValue(int node_id, String node_name);
	abstract void insertVariable(int node_id, String node_name);
	
	abstract void insertDependency(int node_id, int dep_node_id);
}
