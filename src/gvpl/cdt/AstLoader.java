package gvpl.cdt;

import gvpl.ErrorOutputter;
import gvpl.common.VarDecl;
import gvpl.common.typedefs.VarId;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.DirectVarDecl;
import gvpl.graph.GraphBuilder.FuncDecl;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.StructDecl;
import gvpl.graph.GraphBuilder.StructVarDecl;
import gvpl.graph.GraphBuilder.TypeId;
import gvpl.graph.GraphNode;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
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
	

	private Map<VarId, DirectVarDecl> _direct_var_graph_nodes = new HashMap<VarId, DirectVarDecl>();
	
	public AstLoader(GraphBuilder graph_builder, AstLoader parent, CppMaps cppMaps, AstInterpreter astInterpreter) {
		_graph_builder = graph_builder;
		_parent = parent;
		_cppMaps = cppMaps;
		_astInterpreter = astInterpreter;
	}
	
	protected void addVarDecl(IBinding binding, VarId id){
		_var_id_map.put(binding, id);
	}

	protected VarDecl getVarDeclOfLocalReference(IASTIdExpression id_expr) {
		IBinding binding = id_expr.getName().resolveBinding();
		VarId lhs_var_id = _var_id_map.get(binding);

		if(lhs_var_id == null)
			return null;
		
		return find_var(lhs_var_id);
	}
	
	protected VarDecl getVarDeclOfReference(IASTIdExpression id_expr) {
		VarDecl varDecl = getVarDeclOfLocalReference(id_expr);
		
		if(varDecl != null)
			return varDecl;
		else
			return _parent.getVarDeclOfReference(id_expr);
	}
	
	protected TypeId getVarTypeFromBinding(IBinding binding) {
		VarId owner_var_id = _var_id_map.get(binding);
		VarDecl owner_var_decl = find_var(owner_var_id);
		return owner_var_decl.getType();
	}
	
	protected VarDecl getVarDeclOfFieldRef(IASTFieldReference field_ref){
		IASTIdExpression owner = (IASTIdExpression) field_ref.getFieldOwner();

		IBinding field_binding = field_ref.getFieldName().resolveBinding();

		IASTName owner_name = owner.getName();
		IBinding owner_binding = owner_name.resolveBinding();
		VarId owner_var_id = _var_id_map.get(owner_binding);
		StructVarDecl owner_var_decl = (StructVarDecl) find_var(owner_var_id);
		
		MemberId member_id = _astInterpreter.getMemberId(owner_var_decl.getType(), field_binding);
		
		return owner_var_decl.findMember(member_id);
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
	
	public DirectVarDecl load_var_decl(IASTDeclarator decl, TypeId type) {
		IASTName name = decl.getName();

		DirectVarDecl var_decl = add_var_decl(type, name.toString());
		addVarDecl(name.resolveBinding(), var_decl.getVarId());

		return var_decl;
	}
	
	public GraphNode addReturnStatement(GraphNode rvalue, TypeId type, String functionName) {
		DirectVarDecl var_decl = add_var_decl(type, functionName);

		return _graph_builder.add_assign(var_decl, NodeType.E_RETURN_VALUE, rvalue);
	}
	
	public void add_var_decl(DirectVarDecl var_decl) {
		_direct_var_graph_nodes.put(var_decl.getVarId(), var_decl);
	}
	
	public DirectVarDecl add_var_decl(TypeId type, String functionName) {
		DirectVarDecl var_decl = null;
		
		if (type == null) {
			var_decl = _graph_builder.new DirectVarDecl(new VarId(), functionName, type);
		}else
		{
			StructDecl structDecl = _astInterpreter.getStructDecl(type);
			var_decl = _graph_builder.new StructVarDecl(new VarId(), functionName, type, structDecl);
		}
		_direct_var_graph_nodes.put(var_decl.getVarId(), var_decl);
		return var_decl;
	}

	public DirectVarDecl find_var(VarId id) {
		DirectVarDecl result = _direct_var_graph_nodes.get(id);
		if (result == null)
			return _parent.find_var(id);

		return result;
	}
	
	public FuncDecl getFuncDecl() {
		return _parent.getFuncDecl();
	}
}
