package gvpl.cdt;

import gvpl.common.ClassMember;
import gvpl.common.ClassVar;
import gvpl.common.FuncParameter;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.common.ErrorOutputter;
import gvpl.common.MemberId;
import gvpl.common.PointerVar;
import gvpl.common.ReferenceVar;
import gvpl.common.TypeId;
import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
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
			if(ext == null)
				ErrorOutputter.fatalError("ext cannot be null");
			_in = in;
			_ext = ext;
		}
	}
	
	protected Graph _gvplGraph;
	protected AstLoader _parent;
	protected AstInterpreter _astInterpreter;
	private Map<IBinding, Var> _localVariables = new HashMap<IBinding, Var>();
	protected Map<List<IBinding>, Var> _extToInVars = new HashMap<List<IBinding>, Var>();

	public AstLoader(Graph gvplGraph, AstLoader parent, AstInterpreter astInterpreter) {
		_gvplGraph = gvplGraph;
		_parent = parent;
		_astInterpreter = astInterpreter;
	}

	protected Var getVarFromExpr(IASTExpression expr) {
		int startingLine = expr.getFileLocation().getStartingLineNumber();
		Var var = getVarFromExprInternal(expr);

		if (var != null) 
			return var; 
		else {
			List<IBinding> bindingStack = new ArrayList<>();
			getBindingStack(expr, bindingStack);
			
			return createVarFromBindings(bindingStack, startingLine);
		}
	}
	
	protected Var createVarFromBindings(List<IBinding> bindingStack, int startingLine) {
		IBinding binding = bindingStack.get(bindingStack.size() - 1);
		
		
		TypeId type = getTypeFromBindingStack(bindingStack);
		String name = binding.getName();
		
		//TODO review the null in the last parameter
		Var var = addVarDecl(name, type, null);
		var.initializeGraphNode(NodeType.E_VARIABLE, _gvplGraph, this, _astInterpreter, startingLine);
		_extToInVars.put(bindingStack, var);
		return var;
	}
	
	private void getBindingStack(IASTExpression expr, List<IBinding> bindingStack) {
		if (expr instanceof IASTIdExpression)
			getBindingFromIdExpr((IASTIdExpression) expr, bindingStack);
		else if (expr instanceof IASTFieldReference) {
			getBindingFromFieldRef((IASTFieldReference) expr, bindingStack);
		} else if (expr instanceof IASTUnaryExpression) {
			IASTExpression opExpr = ((IASTUnaryExpression) expr).getOperand();
			getBindingFromIdExpr((IASTIdExpression) opExpr, bindingStack);
		}
	}
	
	private void getBindingFromIdExpr(IASTIdExpression idExpr, List<IBinding> bindingStack) {
		IBinding binding = idExpr.getName().resolveBinding();
		bindingStack.add(binding);
	}
	
	private void getBindingFromFieldRef(IASTFieldReference field_ref, List<IBinding> bindingStack) {
		IASTExpression owner = field_ref.getFieldOwner();

		getBindingStack(owner, bindingStack);
		IBinding field_binding = field_ref.getFieldName().resolveBinding();
		bindingStack.add(field_binding);
	}
	
	protected Var getVarFromBindingStack(List<IBinding> bindingStack) {
		Var currVar = getVarFromBinding(bindingStack.get(0));
		
		for(int i = bindingStack.size() - 1; i >= 1 ; i--) {
			ClassVar classVar = (ClassVar) currVar;
			IBinding memberBinding = bindingStack.get(i);
			MemberId memberId = _astInterpreter.getMemberId(classVar.getType(), memberBinding);
			currVar = classVar.getMember(memberId);
		}
		
		return currVar;
	}
	
	protected Var getVarFromBinding(IBinding binding) {
		Var var = _localVariables.get(binding);
		if(var != null)
			return var;
		
		return _parent.getVarFromBinding(binding);
	}
	
	
	
	
	private TypeId getTypeFromBindingStack(List<IBinding> bindingStack) {
		TypeId currType = getTypeFromVarBinding(bindingStack.get(0));
		
		for(int i = bindingStack.size() - 1; i >= 1 ; i--) {
			IBinding memberBinding = bindingStack.get(i);
			ClassDecl classDecl = _astInterpreter.getClassDecl(currType);
			ClassMember classMember = classDecl.getMember(memberBinding);
			currType = classMember.getMemberType();
		}
		
		return currType;
	}
	
	protected TypeId getTypeFromVarBinding(IBinding binding) {
		Var var = _localVariables.get(binding);
		if(var != null)
			return var.getType();
		
		return _parent.getTypeFromVarBinding(binding);
	}
	
	
	
	
	
	protected Var getVarFromExprInternal(IASTExpression expr) {
		if (expr instanceof IASTIdExpression)
			return getLocalVarFromIdExpr((IASTIdExpression) expr);
		else if (expr instanceof IASTFieldReference) {
			return getVarFromFieldRef((IASTFieldReference) expr);
		} else if (expr instanceof IASTUnaryExpression) {
			IASTExpression opExpr = ((IASTUnaryExpression) expr).getOperand();
			return getLocalVarFromIdExpr((IASTIdExpression) opExpr);
		}
		return null;
	}

	protected Var getLocalVarFromIdExpr(IASTIdExpression id_expr) {
		IBinding binding = id_expr.getName().resolveBinding();
		return _localVariables.get(binding);
	}

	protected TypeId getVarTypeFromBinding(IBinding binding) {
		Var owner_var_decl = _localVariables.get(binding);
		return owner_var_decl.getType();
	}

	protected Var getVarFromFieldRef(IASTFieldReference field_ref) {
		IASTExpression owner = field_ref.getFieldOwner();

		Var varOfRef = getVarFromExpr(owner);
		Var varInMem = varOfRef.getVarInMem();
		ClassVar ownerVar = (ClassVar) varInMem;

		IBinding field_binding = field_ref.getFieldName().resolveBinding();
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
			if (astInterpreter.isPrimitiveType(typeId))
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

	protected void getAccessedVars(Var intVar, List<IBinding> extBindingStack, List<InExtVarPair> read, List<InExtVarPair> written, List<InExtVarPair> ignored, int startingLine) {
		Var extVar = getVarFromBindingStack(extBindingStack);
		if(extVar == null) 
			ErrorOutputter.fatalError("extVar cannot be null");
		
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
