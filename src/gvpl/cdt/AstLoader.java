package gvpl.cdt;

import gvpl.common.ErrorOutputter;
import gvpl.common.VarDecl;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.DirectVarDecl;
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

	private Map<IBinding, DirectVarDecl> _direct_var_graph_nodes = new HashMap<IBinding, DirectVarDecl>();
	
	public AstLoader(GraphBuilder graph_builder, AstLoader parent, CppMaps cppMaps, AstInterpreter astInterpreter) {
		_graph_builder = graph_builder;
		_parent = parent;
		_cppMaps = cppMaps;
		_astInterpreter = astInterpreter;
	}

	protected VarDecl getVarDeclOfLocalReference(IASTIdExpression id_expr) {
		IBinding binding = id_expr.getName().resolveBinding();
		return _direct_var_graph_nodes.get(binding);
	}
	
	protected VarDecl getVarDeclOfReference(IASTIdExpression id_expr) {
		VarDecl varDecl = getVarDeclOfLocalReference(id_expr);
		
		if(varDecl != null)
			return varDecl;
		else
			return _parent.getVarDeclOfReference(id_expr);
	}
	
	protected TypeId getVarTypeFromBinding(IBinding binding) {
		VarDecl owner_var_decl = _direct_var_graph_nodes.get(binding);
		return owner_var_decl.getType();
	}
	
	protected VarDecl getVarDeclOfFieldRef(IASTFieldReference field_ref){
		IASTIdExpression owner = (IASTIdExpression) field_ref.getFieldOwner();

		IBinding field_binding = field_ref.getFieldName().resolveBinding();

		IASTName owner_name = owner.getName();
		IBinding owner_binding = owner_name.resolveBinding();
		StructVarDecl owner_var_decl = (StructVarDecl) _direct_var_graph_nodes.get(owner_binding);
		
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
		_direct_var_graph_nodes.put(name.resolveBinding(), var_decl);

		return var_decl;
	}
	
	public GraphNode addReturnStatement(GraphNode rvalue, TypeId type, String functionName) {
		DirectVarDecl var_decl = add_var_decl(type, functionName);
		return _graph_builder.add_assign(var_decl, NodeType.E_RETURN_VALUE, rvalue, this);
	}
	
	private DirectVarDecl add_var_decl(TypeId type, String functionName) {
		DirectVarDecl var_decl = null;
		
		if (type == null) {
			var_decl = _graph_builder.new DirectVarDecl(functionName, type);
		}else
		{
			StructDecl structDecl = _astInterpreter.getStructDecl(type);
			var_decl = _graph_builder.new StructVarDecl(functionName, type, structDecl);
		}
		return var_decl;
	}
	
	public Function getFunction() {
		return _parent.getFunction();
	}
	
	public GraphBuilder getGraphBuilder() {
		return _graph_builder;
	}
	
	public void varWrite(VarDecl var) {
		if (_parent != null) 
			_parent.varWrite(var);
	}
}
