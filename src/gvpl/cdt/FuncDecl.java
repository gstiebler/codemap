package gvpl.cdt;

import gvpl.common.VarDecl;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.List;

public class FuncDecl {

	private GraphNode _return_node;
	
	private String _name;
	public List<VarDecl> _parameters;

	public FuncDecl(String name) {
		_name = name;
		_parameters = new ArrayList<VarDecl>();
		_return_node = null;
	}
	
	public String getName() {
		return _name;
	}
	
	public void setReturnNode(GraphNode returnNode) {
		_return_node = returnNode;
	}
	
	public GraphNode getReturnNode() {
		return _return_node;
	}
}