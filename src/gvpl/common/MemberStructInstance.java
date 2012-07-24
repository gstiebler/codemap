package gvpl.common;

import gvpl.cdt.AstLoader;
import gvpl.graph.Graph;
import gvpl.graph.GraphBuilder.StructMember;

public class MemberStructInstance extends VarDecl {

	private StructMember _struct_member;
	private VarDecl _parent;
	
	public MemberStructInstance(StructMember struct_member, VarDecl parent, Graph graph, AstLoader parentAstLoader){
		super(struct_member.getMemberType(), graph, parentAstLoader);
		_struct_member = struct_member;
		_parent = parent;
	}
	
	public String getName() {
		return _parent.getName() + "." + _struct_member.getName();
	}

	public void clearNodes() {
		_curr_graph_node = null;
		_first_graph_node = null;
	}
}
