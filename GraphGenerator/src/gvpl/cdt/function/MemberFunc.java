package gvpl.cdt.function;

import gvpl.cdt.AstInterpreterCDT;
import gvpl.cdt.ClassDeclCDT;
import gvpl.cdt.InstructionLine;
import gvpl.common.BaseScope;
import gvpl.common.ClassMember;
import gvpl.common.ClassVar;
import gvpl.common.CodeLocation;
import gvpl.common.FuncParameter;
import gvpl.common.IVar;
import gvpl.common.MemberId;
import gvpl.common.Value;
import gvpl.exceptions.NotFoundException;
import gvpl.graph.Graph;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;

import debug.DebugOptions;
import debug.ExecTreeLogger;

public class MemberFunc extends Function {
	
	static Logger logger = LogManager.getLogger(Graph.class.getName());

	private ClassDeclCDT _parentClass;
	/** represents the "this" pointer inside the function */
	private ClassVar _thisVar = null;
	/** The equivalent function in a parent class. It's only used if the current
	 * function implements a function in a parent class. */
	private MemberFunc _parentMemberFunc = null;
	
	private InitializedMembers _initializedMembers = null;
	
	public MemberFunc(ClassDeclCDT parent, AstInterpreterCDT astInterpreter, IBinding ownBinding) {
		super(astInterpreter, ownBinding);
		_parentClass = parent;
	}

	protected String calcName() {
		return _parentClass.getName() + "::" + _funcName;
	}
	
	@Override
	public void loadDeclaration(ICPPASTFunctionDeclarator decl) {
		super.loadDeclaration(decl);
		
		// TODO create a special class for constructor functions
		if (_funcName.equals(_parentClass.getName()))
			_parentClass.setConstructorFunc(this);

		// TODO create a special class for destructor functions
		if (_funcName.length() > 0 && _funcName.charAt(0) == '~')
			_parentClass.setDestructorFunc(this);
		
		_parentMemberFunc = _parentClass.getEquivalentFunc(this);
	}

	@Override
	void loadConstructorChain(Graph graph, BaseScope caller) {
		ExecTreeLogger.log("");
		for (ICPPASTConstructorChainInitializer initializer : _ccInitializer) {
			IASTExpression expr = initializer.getInitializerValue();
			if(expr != null) {
				int startingLine = expr.getFileLocation().getStartingLineNumber();
				DebugOptions.setStartingLine(startingLine);
			} else
				continue;
			
			IASTName memberInitId = initializer.getMemberInitializerId();
			IBinding memberBinding = memberInitId.resolveBinding();
			InstructionLine instructionLine = new InstructionLine(graph, this, _astInterpreter);

			if (memberBinding instanceof ICPPField) {
				IVar var = getMemberFromBinding(memberBinding);
				if(var == null) {
					logger.error("Problem with member {}", memberBinding.getName());
					continue;
				}
				instructionLine.loadConstructorInitializer(var, expr);
				MemberId memberId = _parentClass.getMember(memberBinding).getMemberId();
				_initializedMembers._members.add(memberId);
			} else if (memberBinding instanceof ICPPConstructor) {
				for (ClassDeclCDT parentClass : _parentClass.getParentClassesCDT()) {
					MemberFunc memberFunc = parentClass.getMemberFunc(memberBinding);
					if (memberFunc == null)
						continue;

					IASTExpression initValue = initializer.getInitializerValue();
					List<FuncParameter> parameters = instructionLine.loadFunctionParameters(
							memberFunc, initValue);
					memberFunc.addFuncRef(parameters, graph, _thisVar, caller);
					break;
				}
			} else
				logger.fatal("not expected");
		}
	}
	
	private IVar getMemberFromBinding(IBinding binding) {
		ClassMember member = _parentClass.getMember(binding);
		IVar var;
		if(member != null) {
			MemberId memberId = member.getMemberId();
			var = _thisVar.getMember(memberId);
			if(var != null)
				return var;
		} else {
			logger.debug("listing members of class {}, binding {} not found", _parentClass.getName(), binding.toString());
			for(Map.Entry<IBinding, ClassMember> memberES : _parentClass._memberIdMap.entrySet()) {
				logger.debug("member binding: {}, member: {}", memberES.getKey(), 
						memberES.getValue().getName());
			}
		}
		
		return null;
	}

	public Value addFuncRef(List<FuncParameter> parameterValues, Graph gvplGraph, ClassVar thisVar, BaseScope caller) {
		_thisVar = thisVar;
		
		Value result;
		if(_body != null)
			result = super.addFuncRef(parameterValues, gvplGraph, caller);
		else {
			MemberFunc eqFunc = thisVar.getClassDecl().getEquivalentFunc(this);
			if(eqFunc != this)
				result = eqFunc.addFuncRef(parameterValues, gvplGraph, thisVar, caller);
			else
				result = super.addFuncRef(parameterValues, gvplGraph, caller);
		}
		_thisVar = null;
		return result;
	}
	
	@Override
	protected IVar getVarFromExpr(IASTNode expr) throws NotFoundException {
		ExecTreeLogger.log(expr.getRawSignature());
		IVar var = super.getVarFromExpr(expr);
		if (var != null) 
			return var; 
		
		IBinding binding = getBindingFromExpr(expr);
		CodeLocation codeLocation = null;
		var = super.getVarFromBinding(binding, codeLocation);
		if (var != null) 
			return var;
		
		String exprStr = expr.getRawSignature();
		if (exprStr.equals("this")) {
			// quite weird, but to deal with "this" in source code, i had to use "this" here
			MemberFunc thisMemberFunc = (MemberFunc) this;
			return thisMemberFunc.getThisReference();
		}
		
		return null;
	}
	
	public MemberFunc getParentMemberFunc() {
		return _parentMemberFunc;
	}
	
	public ClassDeclCDT getParentClass() {
		return _parentClass;
	}

	public ClassVar getThisReference() {
		return _thisVar;
	}
	
	public void setIsConstructor() {
		_initializedMembers = new InitializedMembers();
	}
	
	public boolean memberIsInitialized(MemberId memberId) {
		return _initializedMembers.contains(memberId);
	}

	@Override
	public IVar getVarFromBinding(IBinding binding, CodeLocation codeLoc) {
		ExecTreeLogger.log(binding.toString());
		if(binding instanceof IProblemBinding) {
			logger.warn("problem binding {}", binding.getName());
			return null;
		}
		
		IVar var = super.getVarFromBinding(binding, codeLoc);
		if(var != null)
			return var;
		
		return getMemberFromBinding(binding);
	}
}
