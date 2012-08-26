package gvpl.common;

import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.TypeId;

import java.util.HashMap;
import java.util.Map;

public class ClassDecl {
	public TypeId _id;
	private String _name;
	private Map<MemberId, ClassMember> _member_var_graph_nodes;

	public ClassDecl(TypeId id, String name) {
		_id = id;
		_name = name;
		_member_var_graph_nodes = new HashMap<MemberId, ClassMember>();
	}

	public void addMember(ClassMember structMember) {
		_member_var_graph_nodes.put(structMember._id, structMember);
	}

	public String getName() {
		return _name;
	}
	
	public Iterable<Map.Entry<MemberId, ClassMember>> getMemberVarGraphNodes() {
		return _member_var_graph_nodes.entrySet();
	}
}