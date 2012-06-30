package gvpl.cdt;

import gvpl.graph.GraphBuilder.DirectVarDecl;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.MemberStructInstance;
import gvpl.graph.GraphBuilder.StructMember;
import gvpl.graph.GraphBuilder.VarDecl;
import gvpl.graph.GraphBuilder.VarId;

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
	public VarDecl getVarDeclOfReference(IASTIdExpression id_expr) {
		IASTName name = id_expr.getName();
		IBinding binding = name.resolveBinding();
		StructMember structMember = _parentLoadStruct.getMember(binding);
		MemberId lhs_member_id = structMember.getMemberId();

		DirectVarDecl var_decl = _referenced_members.get(lhs_member_id);

		// If it was not referenced yet, add to the list of referenced variables
		if (var_decl == null) {
			VarId id = _graph_builder.new VarId();
			var_decl = _graph_builder.new DirectVarDecl(id, name.toString(),
					structMember.getMemberType());
			var_decl.initializeGraphNode();
			addVarDecl(binding, id);
			_graph_builder.add_var_decl(var_decl);

			_referenced_members.put(lhs_member_id, var_decl);
		}

		return var_decl;
	}

	public void loadMemberFuncRef(DirectVarDecl varDecl) {
		for (Map.Entry<MemberId, DirectVarDecl> entry : _referenced_members.entrySet()) {
			MemberId memberId = entry.getKey();
			DirectVarDecl internalVarDecl = entry.getValue();

			MemberStructInstance member_instance = _graph_builder.findMember(varDecl.getVarId(),
					memberId);

			_graph_builder.addDependency(member_instance, internalVarDecl);
		}
	}

}
