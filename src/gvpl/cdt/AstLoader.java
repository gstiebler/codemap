package gvpl.cdt;

import gvpl.common.Class;
import gvpl.common.ClassVar;
import gvpl.common.Var;
import gvpl.common.FuncParameter;
import gvpl.common.FuncParameter.eParameterType;
import gvpl.common.PointerVar;
import gvpl.common.ReferenceVar;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.TypeId;
import gvpl.graph.GraphNode;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class AstLoader {

	protected GraphBuilder _graphBuilder;
	protected AstLoader _parent;
	protected CppMaps _cppMaps;
	protected AstInterpreter _astInterpreter;

	private Map<IBinding, Var> _direct_var_graph_nodes = new HashMap<IBinding, Var>();

	public AstLoader(GraphBuilder graph_builder, AstLoader parent, CppMaps cppMaps,
			AstInterpreter astInterpreter) {
		_graphBuilder = graph_builder;
		_parent = parent;
		_cppMaps = cppMaps;
		_astInterpreter = astInterpreter;
	}

	protected Var getVarDeclOfLocalReference(IASTIdExpression id_expr) {
		IBinding binding = id_expr.getName().resolveBinding();
		return _direct_var_graph_nodes.get(binding);
	}

	protected Var getVarOfReference(IASTExpression expr) {
		Var varDecl = null;
		if (expr instanceof IASTIdExpression)
			varDecl = getVarDeclOfLocalReference((IASTIdExpression) expr);
		else if (expr instanceof IASTFieldReference){
			varDecl = getVarDeclOfFieldRef((IASTFieldReference) expr);
		} else if (expr instanceof IASTUnaryExpression) {
			IASTExpression opExpr = ((IASTUnaryExpression)expr).getOperand();
			varDecl = getVarDeclOfLocalReference((IASTIdExpression) opExpr);
			//return InstructionLine.loadPointedVar(opExpr, this);
		}

		if(_parent == null)
			return null;
		
		if (varDecl != null)
			return varDecl;
		else
			return _parent.getVarOfReference(expr);
	}

	protected TypeId getVarTypeFromBinding(IBinding binding) {
		Var owner_var_decl = _direct_var_graph_nodes.get(binding);
		return owner_var_decl.getType();
	}

	protected Var getVarDeclOfFieldRef(IASTFieldReference field_ref) {
		IASTExpression owner = field_ref.getFieldOwner();

		IBinding field_binding = field_ref.getFieldName().resolveBinding();

		ClassVar owner_var_decl = (ClassVar) getVarOfReference(owner);

		MemberId member_id = _astInterpreter.getMemberId(owner_var_decl.getType(), field_binding);

		return owner_var_decl.findMember(member_id);
	}

	public Var loadVarDecl(IASTDeclarator decl, TypeId type) {
		IASTName name = decl.getName();
		Var var_decl = addVarDecl(name.toString(), type, decl.getPointerOperators());
		_direct_var_graph_nodes.put(name.resolveBinding(), var_decl);

		return var_decl;
	}

	public GraphNode addReturnStatement(GraphNode rvalue, TypeId type, String functionName, int startLine) {
		Var var_decl = addVarDecl(functionName, type, 0);
		return var_decl.receiveAssign(NodeType.E_RETURN_VALUE, rvalue, this, startLine);
	}

	
	public Var addVarDecl(String name, TypeId type, int numPointerOps) {
		Var varDecl = null;

		if(numPointerOps > 0) {
			varDecl = new PointerVar(_graphBuilder._gvplGraph, name, type);
		}  else if (type == null) {
			varDecl = new Var(_graphBuilder._gvplGraph, name, type);
		} else {
			Class structDecl = _astInterpreter.getStructDecl(type);
			varDecl = new ClassVar(_graphBuilder, name, type, structDecl, this);
		}
		return varDecl;
	}
	
	public Var addVarDecl(String name, TypeId type, IASTPointerOperator[] pointerOps) {
		FuncParameter.eParameterType parameterVarType = null;
		parameterVarType = Function.getFuncParameterType(pointerOps);
		return instanceVarDecl(parameterVarType, name, type);
	}
	
	private Var instanceVarDecl(eParameterType parameterType, String name, TypeId type) {
		switch(parameterType) {
		case E_VARIABLE:
			if(type == null)
				return new Var(_graphBuilder._gvplGraph, name, type);
			
			Class structDecl = _astInterpreter.getStructDecl(type);
			return new ClassVar(_graphBuilder, name, type, structDecl, this);
		case E_POINTER: 
			return new PointerVar(_graphBuilder._gvplGraph, name, type);
		case E_REFERENCE: 
			return new ReferenceVar(_graphBuilder._gvplGraph, name, type);
		}
		return null;
	}

	public Function getFunction() {
		return _parent.getFunction();
	}

	public GraphBuilder getGraphBuilder() {
		return _graphBuilder;
	}

	public void varWrite(Var var, int startingLine) {
		if (_parent != null)
			_parent.varWrite(var, startingLine);
	}

	public void varRead(Var var) {
		if (_parent != null)
			_parent.varRead(var);
	}
}
