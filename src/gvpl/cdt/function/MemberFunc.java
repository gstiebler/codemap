package gvpl.cdt.function;

import gvpl.cdt.AstInterpreterCDT;
import gvpl.cdt.ClassDeclCDT;
import gvpl.cdt.CodeLocationCDT;
import gvpl.cdt.InstructionLine;
import gvpl.common.ClassMember;
import gvpl.common.ClassVar;
import gvpl.common.FuncParameter;
import gvpl.common.IVar;
import gvpl.common.MemberId;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPField;

import debug.DebugOptions;

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
		super(new Graph(), astInterpreter, ownBinding);
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
	void loadConstructorChain(ICPPASTConstructorChainInitializer[] constructorInit, Graph graph, ClassVar thisVar) {
		for (ICPPASTConstructorChainInitializer initializer : constructorInit) {
			IASTExpression expr = initializer.getInitializerValue();
			int startingLine = expr.getFileLocation().getStartingLineNumber();
			DebugOptions.setStartingLine(startingLine);
			
			IASTName memberInitId = initializer.getMemberInitializerId();
			IBinding memberBinding = memberInitId.resolveBinding();
			InstructionLine instructionLine = new InstructionLine(graph, this, _astInterpreter);

			if (memberBinding instanceof CPPField) {
				IVar var = getPreLoadedVarFromBinding(memberBinding);
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
					memberFunc.addFuncRef(parameters, graph, thisVar);
					break;
				}
			} else
				logger.fatal("not expected");
		}
	}

	@Override
	protected IVar getVarFromBinding(IBinding binding) {
		ClassMember member = _parentClass.getMember(binding);
		if(member != null) {
			MemberId memberId = member.getMemberId();
			IVar var = _thisVar.getMember(memberId);
			if(var != null)
				return var;
		}
		
		// search the variable in the function parameters
		return super.getVarFromBinding(binding);
	}

	public GraphNode addFuncRef(List<FuncParameter> parameterValues, Graph gvplGraph, ClassVar thisVar) {
		_thisVar = thisVar;
		GraphNode result = super.addFuncRef(parameterValues, gvplGraph);
		_thisVar = null;
		return result;
	}
	
	@Override
	public void loadDefinition(Graph gvplGraph) {
		if(_implLocation != null) //function definition has already been loaded
			return;
		
		loadConstructorChain(_ccInitializer, gvplGraph, _thisVar);
		super.loadDefinition(gvplGraph);
	}
	
	@Override
	protected IVar getVarFromExpr(IASTExpression expr) {
		IVar var = super.getVarFromExpr(expr);

		if (var != null) 
			return var; 
		
		String exprStr = expr.getRawSignature();
		if (exprStr.equals("this")) {
			// quite weird, but to deal with "this" in source code, i had to use "this" here
			MemberFunc thisMemberFunc = (MemberFunc) this;
			return thisMemberFunc.getThisReference();
		}
		
		logger.fatal("not supposed to be here");
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
}
