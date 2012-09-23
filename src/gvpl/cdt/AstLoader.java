package gvpl.cdt;

import gvpl.common.ClassVar;
import gvpl.common.ErrorOutputter;
import gvpl.common.FuncParameter;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.common.MemberId;
import gvpl.common.PointerVar;
import gvpl.common.ReferenceVar;
import gvpl.common.TypeId;
import gvpl.common.Var;
import gvpl.common.VarInfo;
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
	protected Map<IBinding, Var> _extToInVars = new HashMap<IBinding, Var>();

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
		
		IBinding binding = getBindingFromExpr(expr);
		var = _extToInVars.get(binding);
		if (var != null) 
			return var; 
		
		return createVarFromBinding(binding, startingLine);
	}
	
	protected Var createVarFromBinding(IBinding binding, int startingLine) {
		VarInfo varInfo = getTypeFromVarBinding(binding);
		String name = binding.getName();
		
		Var var =  instanceVar(varInfo._indirectionType, name, varInfo._type, _gvplGraph, this, _astInterpreter);
		var.initializeGraphNode(NodeType.E_VARIABLE, _gvplGraph, this, _astInterpreter, startingLine);
		_extToInVars.put(binding, var);
		return var;
	}
	
	private IBinding getBindingFromExpr(IASTExpression expr) {
		if (expr instanceof IASTIdExpression)
			return ((IASTIdExpression) expr).getName().resolveBinding();
		else if (expr instanceof IASTUnaryExpression) {
			IASTExpression opExpr = ((IASTUnaryExpression) expr).getOperand();
			return ((IASTIdExpression) opExpr).getName().resolveBinding();
		}
		return null;
	}
	
	protected Var getVarFromBinding(IBinding binding) {
		Var var = _localVariables.get(binding);
		if(var != null)
			return var;
		
		if(_parent != null)
			return _parent.getVarFromBinding(binding);
		
		var = _extToInVars.get(binding);
		if(var != null)
			return var;
		
		return null;
	}
	
	protected VarInfo getTypeFromVarBinding(IBinding binding) {
		Var var = _localVariables.get(binding);
		if(var != null)
			return var.getVarInfo();
		
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
		FuncParameter.IndirectionType parameterVarType;
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
		case E_INDIFERENT:
			{
				ErrorOutputter.fatalError("Not expected");
				return null;
			}
		}
		return null;
	}

	public Function getFunction() {
		return _parent.getFunction();
	}

	public Graph getGraph() {
		return _gvplGraph;
	}

	protected void getAccessedVars(List<InExtVarPair> read, List<InExtVarPair> written, List<InExtVarPair> ignored, int startingLine) {
		for (Map.Entry<IBinding, Var> entry : _extToInVars.entrySet()) {
			Var extVar = getVarFromBinding(entry.getKey());
			if(extVar == null) 
				ErrorOutputter.fatalError("extVar cannot be null");
			
			getAccessedVarsRecursive(entry.getValue(), extVar, read, written, ignored, startingLine);
		}
	}
	
	private void getAccessedVarsRecursive(Var intVar, Var extVar, List<InExtVarPair> read, List<InExtVarPair> written, List<InExtVarPair> ignored, int startingLine) {
		Var extVarInMem = extVar.getVarInMem();
		Var intVarInMem = intVar.getVarInMem();
		if(intVarInMem instanceof ClassVar) {
			ClassVar extClassVar = (ClassVar) extVarInMem;
			ClassVar intClassVar = (ClassVar) intVarInMem;
			for(MemberId memberId : intClassVar.getClassDecl().getMemberIds()) {
				Var memberExtVar = extClassVar.getMember(memberId);
				Var memberIntVar = intClassVar.getMember(memberId);
				getAccessedVarsRecursive(memberIntVar, memberExtVar, read, written, ignored, startingLine);
			}
			
			return;
		}
		
		InExtVarPair varPair = new InExtVarPair(intVar, extVar);
		boolean accessed = false;
		GraphNode intVarFirstNode = intVar.getFirstNode();
		if (intVarFirstNode.getNumDependentNodes() > 0) {
			read.add(varPair);
			accessed = true;
		}
		
		GraphNode intVarCurrNode = intVar.getCurrentNode(startingLine);
		if(intVarCurrNode.getNumSourceNodes() > 0) {
			written.add(varPair);
			accessed = true;
		}
		
		if(!accessed)
			ignored.add(varPair);
	}
	
	protected Map<GraphNode, GraphNode> addSubGraph(Graph graph, AstLoader astLoader, int startingLine) {
		
		Map<GraphNode, GraphNode> map = graph.addSubGraph(_gvplGraph, this, startingLine);

		List<InExtVarPair> readVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> writtenVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> ignoredVars = new ArrayList<InExtVarPair>();
		getAccessedVars(readVars, writtenVars, ignoredVars, startingLine);
		
		for(InExtVarPair readPair : readVars) {
			GraphNode firstNodeInNewGraph = map.get(readPair._in.getFirstNode());
			readPair._ext.getCurrentNode(startingLine).addDependentNode(firstNodeInNewGraph,
					astLoader, startingLine);
		}

		for(InExtVarPair writtenPair : writtenVars) {
			GraphNode currNodeInNewGraph = map.get(writtenPair._in.getCurrentNode(startingLine));
			writtenPair._ext.receiveAssign(NodeType.E_VARIABLE, currNodeInNewGraph, astLoader,
					startingLine);
		}
		
		return map;
	}
}
