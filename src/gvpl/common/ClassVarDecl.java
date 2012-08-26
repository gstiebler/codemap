package gvpl.common;

import gvpl.cdt.AstLoader;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.TypeId;

import java.util.HashMap;
import java.util.Map;

public class ClassVarDecl extends Var {

	Map<MemberId, Var> _memberInstances = new HashMap<MemberId, Var>();

	public ClassVarDecl(GraphBuilder graphBuilder, String name, TypeId type, ClassDecl structDecl,
			AstLoader parentAstLoader) {
		super(graphBuilder._gvplGraph, name, type);

		// For each member of the struct, create a variable instance of the
		// member
		for (Map.Entry<MemberId, ClassMember> entry : structDecl.getMemberVarGraphNodes()) {
			ClassMember struct_member = entry.getValue();

			String memberName = name + "." + struct_member.getName();
			Var member_instance = parentAstLoader.addVarDecl(memberName,
					struct_member.getMemberType(), struct_member.getNumPointerOps());
			_memberInstances.put(entry.getKey(), member_instance);
		}
	}

	public Var findMember(MemberId member_id) {
		Var varDecl = _memberInstances.get(member_id);
		if (varDecl != null)
			return varDecl;

		for (Var var : _memberInstances.values()) {
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

		for (Var var : _memberInstances.values())
			var.initializeGraphNode(NodeType.E_VARIABLE, startingLine);
	}

	public Map<MemberId, Var> getInternalVariables() {
		Map<MemberId, Var> internalVariables = new HashMap<MemberId, Var>();

		internalVariables.putAll(_memberInstances);

		for (Var var : _memberInstances.values())
			if (var instanceof ClassVarDecl)
				internalVariables.putAll(((ClassVarDecl) var)._memberInstances);

		return internalVariables;
	}
}