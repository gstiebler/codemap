package gvpl.cdt;

import gvpl.common.AstInterpreter;
import gvpl.common.AstLoader;
import gvpl.common.FuncParameter;
import gvpl.common.IClassVar;
import gvpl.common.IVar;
import gvpl.common.InExtVarPair;
import gvpl.common.InToExtVar;
import gvpl.common.MemAddressVar;
import gvpl.common.MemberId;
import gvpl.common.TypeId;
import gvpl.common.VarInfo;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArraySubscriptExpression;

public class AstLoaderCDT extends AstLoader {
	
	static Logger logger = LogManager.getLogger(Graph.class.getName());
	
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
		logger.debug("expr is {}", expr.getClass());
		if (expr instanceof IASTIdExpression) {
			return ((IASTIdExpression) expr).getName().resolveBinding(); 
		} else if (expr instanceof IASTUnaryExpression) {
			IASTExpression opExpr = ((IASTUnaryExpression) expr).getOperand();
			return ((IASTIdExpression) opExpr).getName().resolveBinding();
		} else if (expr instanceof CPPASTArraySubscriptExpression) {
			//It's an array
			CPPASTArraySubscriptExpression arraySubscrExpr = (CPPASTArraySubscriptExpression) expr;
			IASTExpression opExpr = arraySubscrExpr.getArrayExpression();
			//TODO use the index!!
			//IASTExpression index = arraySubscrExpr.getSubscriptExpression();
			return ((IASTIdExpression) opExpr).getName().resolveBinding();
		} else {
			logger.fatal("Type not found {}", expr.getClass());
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

	public IVar addVarDecl(String name, TypeId type, IASTPointerOperator[] pointerOps) {
		FuncParameter.IndirectionType parameterVarType;
		parameterVarType = Function.getIndirectionType(pointerOps);
		return instanceVar(parameterVarType, name, type, _gvplGraph, this, _astInterpreter);
	}

	@Override
	public void getAccessedVars(List<InExtVarPair> read, List<InExtVarPair> written,
			List<InExtVarPair> ignored, InToExtVar inToExtMap, int startingLine) {
		for (Map.Entry<IBinding, IVar> entry : _extToInVars.entrySet()) {
			IVar extVar = _parent.getVarFromBinding(entry.getKey());
			if (extVar == null)
				logger.fatal("extVar cannot be null");

			getAccessedVarsRecursive(entry.getValue(), extVar, read, written, ignored, inToExtMap, 
					startingLine);
		}
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
	
	@Override
	protected AstInterpreter getAstInterpreter() {
		return _astInterpreter;
	}
	
	@Override
	protected AstLoader getParent() {
		return _parent;
	}
}
