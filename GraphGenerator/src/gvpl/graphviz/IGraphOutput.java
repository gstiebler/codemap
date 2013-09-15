package gvpl.graphviz;

import java.util.List;

import gvpl.graph.GraphNode;

public interface IGraphOutput {
	
	void insertOperation(GraphNode node, String node_name, int startingLine);
	void insertValueNode(int node_id, String node_name, int startingLine);
	void insertGarbageNode(int node_id, String node_name, int startingLine);
	void insertDeclaredParameter(int node_id, String node_name, int startingLine);
	void insertReturnValue(int node_id, String node_name, int startingLine);
	void insertInvalidValue(int node_id, String node_name, int startingLine);
	void insertVariable(GraphNode node, String node_name, int startingLine);
	String insertSubGraphStart(String name, String parent, List<Integer> linesHistory);
	void insertSubGraphEnd();
	
	void insertDependency(int node_id, int dep_node_id);
}
