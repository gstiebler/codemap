package gvpl.cdt;

import gvpl.ErrorOutputter;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.TypeId;
import gvpl.graph.GraphBuilder.VarDecl;
import gvpl.graph.GraphBuilder.VarId;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;

public class AstLoader {

	protected GraphBuilder _graph_builder;
	protected AstLoader _parent;
	protected CppMaps _cppMaps;
	protected AstInterpreter _astInterpreter;

	private Map<IBinding, VarId> _var_id_map = new HashMap<IBinding, VarId>();
	
	public AstLoader(GraphBuilder graph_builder, AstLoader parent, CppMaps cppMaps, AstInterpreter astInterpreter) {
		_graph_builder = graph_builder;
		_parent = parent;
		_cppMaps = cppMaps;
		_astInterpreter = astInterpreter;
	}
	
	protected void addVarDecl(IBinding binding, VarId id){
		_var_id_map.put(binding, id);
	}
	
	protected VarDecl getVarDeclOfReference(IASTIdExpression id_expr) {
		IBinding binding = id_expr.getName().resolveBinding();
		VarId lhs_var_id = _var_id_map.get(binding);
		
		if(lhs_var_id == null)
			return _parent.getVarDeclOfReference(id_expr);
		
		return _graph_builder.find_var(lhs_var_id);
	}
	
	protected TypeId getVarTypeFromBinding(IBinding binding) {
		VarId owner_var_id = _var_id_map.get(binding);
		VarDecl owner_var_decl = _graph_builder.find_var(owner_var_id);
		return owner_var_decl.getType();
	}
	
	protected VarDecl getVarDeclOfFieldRef(IASTFieldReference field_ref){
		IASTIdExpression owner = (IASTIdExpression) field_ref.getFieldOwner();

		IBinding field_binding = field_ref.getFieldName().resolveBinding();

		IASTName owner_name = owner.getName();
		IBinding owner_binding = owner_name.resolveBinding();
		VarId owner_var_id = _var_id_map.get(owner_binding);
		VarDecl owner_var_decl = _graph_builder.find_var(owner_var_id);
		
		MemberId member_id = _astInterpreter.getMemberId(owner_var_decl.getType(), field_binding);
		
		return _graph_builder.findMember(owner_var_id, member_id);
	}
	
	protected VarDecl getVarDecl(IASTExpression expr) {
		if (expr instanceof IASTIdExpression) {
			return getVarDeclOfReference((IASTIdExpression) expr);
		} else if (expr instanceof IASTFieldReference) {
			return getVarDeclOfFieldRef((CPPASTFieldReference) expr);
		} else
			ErrorOutputter.fatalError("Work here " + expr.getClass());

		return null;
	}
}
