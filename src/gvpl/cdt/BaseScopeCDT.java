package gvpl.cdt;

import gvpl.cdt.function.Function;
import gvpl.cdt.function.MemberFunc;
import gvpl.common.BaseScope;
import gvpl.common.FuncParameter;
import gvpl.common.IClassVar;
import gvpl.common.IVar;
import gvpl.common.MemberId;
import gvpl.common.ScopeManager;
import gvpl.common.TypeId;
import gvpl.common.Value;
import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

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

public abstract class BaseScopeCDT extends BaseScope{
	
	static Logger logger = LogManager.getLogger(Graph.class.getName());
	
	protected AstInterpreterCDT _astInterpreter;

	public BaseScopeCDT(AstInterpreterCDT astInterpreter, BaseScope parent) {
		super(parent);
		_astInterpreter = astInterpreter;
		ScopeManager.addScope(this);
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
		
	    IBinding binding = getBindingFromExpr(expr);
	    var = getVarFromBinding(binding);
		if (var != null) 
			return var; 

		if( _parent != null )
			return _parent.getVarFromBinding(binding);
		else
			return _astInterpreter.getVarFromBinding(binding);
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
	
	protected IBinding getBindingFromExpr(IASTExpression expr) {
		ExecTreeLogger.log(expr.getRawSignature());
		logger.debug("expr {} is {}", expr.getRawSignature(), expr.getClass());
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
			//TODO gambi�rre??
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
		} else if (expr instanceof CPPASTArraySubscriptExpression) {
			CPPASTArraySubscriptExpression subsExpr = (CPPASTArraySubscriptExpression) expr;
			IASTExpression arrayExpr = subsExpr.getArrayExpression();
			return getLocalVarFromIdExpr((IASTIdExpression) arrayExpr);
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
		return varDecl;
	}

	public static IVar addVarDecl(String name, TypeId type, IASTPointerOperator[] pointerOps, 
				Graph graph, BaseScopeCDT astLoader, AstInterpreterCDT astInterpreter) {
		ExecTreeLogger.log(name);
		FuncParameter.IndirectionType parameterVarType;
		parameterVarType = Function.getIndirectionType(pointerOps);
		IVar var = instanceVar(parameterVarType, name, type, graph, astInterpreter);
		return var;
	}
	
	public IVar getVarFromBinding(IBinding binding) {
		IVar var = _localVariables.get(binding);
		if(var != null)
			return var;
		
		if( _parent != null) {
			var = _parent.getVarFromBinding(binding);
			if(var != null)
				return var;
		}
		
		return _astInterpreter.getGlobalVar(binding);
	}
	
}
