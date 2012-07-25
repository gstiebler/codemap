package gvpl.cdt;

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

public class AstLoader {

	protected GraphBuilder _graph_builder;
	protected AstLoader _parent;
	protected CppMaps _cppMaps;
	protected AstInterpreter _astInterpreter;

	private Map<IBinding, DirectVarDecl> _direct_var_graph_nodes = new HashMap<IBinding, DirectVarDecl>();

	public AstLoader(GraphBuilder graph_builder, AstLoader parent, CppMaps cppMaps,
			AstInterpreter astInterpreter) {
		_graph_builder = graph_builder;
		_parent = parent;
		_cppMaps = cppMaps;
		_astInterpreter = astInterpreter;
	}

	protected VarDecl getVarDeclOfLocalReference(IASTIdExpression id_expr) {
		IBinding binding = id_expr.getName().resolveBinding();
		return _direct_var_graph_nodes.get(binding);
	}

	protected VarDecl getVarDeclOfReference(IASTExpression expr) {
		VarDecl varDecl = null;
		if (expr instanceof IASTIdExpression)
			varDecl = getVarDeclOfLocalReference((IASTIdExpression) expr);
		else if (expr instanceof IASTFieldReference){
			varDecl = getVarDeclOfFieldRef((IASTFieldReference) expr);
		}

		if (varDecl != null)
			return varDecl;
		else
			return _parent.getVarDeclOfReference(expr);
	}

	protected TypeId getVarTypeFromBinding(IBinding binding) {
		VarDecl owner_var_decl = _direct_var_graph_nodes.get(binding);
		return owner_var_decl.getType();
	}

	protected VarDecl getVarDeclOfFieldRef(IASTFieldReference field_ref) {
		IASTExpression owner = field_ref.getFieldOwner();

		IBinding field_binding = field_ref.getFieldName().resolveBinding();

		StructVarDecl owner_var_decl = (StructVarDecl) getVarDeclOfReference(owner);

		MemberId member_id = _astInterpreter.getMemberId(owner_var_decl.getType(), field_binding);

		return owner_var_decl.findMember(member_id);
	}

	public DirectVarDecl load_var_decl(IASTDeclarator decl, TypeId type) {
		IASTName name = decl.getName();
		DirectVarDecl var_decl = addVarDecl(name.toString(), type);
		_direct_var_graph_nodes.put(name.resolveBinding(), var_decl);

		return var_decl;
	}

	public GraphNode addReturnStatement(GraphNode rvalue, TypeId type, String functionName) {
		DirectVarDecl var_decl = addVarDecl(functionName, type);
		return _graph_builder.add_assign(var_decl, NodeType.E_RETURN_VALUE, rvalue, this);
	}

	public DirectVarDecl addVarDecl(String name, TypeId type) {
		DirectVarDecl var_decl = null;

		if (type == null) {
			var_decl = _graph_builder.new DirectVarDecl(name, type, this);
		} else {
			StructDecl structDecl = _astInterpreter.getStructDecl(type);
			var_decl = _graph_builder.new StructVarDecl(name, type, structDecl, this);
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

	public void varRead(VarDecl var) {
		if (_parent != null)
			_parent.varRead(var);
	}
}
