package gvpl.cdt;

import gvpl.cdt.function.Function;
import gvpl.cdt.function.MemberFunc;
import gvpl.common.AstInterpreter;
import gvpl.common.AstLoader;
import gvpl.common.ClassVar;
import gvpl.common.FuncParameter;
import gvpl.common.IClassVar;
import gvpl.common.IContext;
import gvpl.common.IVar;
import gvpl.common.InExtVarPair;
import gvpl.common.InToExtVar;
import gvpl.common.MemberId;
import gvpl.common.TypeId;
import gvpl.common.Value;
import gvpl.common.Var;
import gvpl.common.VarInfo;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;

import debug.ExecTreeLogger;

public abstract class AstLoaderCDT extends AstLoader{
	
	static Logger logger = LogManager.getLogger(Graph.class.getName());
	
	protected AstInterpreterCDT _astInterpreter;
	private Map<IBinding, IVar> _localVariables = new LinkedHashMap<IBinding, IVar>();
	protected Map<IBinding, IVar> _extToInVars = new LinkedHashMap<IBinding, IVar>();

	public AstLoaderCDT(AstInterpreterCDT astInterpreter) {
		_astInterpreter = astInterpreter;
	}

	protected IVar getVarFromExpr(IASTExpression expr) {
		ExecTreeLogger.log(expr.getRawSignature());
		IVar var = getVarFromExprInternal(expr);

		if (var != null) 
			return var; 
		
		String rs = expr.getRawSignature();
		// deal with a hardcoded string, but we don't want the "this" pointer. 
		// the "this" will be treated elsewhere
	    if (expr instanceof CPPASTLiteralExpression && !rs.equals("this")) {
	    	// it's a hardcoded string between aspas (?)
	    	CPPASTLiteralExpression literal = (CPPASTLiteralExpression)expr;
	    	String str = literal.getRawSignature();
	    	Var tempVar = new Var(_gvplGraph, str, null);
	    	GraphNode node = _gvplGraph.addGraphNode(str, NodeType.E_DIRECT_VALUE);
	    	tempVar.receiveAssign(NodeType.E_DIRECT_VALUE, new Value(node), _gvplGraph);
	    	return tempVar;
	    }
		
		return getVarFromBinding(getBindingFromExpr(expr));
	}
	
	protected GraphNode getNodeFromExpr(IASTExpression expr) {
		ExecTreeLogger.log(expr.getRawSignature());
		IVar var = getVarFromExpr(expr);
		if(var != null)
			return var.getCurrentNode();
		return null;
	}
	
	protected Value getValueFromExpr(IASTExpression expr) {
		ExecTreeLogger.log(expr.getRawSignature());
		IVar var = getVarFromExpr(expr);
		if(var != null)
			return new Value(var);
		
		return new Value(getNodeFromExpr(expr));
	}
	
	protected IVar getLocalVar(IBinding binding) {
		ExecTreeLogger.log(binding.getName());
		return _localVariables.get(binding);
	}
	
	protected IBinding getBindingFromExpr(IASTExpression expr) {
		ExecTreeLogger.log(expr.getRawSignature());
		logger.debug("expr is {}", expr.getClass());
		if (expr instanceof IASTIdExpression) {
			return ((IASTIdExpression) expr).getName().resolveBinding(); 
		} else if (expr instanceof IASTUnaryExpression) {
			IASTExpression opExpr = ((IASTUnaryExpression) expr).getOperand();
			if(opExpr instanceof IASTIdExpression) {
				IASTIdExpression idExpr = (IASTIdExpression) opExpr;
				return idExpr.getName().resolveBinding();
			} else
				return getBindingFromExpr(opExpr);
		} else if (expr instanceof CPPASTArraySubscriptExpression) {
			//It's an array
			CPPASTArraySubscriptExpression arraySubscrExpr = (CPPASTArraySubscriptExpression) expr;
			IASTExpression opExpr = arraySubscrExpr.getArrayExpression();
			//TODO use the index!!
			//IASTExpression index = arraySubscrExpr.getSubscriptExpression();
			return ((IASTIdExpression) opExpr).getName().resolveBinding();
		} else if (expr instanceof CPPASTLiteralExpression) {
			//probably "this" pointer
			return null;
		} else if (expr instanceof CPPASTFunctionCallExpression) {
			logger.fatal("not implemented");
		} else {
			logger.fatal("Type not found {}", expr.getClass());
		}
		return null;
	}
	
	/**
	 * Gets the var inside the current context. If the var doesn't exist, creates a "virtual" var
	 * @param binding
	 * @return A var that may be a real var, or a virtual newly created var
	 */
	protected IVar getVarInsideSandboxFromBinding(IBinding binding) {	
		ExecTreeLogger.log(binding.getName());	
		IVar var = _extToInVars.get(binding);
		if(var != null)
			return var;
		
		var = _localVariables.get(binding);
		if(var != null)
			return var; 
		
		return createVarFromBinding(binding);	
	}
	
	private IVar createVarFromBinding(IBinding binding) {
		ExecTreeLogger.log(binding.getName());
		VarInfo varInfo = getTypeFromVarBinding(binding);
		String name = binding.getName();
		
		IVar var = instanceVar(varInfo._indirectionType, name, varInfo._type, _gvplGraph, _astInterpreter);
		//TODO only initialize a variable that will be read. Otherwise, the nodes generated
		// in the line below will never be used
		var.initializeVar(NodeType.E_VARIABLE, _gvplGraph, this, _astInterpreter);
		_extToInVars.put(binding, var);
		return var;
	}
	
	public VarInfo getTypeFromVarBinding(IBinding binding) {
		IVar var = getVarFromBindingUnbounded(binding);
		return var.getVarInfo();
	}
	
	protected IVar getVarFromExprInternal(IASTExpression expr) {
		ExecTreeLogger.log(expr.getRawSignature());
		if (expr instanceof IASTIdExpression)
			return getLocalVarFromIdExpr((IASTIdExpression) expr);
		else if (expr instanceof IASTFieldReference) {
			return getVarFromFieldRef((IASTFieldReference) expr);
		} else if (expr instanceof IASTUnaryExpression) {
			IASTExpression opExpr = ((IASTUnaryExpression) expr).getOperand();
			return getVarFromExprInternal(opExpr);
		} else if (expr instanceof CPPASTFunctionCallExpression) {
			//TODO gambiérre??
			InstructionLine instructionLine = new InstructionLine(_gvplGraph, this, _astInterpreter);
			Value val = instructionLine.loadFunctionCall((CPPASTFunctionCallExpression) expr);
			return val.getVar();
		} else if (expr instanceof CPPASTLiteralExpression) {
			String exprStr = expr.getRawSignature();
			if (exprStr.equals("this")) {
				// quite weird, but to deal with "this" in source code, i had to use "this" here
				MemberFunc thisMemberFunc = (MemberFunc) this;
				return thisMemberFunc.getThisReference();
			} else {
				// only used for char*
				return addVarDecl(expr.getRawSignature(), _astInterpreter.getPrimitiveType(), 
						_gvplGraph, _astInterpreter);
			}
		} else
			logger.fatal("not implemented: {}", expr.getClass());
		return null;
	}

	protected IVar getLocalVarFromIdExpr(IASTIdExpression idExpr) {
		ExecTreeLogger.log(idExpr.getRawSignature());
		IBinding binding = idExpr.getName().resolveBinding();
		return _localVariables.get(binding);
	}

	protected TypeId getVarTypeFromBinding(IBinding binding) {
		ExecTreeLogger.log(binding.getName());
		IVar owner_var_decl = _localVariables.get(binding);
		return owner_var_decl.getType();
	}

	protected IVar getVarFromFieldRef(IASTFieldReference fieldRef) {
		ExecTreeLogger.log(fieldRef.getRawSignature());
		IASTExpression owner = fieldRef.getFieldOwner();
		IBinding fieldBinding = fieldRef.getFieldName().resolveBinding();

		IVar varOfRef = null;
		String ownerStr = owner.getRawSignature();
		if (ownerStr.equals("this")) {
			// quite weird, but to deal with "this" in source code, i had to use "this" here
			MemberFunc thisMemberFunc = (MemberFunc) this;
			varOfRef = thisMemberFunc.getThisReference();
		} else {
			varOfRef = getVarFromExpr(owner);
		}
		IVar varInMem = varOfRef.getVarInMem();
		
		if(varInMem ==  null)
			return null;
		
		IClassVar ownerVar = (IClassVar) varInMem;

		TypeId ownerType = varOfRef.getType();
		ClassDeclCDT classDecl = _astInterpreter.getClassDecl(ownerType);
		MemberId memberId = classDecl.getMember(fieldBinding).getMemberId();
		IVar childVar = ownerVar.getMember(memberId);
		
		return childVar;
	}

	public IVar loadVarDecl(IASTDeclarator decl, TypeId type, Graph graph) {
		IASTName name = decl.getName();
		ExecTreeLogger.log("Var name: " + name);
		
		IVar varDecl = addVarDecl(name.toString(), type, decl.getPointerOperators(), graph, this, 
				_astInterpreter);
		_localVariables.put(name.resolveBinding(), varDecl);
		if(varDecl instanceof ClassVar)
			_varsCreatedInThisScope.add((ClassVar) varDecl);
		return varDecl;
	}

	public static IVar addVarDecl(String name, TypeId type, IASTPointerOperator[] pointerOps, 
				Graph graph, AstLoaderCDT astLoader, AstInterpreterCDT astInterpreter) {
		ExecTreeLogger.log(name);
		FuncParameter.IndirectionType parameterVarType;
		parameterVarType = Function.getIndirectionType(pointerOps);
		IVar var = instanceVar(parameterVarType, name, type, graph, astInterpreter);
		return var;
	}
	
	@Override
	public AstInterpreter getAstInterpreter() {
		return _astInterpreter;
	}
	
	public void getAccessedVars(List<InExtVarPair> read, List<InExtVarPair> written,
			List<InExtVarPair> ignored, InToExtVar inToExtMap, IContext parent) {
		ExecTreeLogger.log("");
		for (Map.Entry<IBinding, IVar> entry : _extToInVars.entrySet()) {
			IVar extVar = parent.getVarFromBinding(entry.getKey());
			if (extVar == null)
				logger.fatal("extVar cannot be null");

			getAccessedVarsRecursive(entry.getValue(), extVar, read, written, ignored, inToExtMap);
		}
	}
	
	/**
	 * Gets the vars accessed/created in the block. It's recursive because it deals with
	 * members of class vars
	 * @param intVar The var created inside the block
	 * @param extVar the correspondant var in the parent of the block
	 * @param read The resulting list of the vars that were read
	 * @param written The resulting list of the vars that were written
	 * @param ignored The list of the vars that was not read nor written
	 * @param inToExtMap The map of the internal variables to the external variables 
	 */
	protected static void getAccessedVarsRecursive(IVar intVar, IVar extVar, List<InExtVarPair> read,
			List<InExtVarPair> written, List<InExtVarPair> ignored, InToExtVar inToExtMap) {
		
		if(extVar == null)
			return;
		
		IVar extVarInMem = extVar.getVarInMem();
		IVar intVarInMem = intVar.getVarInMem();
		
		if(extVarInMem == null)
			return;
		
		inToExtMap.put(intVar, extVar);
		
		if (intVarInMem instanceof ClassVar && extVarInMem instanceof ClassVar && 
				intVarInMem instanceof ClassVar) {
			ClassVar extClassVar = (ClassVar) extVarInMem;
			ClassVar intClassVar = (ClassVar) intVarInMem;
			for (MemberId memberId : intClassVar.getClassDecl().getMemberIds()) {
				IVar memberExtVar = extClassVar.getMember(memberId);
				IVar memberIntVar = intClassVar.getMember(memberId);
				getAccessedVarsRecursive(memberIntVar, memberExtVar, read, written, ignored, inToExtMap);
			}

			return;
		} else {
			logger.error("Some variables are ClassVar, some are not");
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
}
