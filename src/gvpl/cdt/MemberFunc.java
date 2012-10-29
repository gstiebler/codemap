package gvpl.cdt;

import gvpl.common.ClassMember;
import gvpl.common.ClassVar;
import gvpl.common.FuncParameter;
import gvpl.common.GeneralOutputter;
import gvpl.common.IVar;
import gvpl.common.MemberId;
import gvpl.common.VarInfo;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPField;

public class MemberFunc extends Function {

	private ClassDecl _parentClass;
	/** represents the "this" pointer inside the function */
	private ClassVar _thisVar = null;
	/** The equivalent function in a parent class. It's only used if the current
	 * function implements a function in a parent class. */
	private MemberFunc _parentMemberFunc = null;
	
	public MemberFunc(ClassDecl parent, AstInterpreterCDT astInterpreter, IBinding ownBinding, int startingLine) {
		super(new Graph(startingLine), null, astInterpreter, ownBinding);
		_parentClass = parent;
		
		_thisVar = new ClassVar(_gvplGraph, "THIS", parent, this);
		_thisVar.initializeVar(NodeType.E_VARIABLE, _gvplGraph, this, _astInterpreter, startingLine);
	}

	protected String calcName() {
		return _parentClass.getName() + "::" + _funcName;
	}
	
	@Override
	public void loadDeclaration(CPPASTFunctionDeclarator decl, int startingLine) {
		super.loadDeclaration(decl, startingLine);
		
		if (_funcName.equals(_parentClass.getName()))
			_parentClass.setConstructorFunc(this);
		
		_parentMemberFunc = _parentClass.getEquivalentFunc(this);
	}

	@Override
	void loadConstructorChain(ICPPASTConstructorChainInitializer[] constructorInit) {
		for (ICPPASTConstructorChainInitializer initializer : constructorInit) {
			int startingLine = initializer.getFileLocation().getStartingLineNumber();

			IASTExpression expr = initializer.getInitializerValue();

			IASTName memberInitId = initializer.getMemberInitializerId();
			IBinding memberBinding = memberInitId.resolveBinding();
			InstructionLine instructionLine = new InstructionLine(_gvplGraph, this, _astInterpreter);

			if (memberBinding instanceof CPPField) {
				IVar var = getVarFromBinding(memberBinding);
				instructionLine.loadConstructorInitializer(var, expr, startingLine);
			} else if (memberBinding instanceof CPPConstructor) {
				for (ClassDecl parentClass : _parentClass.getParentClasses()) {
					MemberFunc memberFunc = parentClass.getMemberFunc(memberBinding);
					if (memberFunc == null)
						continue;

					IASTExpression initValue = initializer.getInitializerValue();
					List<FuncParameter> parameters = instructionLine.loadFunctionParameters(
							memberFunc, initValue);
					memberFunc.loadMemberFuncRef(_thisVar, parameters, _gvplGraph, this, startingLine);
					break;
				}
			} else
				GeneralOutputter.fatalError("not expected");
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
		return createVarFromBinding(binding, -2);
	}
	
	@Override
	protected VarInfo getTypeFromVarBinding(IBinding binding) {
		ClassMember classMember = _parentClass.getMember(binding);
		if(classMember != null)
			return classMember.getVarInfo();
		
		VarInfo varInfo = super.getTypeFromVarBinding(binding);
		if(varInfo != null)
			return varInfo;
		
		return _parent.getTypeFromVarBinding(binding);
	}
	
	@Override
	public GraphNode addFuncRef(List<FuncParameter> parameter_values, Graph gvplGraph,
			int startingLine) {
		GeneralOutputter.fatalError("Error! Should call loadMemberFuncRef instead.");
		return null;
	}
	
	/**
	 * Copy the internal graph to the main graph and bind the variables of the
	 * structure to the used variables in the member function
	 * 
	 * @param classVar
	 * @param graphBuilder
	 */
	public GraphNode loadMemberFuncRef(ClassVar classVar, List<FuncParameter> parameterValues,
			Graph graph, AstLoader astLoader, int startingLine) {
		Map<GraphNode, GraphNode> internalToMainGraphMap = graph.addSubGraph(_gvplGraph, this,
				startingLine);
		
		// binds the "this" pointer as a normal parameter
		bindInParameter(internalToMainGraphMap, classVar, _thisVar, startingLine); 
		bindOutParameter(internalToMainGraphMap, classVar, _thisVar, startingLine);
		
		return addParametersReferenceAndReturn(parameterValues, internalToMainGraphMap,
				startingLine);
	}
	
	public MemberFunc getParentMemberFunc() {
		return _parentMemberFunc;
	}
	
	public ClassDecl getParentClass() {
		return _parentClass;
	}

	public ClassVar getThisReference() {
		return _thisVar;
	}
}
