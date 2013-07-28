package gvpl.cdt.function;

import gvpl.cdt.AstInterpreterCDT;
import gvpl.cdt.ClassDeclCDT;
import gvpl.cdt.InstructionLine;
import gvpl.common.ClassMember;
import gvpl.common.ClassVar;
import gvpl.common.FuncParameter;
import gvpl.common.IScope;
import gvpl.common.IVar;
import gvpl.common.MemberId;
import gvpl.common.Value;
import gvpl.graph.Graph;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPField;

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
	public void loadDeclaration(CPPASTFunctionDeclarator decl) {
		super.loadDeclaration(decl);
		
		if (_funcName.equals(_parentClass.getName()))
			_parentClass.setConstructorFunc(this);
		
		if (_funcName.charAt(0) == '~')
			_parentClass.setDestructorFunc(this);
		
		_parentMemberFunc = _parentClass.getEquivalentFunc(this);
	}

	@Override
	void loadConstructorChain(Graph graph, IScope caller) {
		ExecTreeLogger.log("");
		for (ICPPASTConstructorChainInitializer initializer : _ccInitializer) {
			IASTExpression expr = initializer.getInitializerValue();
			int startingLine = expr.getFileLocation().getStartingLineNumber();
			DebugOptions.setStartingLine(startingLine);
			
			IASTName memberInitId = initializer.getMemberInitializerId();
			IBinding memberBinding = memberInitId.resolveBinding();
			InstructionLine instructionLine = new InstructionLine(graph, this, _astInterpreter);

			if (memberBinding instanceof CPPField) {
				IVar var = getVarFromBinding(memberBinding);
				instructionLine.loadConstructorInitializer(var, expr);
				MemberId memberId = _parentClass.getMember(memberBinding).getMemberId();
				_initializedMembers._members.add(memberId);
			} else if (memberBinding instanceof CPPConstructor) {
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
			logger.debug("listing members of class {}, binding {} not found", _parentClass.getName(), binding);
			for(Map.Entry<IBinding, ClassMember> memberES : _parentClass._memberIdMap.entrySet()) {
				logger.debug("member binding: {}, member: {}", memberES.getKey(), 
						memberES.getValue().getName());
			}
		}
		
		return null;
	}

	public Value addFuncRef(List<FuncParameter> parameterValues, Graph gvplGraph, ClassVar thisVar, IScope caller) {
		_thisVar = thisVar;
		Value result = super.addFuncRef(parameterValues, gvplGraph, caller);
		_thisVar = null;
		return result;
	}
	
	@Override
	protected IVar getVarFromExpr(IASTExpression expr) {
		ExecTreeLogger.log(expr.getRawSignature());
		IVar var = super.getVarFromExpr(expr);
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
	public IVar getVarFromBinding(IBinding binding) {
		ExecTreeLogger.log(binding.getName());
		if(binding instanceof ProblemBinding) {
			logger.info("problem binding");
			return null;
		}
		return getMemberFromBinding(binding);
//		//TODO refactoring: this decision should be on BaseScope. This decision should be removed
//		// from getAccessedVars
//		if(_caller.hasVarInScope(binding)) {
//			return getVarInsideSandboxFromBinding(binding);
//		} else {
//			return getVarFromBindingUnbounded(binding);
//		}
	}
	
	@Override
	public boolean hasVarInScope(IBinding binding) {
		if(super.hasVarInScope(binding))
			return true;
		
		return getMemberFromBinding(binding) != null;
	}
}
