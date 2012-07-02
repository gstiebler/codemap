package gvpl.common;

import gvpl.graph.GraphBuilder.StructMember;

public class MemberStructInstance extends VarDecl {

	private StructMember _struct_member;
	private VarDecl _parent;
	
	public MemberStructInstance(StructMember struct_member, VarDecl parent){
		super(struct_member.getMemberType());
		_struct_member = struct_member;
		_parent = parent;
	}
	
	public String getName() {
		return _parent.getName() + "." + _struct_member.getName();
	}

}
