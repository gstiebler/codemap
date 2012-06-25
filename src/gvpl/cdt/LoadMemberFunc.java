package gvpl.cdt;

import gvpl.graph.GraphBuilder.DirectVarDecl;
import gvpl.graph.GraphBuilder.FuncDecl;
import gvpl.graph.GraphBuilder.FuncId;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.MemberStructInstance;
import gvpl.graph.GraphBuilder.StructMember;
import gvpl.graph.GraphBuilder.VarDecl;
import gvpl.graph.GraphBuilder.VarId;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;

public class LoadMemberFunc extends AstLoader {

	private LoadStruct _parentLoadStruct;
	private Map<MemberId, DirectVarDecl> _referenced_members = new HashMap<MemberId, DirectVarDecl>();
	private FuncId _funcId = null;

	public LoadMemberFunc(LoadStruct parent) {
		super(parent._graph_builder, parent, parent._cppMaps, parent._astInterpreter);
		_parentLoadStruct = parent;
	}

	/**
	 * Loads the member function definition
	 * 
	 * @param fd
	 *            The ast function definition
	 * @return The binding of the loaded function member
	 */
	public IBinding load(IASTFunctionDefinition fd) {
		LoadBasicBlock basicBlockLoader = new LoadBasicBlock(this, _astInterpreter);

		CPPASTFunctionDeclarator decl = (CPPASTFunctionDeclarator) fd.getDeclarator();
		IASTParameterDeclaration[] parameters = decl.getParameters();
		IASTName name_binding = decl.getName();
		String function_name = name_binding.toString();

		_funcId = _graph_builder.new FuncId();
		IBinding member_func_binding = name_binding.resolveBinding();
		FuncDecl func_decl = _graph_builder.new FuncDecl(_funcId, function_name);

		_astInterpreter.loadFuncParameters(parameters, func_decl, basicBlockLoader);

		_graph_builder.enter_function(func_decl);

		IASTStatement body = fd.getBody();

		// TODO tratar o else
		if (body instanceof IASTCompoundStatement) {
			basicBlockLoader.load((IASTCompoundStatement) body);
		}

		return member_func_binding;
	}

	@Override
	public VarDecl getVarDeclOfReference(IASTIdExpression id_expr) {
		IASTName name = id_expr.getName();
		IBinding binding = name.resolveBinding();
		StructMember structMember = _parentLoadStruct.getMember(binding);
		MemberId lhs_member_id = structMember.getMemberId();

		DirectVarDecl var_decl = _referenced_members.get(lhs_member_id);
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

	public FuncId getFuncId() {
		return _funcId;
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
