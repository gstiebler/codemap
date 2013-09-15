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
	public boolean _isStatic;

	public ClassMember(MemberId id, String name, TypeId type) {
		_id = id;
		_name = name;
		_type = type;
		_indirectionType = IndirectionType.E_VARIABLE;
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
	
	@Override
	public String toString() {
		return _name;
	}
}