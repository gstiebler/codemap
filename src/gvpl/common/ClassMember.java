package gvpl.common;

import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.TypeId;

public class ClassMember {
	MemberId _id;
	private String _name;
	private TypeId _type;
	private int _numPointerOps;

	// private StructDecl _parent;

	public ClassMember(ClassDecl parent, MemberId id, String name, TypeId type,
			int numPointerOps) {
		// _parent = parent;
		_id = id;
		_name = name;
		_type = type;
		_numPointerOps = numPointerOps;
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

	public int getNumPointerOps() {
		return _numPointerOps;
	}
}