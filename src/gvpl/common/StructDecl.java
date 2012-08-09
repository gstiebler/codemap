package gvpl.common;

import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.TypeId;

import java.util.HashMap;
import java.util.Map;

public class StructDecl {
	public TypeId _id;
	private String _name;
	private Map<MemberId, StructMember> _member_var_graph_nodes;

	public StructDecl(TypeId id, String name) {
		_id = id;
		_name = name;
		_member_var_graph_nodes = new HashMap<MemberId, StructMember>();
	}

	public void addMember(StructMember structMember) {
		_member_var_graph_nodes.put(structMember._id, structMember);
	}

	public String getName() {
		return _name;
	}
	
	public Iterable<Map.Entry<MemberId, StructMember>> getMemberVarGraphNodes() {
		return _member_var_graph_nodes.entrySet();
	}
}