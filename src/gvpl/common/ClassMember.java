package gvpl.common;

import gvpl.common.FuncParameter.IndirectionType;


/**
 * Class member. Unique per class declaration
 */
public class ClassMember {
	private MemberId _id;
	private String _name;
	private TypeId _type;
	private IndirectionType _indirectionType;

	public ClassMember(MemberId id, String name, TypeId type, IndirectionType indirectionType) {
		_id = id;
		_name = name;
		_type = type;
		_indirectionType = indirectionType;
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
	
	public VarInfo getVarInfo() {
		return new VarInfo(_type, _indirectionType);
	}
}