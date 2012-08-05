package gvpl.graphviz;

public interface IGraphOutput {
	
	void insertOperation(int node_id, String node_name);
	void insertValueNode(int node_id, String node_name);
	void insertDeclaredParameter(int node_id, String node_name);
	void insertReturnValue(int node_id, String node_name);
	void insertVariable(int node_id, String node_name);
	void insertSubGraphStart(String name);
	void insertSubGraphEnd();
	
	void insertDependency(int node_id, int dep_node_id);
}
