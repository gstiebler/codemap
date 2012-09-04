package gvpl.cdt;

import gvpl.common.ClassVar;
import gvpl.common.FuncParameter;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.common.MemberId;
import gvpl.common.PointerVar;
import gvpl.common.ReferenceVar;
import gvpl.common.TypeId;
import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.HashMap;
import java.util.LinkedList;
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

	protected Graph _gvplGraph;
	protected AstLoader _parent;
	protected AstInterpreter _astInterpreter;

	private Map<IBinding, Var> _direct_var_graph_nodes = new HashMap<IBinding, Var>();

	public AstLoader(Graph gvplGraph, AstLoader parent, AstInterpreter astInterpreter) {
		_gvplGraph = gvplGraph;
		_parent = parent;
		_astInterpreter = astInterpreter;
	}

	protected Var getVarDeclOfLocalReference(IASTIdExpression id_expr) {
		IBinding binding = id_expr.getName().resolveBinding();
		return _direct_var_graph_nodes.get(binding);
	}

	protected Var getVarOfReference(IASTExpression expr) {
		LinkedList<Var> varStack = new LinkedList<Var>();
		getVarOfReference(expr, varStack);
		if(varStack.size() > 0)
			return varStack.getLast();
		else
			return null;
	}
	
	private void getVarOfReference(IASTExpression expr, LinkedList<Var> varStack) {
		Var varDecl = null;
		if (expr instanceof IASTIdExpression)
			varDecl = getVarDeclOfLocalReference((IASTIdExpression) expr);
		else if (expr instanceof IASTFieldReference) {
			int size = varStack.size();
			getVarDeclOfFieldRef((IASTFieldReference) expr, varStack);
			if(size != varStack.size())
				return;
		} else if (expr instanceof IASTUnaryExpression) {
			IASTExpression opExpr = ((IASTUnaryExpression) expr).getOperand();
			varDecl = getVarDeclOfLocalReference((IASTIdExpression) opExpr);
			// return InstructionLine.loadPointedVar(opExpr, this);
		}

		if (_parent == null)
			return;

		if (varDecl == null)
			varDecl = _parent.getVarOfReference(expr);
		
		varStack.addLast(varDecl);
	}

	protected void getVarDeclOfFieldRef(IASTFieldReference field_ref, LinkedList<Var> varStack) {
		IASTExpression owner = field_ref.getFieldOwner();

		IBinding field_binding = field_ref.getFieldName().resolveBinding();
		
		getVarOfReference(owner, varStack);
		Var varOfRef = varStack.getLast();
		Var varInMem = varOfRef.getVarInMem();
		ClassVar ownerVar = (ClassVar) varInMem;

		MemberId member_id = _astInterpreter.getMemberId(ownerVar.getType(), field_binding);
		Var childVar = ownerVar.findMember(member_id);

		varStack.addLast(childVar);
	}

	protected TypeId getVarTypeFromBinding(IBinding binding) {
		Var owner_var_decl = _direct_var_graph_nodes.get(binding);
		return owner_var_decl.getType();
	}

	public Var loadVarDecl(IASTDeclarator decl, TypeId type) {
		IASTName name = decl.getName();
		Var var_decl = addVarDecl(name.toString(), type, decl.getPointerOperators());
		_direct_var_graph_nodes.put(name.resolveBinding(), var_decl);

		return var_decl;
	}

	public GraphNode addReturnStatement(GraphNode rvalue, TypeId type, String functionName,
			int startLine) {
		Var var_decl = addVarDecl(functionName, type, null);
		return var_decl.receiveAssign(NodeType.E_RETURN_VALUE, rvalue, this, startLine);
	}

	public Var addVarDecl(String name, TypeId type, IASTPointerOperator[] pointerOps) {
		FuncParameter.IndirectionType parameterVarType = null;
		parameterVarType = Function.getIndirectionType(pointerOps);
		return instanceVar(parameterVarType, name, type, _gvplGraph, this, _astInterpreter);
	}

	public static Var instanceVar(IndirectionType indirectionType, String name, TypeId typeId,
			Graph graph, AstLoader astLoader, AstInterpreter astInterpreter) {
		switch (indirectionType) {
		case E_VARIABLE:
			if (typeId == null)
				return new Var(graph, name, typeId);

			ClassDecl classDecl = astInterpreter.getClassDecl(typeId);
			return new ClassVar(graph, name, classDecl, astLoader);
		case E_POINTER:
			return new PointerVar(graph, name, typeId);
		case E_REFERENCE:
			return new ReferenceVar(graph, name, typeId);
		}
		return null;
	}

	public Function getFunction() {
		return _parent.getFunction();
	}

	public Graph getGraph() {
		return _gvplGraph;
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
