package gvpl.graphviz;

public interface IGraphOutput {
	
	void insertOperation(int node_id, String node_name, int startingLine);
	void insertValueNode(int node_id, String node_name, int startingLine);
	void insertDeclaredParameter(int node_id, String node_name, int startingLine);
	void insertReturnValue(int node_id, String node_name, int startingLine);
	void insertVariable(int node_id, String node_name, int startingLine);
	String insertSubGraphStart(String name, String parent, int startingLine);
	void insertSubGraphEnd();
	
	void insertDependency(int node_id, int dep_node_id);
}
