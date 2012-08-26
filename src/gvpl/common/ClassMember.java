package gvpl.common;

import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.TypeId;

public class ClassMember {
	MemberId _id;
	private String _name;
	private TypeId _type;

	// private StructDecl _parent;

	public ClassMember(Class parent, MemberId id, String name, TypeId type) {
		// _parent = parent;
		_id = id;
		_name = name;
		_type = type;
	}

	public MemberId getMemberId() {
		return _id;
	}

	public TypeId getMemberType() {
		return _type;
	}

	public String getName() {
		return _name;
	}
}