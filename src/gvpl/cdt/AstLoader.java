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
import java.util.List;
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

	class InExtVarPair {
		Var _in;
		Var _ext;
		
		public InExtVarPair(Var in, Var ext) {
			_in = in;
			_ext = ext;
		}
	}
	
	protected Graph _gvplGraph;
	protected AstLoader _parent;
	protected AstInterpreter _astInterpreter;

	private Map<IBinding, Var> _localVariables = new HashMap<IBinding, Var>();
	
	protected Map<Var, Var> _extToInVars = new HashMap<Var, Var>();

	public AstLoader(Graph gvplGraph, AstLoader parent, AstInterpreter astInterpreter) {
		_gvplGraph = gvplGraph;
		_parent = parent;
		_astInterpreter = astInterpreter;
	}

	protected Var getVarFromBinding(IASTExpression expr) {
		int startingLine = expr.getFileLocation().getStartingLineNumber();
		Var var = getVarFromBindingInternal(expr);

		if (var != null) 
			return var;
		else
		{
			Var superVar = getVarFromBindingPrivate(expr);
			if(_extToInVars.containsKey(superVar))
				return _extToInVars.get(superVar);
			
			String name = "";
			TypeId type = null;
			if(superVar == null) {
				if(expr instanceof IASTIdExpression) {
					IASTName Name = ((IASTIdExpression) expr).getName();
					IBinding binding = Name.getBinding();
					type = _astInterpreter.getTypeFromBinding(binding);
					name = Name.toString();
				} else {
					name = "temp_" + startingLine;
				}
				//type = _astInterpreter.getVarTypeFromBinding(binding);
			} else {
				name = superVar.getName();
				type = superVar.getType();
			}
			//TODO review the null in the last parameter
			var = addVarDecl(name, type, null);
			var.initializeGraphNode(NodeType.E_VARIABLE, _gvplGraph, this, _astInterpreter, startingLine);
			_extToInVars.put(superVar, var);
			return var;
		}
	}
	
	private Var getVarFromBindingPrivate(IASTExpression expr) {
		Var var = getVarFromBindingInternal(expr);

		if (var != null)
			return var;
		else
			return _parent.getVarFromBinding(expr);
	}
	
	protected Var getVarFromBindingInternal(IASTExpression expr) {
		if (expr instanceof IASTIdExpression)
			return getLocalVarFromBinding((IASTIdExpression) expr);
		else if (expr instanceof IASTFieldReference) {
			return getVarFromFieldRef((IASTFieldReference) expr);
		} else if (expr instanceof IASTUnaryExpression) {
			IASTExpression opExpr = ((IASTUnaryExpression) expr).getOperand();
			return getLocalVarFromBinding((IASTIdExpression) opExpr);
		}
		return null;
	}

	protected Var getLocalVarFromBinding(IASTIdExpression id_expr) {
		IBinding binding = id_expr.getName().resolveBinding();
		return _localVariables.get(binding);
	}

	protected TypeId getVarTypeFromBinding(IBinding binding) {
		Var owner_var_decl = _localVariables.get(binding);
		return owner_var_decl.getType();
	}

	protected Var getVarFromFieldRef(IASTFieldReference field_ref) {
		IASTExpression owner = field_ref.getFieldOwner();

		IBinding field_binding = field_ref.getFieldName().resolveBinding();

		Var varOfRef = getVarFromBinding(owner);
		Var varInMem = varOfRef.getVarInMem();
		ClassVar ownerVar = (ClassVar) varInMem;

		MemberId member_id = _astInterpreter.getMemberId(ownerVar.getType(), field_binding);
		Var childVar = ownerVar.getMember(member_id);
		
		return childVar;
	}

	public Var loadVarDecl(IASTDeclarator decl, TypeId type) {
		IASTName name = decl.getName();
		Var var_decl = addVarDecl(name.toString(), type, decl.getPointerOperators());
		_localVariables.put(name.resolveBinding(), var_decl);

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

	protected void getAccessedVars(Var intVar, Var extVar, List<InExtVarPair> read, List<InExtVarPair> written, List<InExtVarPair> ignored, int startingLine) {
		if(intVar instanceof ClassVar) {
			ClassVar extClassVar = (ClassVar) extVar;
			ClassVar intClassVar = (ClassVar) intVar;
			for(MemberId memberId : intClassVar.getClassDecl().getMemberIds()) {
				Var memberExtVar = extClassVar.getMember(memberId);
				Var memberIntVar = intClassVar.getMember(memberId);
				getAccessedVars(memberIntVar, memberExtVar, read, written, ignored, startingLine);
			}
		} else {
			boolean accessed = false;
			GraphNode intVarFirstNode = intVar.getFirstNode();
			if (intVarFirstNode.getNumDependentNodes() > 0) {
				read.add(new InExtVarPair(intVar, extVar));
				accessed = true;
			}
			
			GraphNode intVarCurrNode = intVar.getCurrentNode(startingLine);
			if(intVarCurrNode.getNumSourceNodes() > 0) {
				written.add(new InExtVarPair(intVar, extVar));
				accessed = true;
			}
			
			if(!accessed)
				ignored.add(new InExtVarPair(intVar, extVar));
		}
	}
}
