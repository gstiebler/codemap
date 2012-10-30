package gvpl.cdt;

import gvpl.common.AstInterpreter;
import gvpl.common.AstLoader;
import gvpl.common.ClassVar;
import gvpl.common.FuncParameter;
import gvpl.common.GeneralOutputter;
import gvpl.common.IClassVar;
import gvpl.common.IVar;
import gvpl.common.MemAddressVar;
import gvpl.common.MemberId;
import gvpl.common.TypeId;
import gvpl.common.VarInfo;
import gvpl.common.InExtVarPair;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

public class AstLoaderCDT extends AstLoader {
	
	protected AstLoaderCDT _parent;
	protected AstInterpreterCDT _astInterpreter;
	private Map<IBinding, IVar> _localVariables = new LinkedHashMap<IBinding, IVar>();
	protected Map<IBinding, IVar> _extToInVars = new LinkedHashMap<IBinding, IVar>();

	public AstLoaderCDT(Graph gvplGraph, AstLoaderCDT parent, AstInterpreterCDT astInterpreter) {
		_gvplGraph = gvplGraph;
		_parent = parent;
		_astInterpreter = astInterpreter;
	}

	protected IVar getVarFromExpr(IASTExpression expr) {
		int startingLine = expr.getFileLocation().getStartingLineNumber();
		IVar var = getVarFromExprInternal(expr);

		if (var != null) 
			return var; 
		
		IBinding binding = getBindingFromExpr(expr);
		var = getVarFromBinding(binding);
		if (var != null) 
			return var; 
		
		return createVarFromBinding(binding, startingLine);
	}
	
	protected IVar createVarFromBinding(IBinding binding, int startingLine) {
		VarInfo varInfo = getTypeFromVarBinding(binding);
		String name = binding.getName();
		
		IVar var =  instanceVar(varInfo._indirectionType, name, varInfo._type, _gvplGraph, this, _astInterpreter);
		//TODO only initialize a variable that will be read. Otherwise, the nodes generated
		// in the line below will never be used
		var.initializeVar(NodeType.E_VARIABLE, _gvplGraph, this, _astInterpreter, startingLine);
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
	
	protected IVar getVarFromBinding(IBinding binding) {		
		IVar var = _extToInVars.get(binding);
		if(var != null)
			return var;
		
		return _localVariables.get(binding);
	}
	
	protected VarInfo getTypeFromVarBinding(IBinding binding) {
		IVar var = _localVariables.get(binding);
		if(var != null)
			return var.getVarInfo();
		
		return _parent.getTypeFromVarBinding(binding);
	}
	
	protected IVar getVarFromExprInternal(IASTExpression expr) {
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

	protected IVar getLocalVarFromIdExpr(IASTIdExpression id_expr) {
		IBinding binding = id_expr.getName().resolveBinding();
		return _localVariables.get(binding);
	}

	protected TypeId getVarTypeFromBinding(IBinding binding) {
		IVar owner_var_decl = _localVariables.get(binding);
		return owner_var_decl.getType();
	}

	protected IVar getVarFromFieldRef(IASTFieldReference fieldRef) {
		IASTExpression owner = fieldRef.getFieldOwner();
		IBinding fieldBinding = fieldRef.getFieldName().resolveBinding();

		IVar varOfRef = getVarFromExpr(owner);
		IVar varInMem = varOfRef.getVarInMem();
		IClassVar ownerVar = (IClassVar) varInMem;

		TypeId ownerType = varOfRef.getType();
		ClassDeclCDT classDecl = _astInterpreter.getClassDecl(ownerType);
		MemberId memberId = classDecl.getMember(fieldBinding).getMemberId();
		IVar childVar = ownerVar.getMember(memberId);
		
		return childVar;
	}

	public IVar loadVarDecl(IASTDeclarator decl, TypeId type) {
		IASTName name = decl.getName();
		IVar var_decl = addVarDecl(name.toString(), type, decl.getPointerOperators());
		_localVariables.put(name.resolveBinding(), var_decl);

		return var_decl;
	}

	public GraphNode addReturnStatement(GraphNode rvalue, TypeId type, String functionName,
			int startLine) {
		IVar var_decl = addVarDecl(functionName, type, null);
		return var_decl.receiveAssign(NodeType.E_RETURN_VALUE, rvalue, startLine);
	}

	public IVar addVarDecl(String name, TypeId type, IASTPointerOperator[] pointerOps) {
		FuncParameter.IndirectionType parameterVarType;
		parameterVarType = Function.getIndirectionType(pointerOps);
		return instanceVar(parameterVarType, name, type, _gvplGraph, this, _astInterpreter);
	}

	public Function getFunction() {
		return _parent.getFunction();
	}

	public Graph getGraph() {
		return _gvplGraph;
	}

	@Override
	public void getAccessedVars(List<InExtVarPair> read, List<InExtVarPair> written,
			List<InExtVarPair> ignored, InToExtVar inToExtMap, int startingLine) {
		for (Map.Entry<IBinding, IVar> entry : _extToInVars.entrySet()) {
			IVar extVar = _parent.getVarFromBinding(entry.getKey());
			if (extVar == null)
				GeneralOutputter.fatalError("extVar cannot be null");

			getAccessedVarsRecursive(entry.getValue(), extVar, read, written, ignored, inToExtMap, 
					startingLine);
		}
	}
	
	private void getAccessedVarsRecursive(IVar intVar, IVar extVar, List<InExtVarPair> read,
			List<InExtVarPair> written, List<InExtVarPair> ignored, InToExtVar inToExtMap,
			int startingLine) {
		
		if(extVar == null)
			return;
		
		IVar extVarInMem = extVar.getVarInMem();
		IVar intVarInMem = intVar.getVarInMem();
		
		if(extVarInMem == null)
			return;
		
		inToExtMap.put(intVar, extVar);
		
		if (intVarInMem instanceof ClassVar) {
			ClassVar extClassVar = (ClassVar) extVarInMem;
			ClassVar intClassVar = (ClassVar) intVarInMem;
			for (MemberId memberId : intClassVar.getClassDecl().getMemberIds()) {
				IVar memberExtVar = extClassVar.getMember(memberId);
				IVar memberIntVar = intClassVar.getMember(memberId);
				getAccessedVarsRecursive(memberIntVar, memberExtVar, read, written, ignored, inToExtMap,
						startingLine);
			}

			return;
		}

		InExtVarPair varPair = new InExtVarPair(intVar, extVar);
		boolean accessed = false;
		if (intVar.onceRead()) {
			read.add(varPair);
			accessed = true;
		}

		if (intVar.onceWritten()) {
			written.add(varPair);
			accessed = true;
		}

		if (!accessed)
			ignored.add(varPair);
	}

	//TODO prepare to read member vars of each var. It's only working
	// for primitive types
	public List<InExtMAVarPair> getAccessedMemAddressVar() {
		List<InExtMAVarPair> vars = new ArrayList<InExtMAVarPair>();
		
		for (Map.Entry<IBinding, IVar> entry : _extToInVars.entrySet()) {
			IVar intVar = entry.getValue();
			if(intVar instanceof MemAddressVar) {
				MemAddressVar extVar = (MemAddressVar) _parent.getVarFromBinding(entry.getKey());
				
				vars.add(new InExtMAVarPair((MemAddressVar)intVar, extVar));
			}
		}
		
		return vars;
	}
	
	/**
	 * Connects a external graph to the internal graph
	 * @param graph External graph
	 * @param startingLine
	 * @return A map from the internal graph nodes to the external graph nodes
	 */
	protected Map<GraphNode, GraphNode> addSubGraph(Graph graph, int startingLine) {
		
		Map<GraphNode, GraphNode> map = graph.addSubGraph(_gvplGraph, this, startingLine);

		List<InExtVarPair> readVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> writtenVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> ignoredVars = new ArrayList<InExtVarPair>();
		getAccessedVars(readVars, writtenVars, ignoredVars, new InToExtVar(graph), startingLine);
		
		for(InExtVarPair readPair : readVars) {
			GraphNode firstNodeInNewGraph = map.get(readPair._in.getFirstNode());
			GraphNode currNode = readPair._ext.getCurrentNode(startingLine);
			currNode.addDependentNode(firstNodeInNewGraph, startingLine);
		}

		for(InExtVarPair writtenPair : writtenVars) {
			GraphNode currNodeInNewGraph = map.get(writtenPair._in.getCurrentNode(startingLine));
			writtenPair._ext.receiveAssign(NodeType.E_VARIABLE, currNodeInNewGraph, startingLine);
		}
		
		return map;
	}
	
	@Override
	protected AstInterpreter getAstInterpreter() {
		return _astInterpreter;
	}
}
