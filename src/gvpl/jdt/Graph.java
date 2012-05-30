package gvpl.jdt;

import java.util.ArrayList;
import java.util.List;

public class Graph {
	
	public enum NodeType {
			E_INVALID_NODE_TYPE,
            E_DIRECT_VALUE,
            E_VARIABLE,
            E_OPERATION,
            E_FOR_LOOP
	}
	
	public class GraphNode
	{
		private GraphNodeId _id;
		private String _name;
		private NodeType _type;
		private List<GraphNode> _dependent_nodes;

		public GraphNode(GraphNodeId id, String name, NodeType type) {
			_id = id;
			_name = name;
			_type = type;
	    }
	};
	
	public class GraphNodeId
	{
		
		private int _id;
		
		public GraphNodeId(int id)
		{
			_id = id;
		}
	}
	
	
	private List<GraphNode> _graph_nodes = new ArrayList<GraphNode>();
	
	public GraphNode addGraphNode(String name, NodeType type)
	{
		GraphNode graph_node = new GraphNode(new GraphNodeId(_graph_nodes.size()), name, type);
		_graph_nodes.add(graph_node);
		return graph_node;
	}

	public GraphNode findGraphNode(GraphNodeId id)
	{
	  if(id._id > _graph_nodes.size())
	    ErrorOutputter.fatalError("Graph node id (" + id._id + ")not found.\n");

	  return _graph_nodes.get(id._id);
	}
}
