package gvpl.cdt;

import gvpl.graph.GraphBuilder.DirectVarDecl;
import gvpl.graph.GraphBuilder.FuncDecl;
import gvpl.graph.GraphBuilder.FuncId;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.TypeId;
import gvpl.graph.GraphBuilder.VarDecl;
import gvpl.graph.GraphBuilder.VarId;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
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

	public LoadMemberFunc(LoadStruct parent) {
		super(parent._graph_builder, parent, parent._cppMaps, parent._astInterpreter);
		_parentLoadStruct = parent;
	}
	
	public void load(IASTFunctionDefinition fd) {		
		LoadBasicBlock basicBlockLoader = new LoadBasicBlock(this, _astInterpreter);

		CPPASTFunctionDeclarator decl = (CPPASTFunctionDeclarator) fd.getDeclarator();
		IASTParameterDeclaration[] parameters = decl.getParameters();
		IASTName name_binding = decl.getName();
		String function_name = name_binding.toString();

		FuncId func_id = _graph_builder.new FuncId();
		_parentLoadStruct.addMemberFuncBinding(name_binding.resolveBinding(), func_id);
		FuncDecl func_decl = _graph_builder.new FuncDecl(func_id, function_name);
		
		_astInterpreter.loadFuncParameters(parameters, func_decl, basicBlockLoader);

		_graph_builder.enter_function(func_decl);

		IASTStatement body = fd.getBody();

		// TODO tratar o else
		if (body instanceof IASTCompoundStatement) {
			basicBlockLoader.load((IASTCompoundStatement) body);
		}
	}
	
	@Override
	public VarDecl getVarDeclOfReference(IASTIdExpression id_expr) {
		IASTName name = id_expr.getName();
		IBinding binding = name.resolveBinding();
		MemberId lhs_member_id = _parentLoadStruct.getMemberId(binding);
		
		DirectVarDecl var_decl = _referenced_members.get(lhs_member_id);
		if(var_decl == null) {
			IASTDeclSpecifier decl_spec = null;//simple_decl.getDeclSpecifier();
			TypeId type = _astInterpreter.getType(decl_spec);
			VarId id = _graph_builder.new VarId();
			var_decl = _graph_builder.new DirectVarDecl(id, name.toString(), type);
			addVarDecl(name.resolveBinding(), id);
			_graph_builder.add_var_decl(var_decl);
			
			_referenced_members.put(lhs_member_id, var_decl);
		}
			
		return var_decl;
	}

}
