package gvpl.common;

import gvpl.cdt.AstLoader;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.TypeId;

import java.util.HashMap;
import java.util.Map;

public class ClassVarDecl extends DirectVarDecl {

	Map<MemberId, DirectVarDecl> _memberInstances = new HashMap<MemberId, DirectVarDecl>();

	public ClassVarDecl(GraphBuilder graphBuilder, String name, TypeId type, ClassDecl structDecl,
			AstLoader parentAstLoader) {
		super(graphBuilder._gvplGraph, name, type);

		// For each member of the struct, create a variable instance of the
		// member
		for (Map.Entry<MemberId, ClassMember> entry : structDecl.getMemberVarGraphNodes()) {
			ClassMember struct_member = entry.getValue();

			String memberName = name + "." + struct_member.getName();
			DirectVarDecl member_instance = parentAstLoader.addVarDecl(memberName,
					struct_member.getMemberType(), struct_member.getNumPointerOps());
			_memberInstances.put(entry.getKey(), member_instance);
		}
	}

	public DirectVarDecl findMember(MemberId member_id) {
		DirectVarDecl varDecl = _memberInstances.get(member_id);
		if (varDecl != null)
			return varDecl;

		for (DirectVarDecl var : _memberInstances.values()) {
			if (var instanceof ClassVarDecl) {
				varDecl = ((ClassVarDecl) var).findMember(member_id);
				if (varDecl != null)
					return varDecl;
			}

		}

		return null;
	}

	@Override
	public void initializeGraphNode(NodeType type, int startingLine) {
		super.initializeGraphNode(type, startingLine);

		for (DirectVarDecl var : _memberInstances.values())
			var.initializeGraphNode(NodeType.E_VARIABLE, startingLine);
	}

	public Map<MemberId, DirectVarDecl> getInternalVariables() {
		Map<MemberId, DirectVarDecl> internalVariables = new HashMap<MemberId, DirectVarDecl>();

		internalVariables.putAll(_memberInstances);

		for (DirectVarDecl var : _memberInstances.values())
			if (var instanceof ClassVarDecl)
				internalVariables.putAll(((ClassVarDecl) var)._memberInstances);

		return internalVariables;
	}
}