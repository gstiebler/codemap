package gvpl.cdt.function;

import gvpl.cdt.AstInterpreterCDT;
import gvpl.cdt.ClassDeclCDT;
import gvpl.cdt.InstructionLine;
import gvpl.common.AstLoader;
import gvpl.common.ClassMember;
import gvpl.common.ClassVar;
import gvpl.common.FuncParameter;
import gvpl.common.IVar;
import gvpl.common.MemberId;
import gvpl.common.VarInfo;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
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
		super(new Graph(), null, astInterpreter, ownBinding);
		_parentClass = parent;
		
		_thisVar = new ClassVar(_gvplGraph, "THIS", parent, this);
		_thisVar.initializeVar(NodeType.E_VARIABLE, _gvplGraph, this, _astInterpreter);
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
	void loadConstructorChain(ICPPASTConstructorChainInitializer[] constructorInit) {
		for (ICPPASTConstructorChainInitializer initializer : constructorInit) {
			IASTExpression expr = initializer.getInitializerValue();
			int startingLine = expr.getFileLocation().getStartingLineNumber();
			DebugOptions.setStartingLine(startingLine);
			
			IASTName memberInitId = initializer.getMemberInitializerId();
			IBinding memberBinding = memberInitId.resolveBinding();
			InstructionLine instructionLine = new InstructionLine(_gvplGraph, this, _astInterpreter);

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
					memberFunc.loadMemberFuncRef(_thisVar, parameters, _gvplGraph, this);
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
		IVar var = super.getVarFromBinding(binding);
		if(var != null)
			return var;
		
		//if the variable isn't in scope, create it
		return createVarFromBinding(binding);
	}
	
	@Override
	public VarInfo getTypeFromVarBinding(IBinding binding) {
		ClassMember classMember = _parentClass.getMember(binding);
		if(classMember != null)
			return classMember.getVarInfo();
		
		VarInfo varInfo = super.getTypeFromVarBinding(binding);
		if(varInfo != null)
			return varInfo;
		
		return _parent.getTypeFromVarBinding(binding);
	}
	
	/**
	 * Copy the internal graph to the main graph and bind the variables of the
	 * structure to the used variables in the member function
	 * 
	 * @param classVar
	 * @param graphBuilder
	 */
	public GraphNode loadMemberFuncRef(ClassVar classVar, List<FuncParameter> parameterValues,
			Graph graph, AstLoader astLoader) {
		Map<GraphNode, GraphNode> internalToMainGraphMap = graph.addSubGraph(_gvplGraph, this);
		
		// binds the "this" pointer as a normal parameter
		bindInParameter(internalToMainGraphMap, classVar, _thisVar); 
		bindOutParameter(internalToMainGraphMap, classVar, _thisVar);
		
		return addParametersReferenceAndReturn(parameterValues, internalToMainGraphMap);
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
