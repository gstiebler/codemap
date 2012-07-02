package gvpl.cdt;

import gvpl.common.MemberStructInstance;
import gvpl.common.VarDecl;
import gvpl.graph.GraphBuilder.DirectVarDecl;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.StructMember;
import gvpl.graph.GraphBuilder.StructVarDecl;
import gvpl.common.typedefs.VarId;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class LoadMemberFunc extends LoadFunction {

	private LoadStruct _parentLoadStruct;
	private Map<MemberId, DirectVarDecl> _referenced_members = new HashMap<MemberId, DirectVarDecl>();

	public LoadMemberFunc(LoadStruct parent) {
		super(parent._graph_builder, parent, parent._cppMaps, parent._astInterpreter);
		_parentLoadStruct = parent;
	}

	@Override
	/**
	 * Returns the VarDecl of the reference to a variable
	 */
	public VarDecl getVarDeclOfReference(IASTIdExpression id_expr) {
		VarDecl var_decl = getVarDeclOfLocalReference(id_expr);
		if (var_decl != null)
			return var_decl;

		IASTName name = id_expr.getName();
		IBinding binding = name.resolveBinding();
		StructMember structMember = _parentLoadStruct.getMember(binding);
		MemberId lhs_member_id = structMember.getMemberId();

		DirectVarDecl direct_var_decl = _referenced_members.get(lhs_member_id);

		// If it was not referenced yet, add to the list of referenced variables
		if (direct_var_decl == null) {
			VarId id = new VarId();
			direct_var_decl = _graph_builder.new DirectVarDecl(id, name.toString(),
					structMember.getMemberType());
			direct_var_decl.initializeGraphNode();
			addVarDecl(binding, id);
			add_var_decl(direct_var_decl);

			_referenced_members.put(lhs_member_id, direct_var_decl);
		}

		return direct_var_decl;
	}

	public void loadMemberFuncRef(StructVarDecl structVarDecl) {
		for (Map.Entry<MemberId, DirectVarDecl> entry : _referenced_members.entrySet()) {
			MemberId memberId = entry.getKey();
			DirectVarDecl internalVarDecl = entry.getValue();

			MemberStructInstance member_instance = structVarDecl.findMember(memberId);

			_graph_builder.addDependency(member_instance, internalVarDecl);
		}
	}

}
