package gvpl.cdt;

import gvpl.cdt.function.Function;
import gvpl.cdt.function.MemberFunc;
import gvpl.common.AstInterpreter;
import gvpl.common.AstLoader;
import gvpl.common.ClassVar;
import gvpl.common.FuncParameter;
import gvpl.common.IClassVar;
import gvpl.common.IVar;
import gvpl.common.MemberId;
import gvpl.common.TypeId;
import gvpl.common.Var;
import gvpl.common.VarInfo;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.LinkedHashMap;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;

public abstract class AstLoaderCDT extends AstLoader {
	
	static Logger logger = LogManager.getLogger(Graph.class.getName());
	
	protected AstInterpreterCDT _astInterpreter;
	private Map<IBinding, IVar> _localVariables = new LinkedHashMap<IBinding, IVar>();
	protected Map<IBinding, IVar> _extToInVars = new LinkedHashMap<IBinding, IVar>();

	public AstLoaderCDT(AstInterpreterCDT astInterpreter) {
		_astInterpreter = astInterpreter;
	}

	protected IVar getVarFromExpr(IASTExpression expr) {
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
	    	tempVar.receiveAssign(NodeType.E_DIRECT_VALUE, node, _gvplGraph);
	    	return tempVar;
	    }
		
		return getVarFromBinding(getBindingFromExpr(expr));
	}
	
	protected GraphNode getNodeFromExpr(IASTExpression expr) {
		IVar var = getVarFromExpr(expr);
		if(var != null)
			return var.getCurrentNode();
		return null;
	}
	
	abstract protected IVar getVarFromBinding(IBinding binding);
	
	protected IVar getLocalVar(IBinding binding) {
		return _localVariables.get(binding);
	}
	
	protected IBinding getBindingFromExpr(IASTExpression expr) {
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
		} else if (expr instanceof CPPASTLiteralExpression) {
			//probably "this" pointer
			return null;
		} else {
			logger.fatal("Type not found {}", expr.getClass());
		}
		return null;
	}
	
	protected IVar getPreLoadedVarFromBinding(IBinding binding) {		
		IVar var = _extToInVars.get(binding);
		if(var != null)
			return var;
		
		return _localVariables.get(binding);
	}
	
	public VarInfo getTypeFromVarBinding(IBinding binding) {
		IVar var = _localVariables.get(binding);
		if(var == null)
			return null;
		
		return var.getVarInfo();
	}
	
	protected IVar getVarFromExprInternal(IASTExpression expr) {
		if (expr instanceof IASTIdExpression)
			return getLocalVarFromIdExpr((IASTIdExpression) expr);
		else if (expr instanceof IASTFieldReference) {
			return getVarFromFieldRef((IASTFieldReference) expr);
		} else if (expr instanceof IASTUnaryExpression) {
			IASTExpression opExpr = ((IASTUnaryExpression) expr).getOperand();
			return getVarFromExprInternal(opExpr);
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
		IVar var_decl = addVarDecl(name.toString(), type, decl.getPointerOperators(), graph);
		_localVariables.put(name.resolveBinding(), var_decl);

		return var_decl;
	}

	public IVar addVarDecl(String name, TypeId type, IASTPointerOperator[] pointerOps, Graph graph) {
		FuncParameter.IndirectionType parameterVarType;
		parameterVarType = Function.getIndirectionType(pointerOps);
		IVar var = instanceVar(parameterVarType, name, type, graph, this, _astInterpreter);
		if(var instanceof ClassVar)
			_varsCreatedInThisScope.add((ClassVar) var);
		return var;
	}
	
	@Override
	public AstInterpreter getAstInterpreter() {
		return _astInterpreter;
	}
}
