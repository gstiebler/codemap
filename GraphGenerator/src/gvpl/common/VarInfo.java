package gvpl.common;

import gvpl.common.FuncParameter.IndirectionType;

public class VarInfo {
	public TypeId _type;
	public IndirectionType _indirectionType;
	
	public VarInfo(TypeId type, IndirectionType indirectionType) {
		_type = type;
		_indirectionType = indirectionType;
	}
}