package gvpl.cdt;

import gvpl.common.ClassVar;
import gvpl.common.GeneralOutputter;
import gvpl.common.FuncParameter;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.common.MemAddressVar;
import gvpl.common.MemberId;
import gvpl.common.TypeId;
import gvpl.common.IVar;
import gvpl.common.VarInfo;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReferenceOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;

public class Function extends AstLoader {

	private GraphNode _returnNode = null;
	private TypeId _returnType = null;

	private String _externalName = "";
	private Map<IBinding, FuncParameter> _parametersMap = new LinkedHashMap<IBinding, FuncParameter>();
	private List<FuncParameter> _parametersList = new ArrayList<FuncParameter>();
	protected String _funcName;
	protected IBinding _ownBinding;

	public Function(Graph gvplGraph, AstLoader parent, AstInterpreter astInterpreter, IBinding ownBinding) {
		super(new Graph(-1), parent, astInterpreter);
		
		_ownBinding = ownBinding;
		//TODO FIX!!
		_returnType = _astInterpreter.getPrimitiveType();
	}
	
	public void loadDeclaration(CPPASTFunctionDeclarator decl, int startingLine) {
		IASTParameterDeclaration[] parameters = decl.getParameters();
		IASTName astName = decl.getName();
		// Gets the name of the function
		if (astName instanceof CPPASTQualifiedName)
			_funcName = ((CPPASTQualifiedName) astName).getNames()[1].toString();
		else
			_funcName = astName.toString();

		setName(calcName());

		loadFuncParameters(parameters);

		for (Map.Entry<IBinding, FuncParameter> entry : _parametersMap.entrySet()) {
			entry.getValue().getVar().initializeGraphNode(NodeType.E_DECLARED_PARAMETER, _gvplGraph, this,
					_astInterpreter, startingLine);
		}
	}
	
	public void loadDefinition(ICPPASTConstructorChainInitializer[] ccInitializer, IASTStatement body) {
		loadConstructorChain(ccInitializer);
		
		if (body instanceof IASTCompoundStatement) {
			IASTStatement[] statements = ((IASTCompoundStatement)body).getStatements();
			for (IASTStatement statement : statements) {
				InstructionLine instructionLine = new InstructionLine(_gvplGraph, this, _astInterpreter);
				instructionLine.load(statement);
			}
		} else
			GeneralOutputter.fatalError("Work here.");
	}
	
	public TypeId getReturnTypeId() {
		return _returnType;
	}

	void loadConstructorChain(ICPPASTConstructorChainInitializer[] constructorInit) {
	}

	protected String calcName() {
		return _funcName;
	}

	/**
	 * Reads the parameters from a function declaration
	 * 
	 * @param parameters
	 *            Parameters of the function
	 * @param func_decl
	 *            Declaration of the function
	 * @param basicBlockLoader
	 *            The class which loads the function definition
	 */
	public void loadFuncParameters(IASTParameterDeclaration[] parameters) {
		for (IASTParameterDeclaration parameter : parameters) {
			IASTDeclarator parameterVarDecl = parameter.getDeclarator();
			IASTDeclSpecifier declSpec = parameter.getDeclSpecifier();
			TypeId type = _astInterpreter.getType(declSpec);
			IVar var_decl = loadVarDecl(parameterVarDecl, type);
			IBinding binding = parameterVarDecl.getName().resolveBinding();

			FuncParameter.IndirectionType parameterVarType = null;
			parameterVarType = getIndirectionType(parameter.getDeclarator().getPointerOperators());
			FuncParameter funcParameter = new FuncParameter(var_decl, parameterVarType);
			if(declSpec instanceof CPPASTSimpleDeclSpecifier)
				funcParameter.setType(((CPPASTSimpleDeclSpecifier)declSpec).getType());

			addParameter(binding, funcParameter);
		}
	}

	public GraphNode addFuncRef(List<FuncParameter> parameter_values, Graph gvplGraph,
			int startingLine) {
		Map<GraphNode, GraphNode> internalToMainGraphMap = gvplGraph.addSubGraph(_gvplGraph, this,
				startingLine);
		return addParametersReferenceAndReturn(parameter_values, internalToMainGraphMap,
				startingLine);
	}

	protected GraphNode addParametersReferenceAndReturn(List<FuncParameter> callingParameters,
			Map<GraphNode, GraphNode> internalToMainGraphMap, int startingLine) {
		if (_parametersMap.size() != callingParameters.size())
			GeneralOutputter.fatalError("Number of parameters differs from func declaration!");

		for (int i = 0; i < callingParameters.size(); ++i) {
			IVar declaredParameter = _parametersList.get(i).getVar();
			FuncParameter callingParameter = callingParameters.get(i);

			IVar receivedVar = callingParameter.getVar();
			if(receivedVar != null)
				receivedVar = receivedVar.getVarInMem();
			if (receivedVar instanceof ClassVar) {
				// Binds the received parameter to the function parameter
				bindInParameter(internalToMainGraphMap, (ClassVar) receivedVar,
						declaredParameter.getVarInMem(), startingLine);
			} else {
				GraphNode receivedParameter = callingParameter.getNode(startingLine);

				// Point the received values to the received parameters ([in]
				// parameters)
				if (receivedParameter != null) {
					GraphNode declParamNodeInMainGraph = internalToMainGraphMap
							.get(declaredParameter.getFirstNode());

					receivedParameter
							.addDependentNode(declParamNodeInMainGraph, startingLine);
				}
			}

			// Writes the written pointer parameter values to the pointed
			// variables in the main graph
			// ([out] parameters)
			if (declaredParameter instanceof MemAddressVar) {
				bindOutParameter(internalToMainGraphMap, callingParameter.getVar().getVarInMem(),
						declaredParameter.getVarInMem(), startingLine);
			}
		}

		return internalToMainGraphMap.get(_returnNode);
	}

	protected void bindInParameter(Map<GraphNode, GraphNode> internalToMainGraphMap,
			IVar callingParameter, IVar declaredParameter, int startingLine) {
		if (callingParameter instanceof ClassVar) {
			ClassVar callingParameterClass = (ClassVar) callingParameter;
			ClassVar declaredParameterClass = (ClassVar) declaredParameter;
			Set<MemberId> members = callingParameterClass.getClassDecl().getMemberIds();
			for (MemberId memberId : members) {
				IVar callingParameterChild = callingParameterClass.getMember(memberId);
				IVar declaredParameterChild = declaredParameterClass.getMember(memberId);

				if (declaredParameterChild == null)
					continue;

				bindInParameter(internalToMainGraphMap, callingParameterChild.getVarInMem(),
						declaredParameterChild.getVarInMem(), startingLine);
			}
			return;
		}

		GraphNode declParamNodeInMainGraph = internalToMainGraphMap.get(declaredParameter
				.getFirstNode());
		GraphNode callingParameterNode = callingParameter.getCurrentNode(startingLine);

		// Point the received values to the received parameters ([in]
		// parameters)
		if (callingParameterNode != null && declParamNodeInMainGraph != null
				&& (declParamNodeInMainGraph.getNumDependentNodes() > 0)) {
			callingParameterNode.addDependentNode(declParamNodeInMainGraph, startingLine);
		}
	}

	protected void bindOutParameter(Map<GraphNode, GraphNode> internalToMainGraphMap, IVar callingParameter,
			IVar declaredParameter, int startingLine) {
		if (callingParameter instanceof ClassVar) {
			ClassVar callingParameterClass = (ClassVar) callingParameter;
			ClassVar declaredParameterClass = (ClassVar) declaredParameter;
			for (MemberId memberId : callingParameterClass.getClassDecl().getMemberIds()) {
				IVar callingParameterChild = callingParameterClass.getMember(memberId);
				IVar declaredParameterChild = declaredParameterClass.getMember(memberId);

				if(declaredParameterChild == null)
					continue;

				bindOutParameter(internalToMainGraphMap, callingParameterChild.getVarInMem(),
						declaredParameterChild.getVarInMem(), startingLine);
			}
			return;
		}

		GraphNode declParamNodeInMainGraph = internalToMainGraphMap.get(declaredParameter
				.getCurrentNode(startingLine));

		if(declaredParameter.onceWritten())
			callingParameter.receiveAssign(NodeType.E_VARIABLE, declParamNodeInMainGraph, startingLine);
	}

	private void setName(String name) {
		_externalName = name;
		_gvplGraph.setLabel(name);
	}

	public String getName() {
		return _externalName;
	}

	public void setReturnNode(GraphNode returnNode) {
		_returnNode = returnNode;
	}

	@Override
	public Function getFunction() {
		return this;
	}

	public static IndirectionType getIndirectionType(IASTPointerOperator[] pointerOps) {
		if (pointerOps == null)
			return IndirectionType.E_VARIABLE;

		if (pointerOps.length > 0) {
			if (pointerOps[0] instanceof IASTPointer)
				return IndirectionType.E_POINTER;
			else if (pointerOps[0] instanceof CPPASTReferenceOperator)
				return IndirectionType.E_REFERENCE;
		} else
			return IndirectionType.E_VARIABLE;
		GeneralOutputter.fatalError("error not expected");
		return null;
	}
	
	private void addParameter(IBinding binding, FuncParameter parameter) {
		_parametersMap.put(binding, parameter);
		_parametersList.add(parameter);
	}
	
	public FuncParameter getParameter(int index) {
		return _parametersList.get(index);
	}
	
	public int getNumParameters() {
		return _parametersList.size();
	}

	@Override
	protected IVar getVarFromBinding(IBinding binding) {
		FuncParameter funcParameter = _parametersMap.get(binding);
		if(funcParameter != null)
			return funcParameter.getVar().getVarInMem();
		
		return super.getVarFromBinding(binding);
	}
	
	@Override
	protected VarInfo getTypeFromVarBinding(IBinding binding) {
		FuncParameter funcParameter = _parametersMap.get(binding);
		if(funcParameter != null)
			return funcParameter.getVar().getVarInfo();
		
		VarInfo varInfo = super.getTypeFromVarBinding(binding);
		if(varInfo != null)
			return varInfo;
		
		return _parent.getTypeFromVarBinding(binding);
	}
	
	//TODO improve! Can be done with bindings? There is a function in Eclipse IDE that
	// do this. It points the implementations of a function
	/**
	 * Returns true if the declarations are equivalent. It's used to verify if one function
	 * is the implementation of the other in a derived class 
	 * @param other Other function to compare
	 * @return True if the two functions are equivalent
	 */
	public boolean isDeclarationEquivalent(Function other) {
		if(!_funcName.equals(other._funcName))
			return false;
		
		if(_returnType != other._returnType)
			return false;
		
		if(_parametersList.size() != other._parametersList.size())
			return false;
		
		for(int i = 0; i < _parametersList.size(); ++i) {
			FuncParameter internal = _parametersList.get(i);
			FuncParameter external = other._parametersList.get(i);
			if(!internal.isEquivalent(external))
				return false;
		}
		
		return true;
	}
	
	public IBinding getBinding() {
		return _ownBinding;
	}

}
