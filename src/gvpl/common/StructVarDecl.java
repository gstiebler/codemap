package gvpl.common;

import gvpl.cdt.AstLoader;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.TypeId;

import java.util.HashMap;
import java.util.Map;

public class StructVarDecl extends DirectVarDecl {

	Map<MemberId, VarDecl> _memberInstances = new HashMap<MemberId, VarDecl>();

	public StructVarDecl(GraphBuilder graphBuilder, String name, TypeId type, StructDecl structDecl,
			AstLoader parentAstLoader) {
		super(graphBuilder, name, type);

		// For each member of the struct, create a variable instance of the
		// member
		for (Map.Entry<MemberId, StructMember> entry : structDecl.getMemberVarGraphNodes()) {
			StructMember struct_member = entry.getValue();

			String memberName = name + "." + struct_member.getName();
			VarDecl member_instance = parentAstLoader.addVarDecl(memberName,
					struct_member.getMemberType(), struct_member.getNumPointerOps());
			_memberInstances.put(entry.getKey(), member_instance);
		}
	}

	public VarDecl findMember(MemberId member_id) {
		VarDecl varDecl = _memberInstances.get(member_id);
		if (varDecl != null)
			return varDecl;

		for (VarDecl var : _memberInstances.values()) {
			if (var instanceof StructVarDecl) {
				varDecl = ((StructVarDecl) var).findMember(member_id);
				if (varDecl != null)
					return varDecl;
			}

		}

		return null;
	}

	@Override
	public void initializeGraphNode(NodeType type, int startingLine) {
		super.initializeGraphNode(type, startingLine);

		for (VarDecl var : _memberInstances.values())
			var.initializeGraphNode(NodeType.E_VARIABLE, startingLine);
	}

	public Map<MemberId, VarDecl> getInternalVariables() {
		Map<MemberId, VarDecl> internalVariables = new HashMap<MemberId, VarDecl>();

		internalVariables.putAll(_memberInstances);

		for (VarDecl var : _memberInstances.values())
			if (var instanceof StructVarDecl)
				internalVariables.putAll(((StructVarDecl) var)._memberInstances);

		return internalVariables;
	}
}