package gvpl.cdt;

import gvpl.cdt.function.Function;
import gvpl.cdt.function.MemberFunc;
import gvpl.common.BaseScope;
import gvpl.common.ClassMember;
import gvpl.common.CodeLocation;
import gvpl.common.FuncParameter;
import gvpl.common.IClassVar;
import gvpl.common.IVar;
import gvpl.common.MemberId;
import gvpl.common.TypeId;
import gvpl.common.Value;
import gvpl.common.Var;
import gvpl.exceptions.NotFoundException;
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
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArraySubscriptExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;

import debug.ExecTreeLogger;

public abstract class BaseScopeCDT extends BaseScope{
	
	static Logger logger = LogManager.getLogger(BaseScopeCDT.class.getName());
	
	protected AstInterpreterCDT _astInterpreter;

	public BaseScopeCDT(AstInterpreterCDT astInterpreter, BaseScope parent) {
		super(parent);
		_astInterpreter = astInterpreter;
	}

	protected IVar getVarFromExpr(IASTExpression expr) throws NotFoundException {
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
	    CodeLocation codeLocation = null;
		if (binding instanceof CPPVariable) {
			IASTNode[] nodes = ((CPPVariable) binding).getDeclarations();
			if (nodes != null && nodes.length > 0) {
				codeLocation = CodeLocationCDT.NewFromFileLocation(nodes[0]);
			}
		}
		
	    var = getVarFromBinding(binding, codeLocation);
		if (var != null) 
			return var; 

		if( _parent != null )
			return _parent.getVarFromBinding(binding, codeLocation);
		else
			return _astInterpreter.getVarFromBinding(binding, codeLocation);
	}
	
	protected GraphNode getNodeFromExpr(IASTExpression expr) {
		ExecTreeLogger.log(expr.getRawSignature());
		IVar var;
		try {
			var = getVarFromExpr(expr);
		} catch (NotFoundException e) {
			return _gvplGraph.addGraphNode("PROBLEM_NODE " + e.getItemName(), NodeType.E_INVALID_NODE_TYPE);
		}
		if(var != null)
			return var.getCurrentNode();
		return null;
	}
	
	protected Value getValueFromExpr(IASTExpression expr) {
		ExecTreeLogger.log(expr.getRawSignature());
		IVar var;
		try {
			var = getVarFromExpr(expr);
		} catch (NotFoundException e) {
			return new Value( _gvplGraph.addGraphNode("PROBLEM_NODE " + e.getItemName(), NodeType.E_INVALID_NODE_TYPE));
		}
		if(var != null)
			return new Value(var);
		
		GraphNode node = getNodeFromExpr(expr);
		if(node == null)
			return null;
		
		return new Value(node);
	}
	
	protected IBinding getBindingFromExpr(IASTExpression expr) throws NotFoundException {
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
			throw new NotFoundException(expr.getRawSignature().toString());
		} else if (expr instanceof CPPASTFieldReference) {
			logger.fatal("not implemented CPPASTFieldReference", ((CPPASTFieldReference)expr).getFieldName());
			throw new NotFoundException(expr.getRawSignature().toString());
		}  else {
			logger.fatal("Type not found {}, ", expr.getClass());
			throw new NotFoundException(expr.getRawSignature().toString());
		}
	}
	
	protected IVar getVarFromExprInternal(IASTExpression expr) throws NotFoundException {
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
						null, _gvplGraph, _astInterpreter);
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

	protected IVar getVarFromFieldRef(IASTFieldReference fieldRef) throws NotFoundException {
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
		ClassMember classMember = classDecl.getMember(fieldBinding);
		if(classMember == null) {
			logger.error("Problem on {}", fieldBinding.getName());
			return null;
		}
		MemberId memberId = classMember.getMemberId();
		IVar childVar = ownerVar.getMember(memberId);
		
		return childVar;
	}

	public IVar loadVarDecl(IASTDeclarator decl, TypeId type, Graph graph) {
		IASTName name = decl.getName();
		ExecTreeLogger.log("Var name: " + name);
		
		IVar varDecl = addVarDecl(name.toString(), type, decl.getPointerOperators(), graph, _astInterpreter);
		_localVariables.put(name.resolveBinding(), varDecl);
		return varDecl;
	}

	public static IVar addVarDecl(String name, TypeId type, IASTPointerOperator[] pointerOps, 
				Graph graph, AstInterpreterCDT astInterpreter) {
		ExecTreeLogger.log(name);
		FuncParameter.IndirectionType parameterVarType;
		parameterVarType = Function.getIndirectionType(pointerOps);
		IVar var = instanceVar(parameterVarType, name, type, graph, astInterpreter);
		return var;
	}
	
	public IVar getVarFromBinding(IBinding binding, CodeLocation codeLoc) {
		IVar var = _localVariables.get(binding);
		if(var != null)
			return var;
		
		if( _parent != null) {
			var = _parent.getVarFromBinding(binding, codeLoc);
			if(var != null)
				return var;
		}

		return _astInterpreter.getGlobalVar(binding, codeLoc);
	}
	
}
