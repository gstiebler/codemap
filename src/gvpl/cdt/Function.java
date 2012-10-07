package gvpl.cdt;

import gvpl.common.ClassVar;
import gvpl.common.ErrorOutputter;
import gvpl.common.FuncParameter;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.common.MemAddressVar;
import gvpl.common.MemberId;
import gvpl.common.TypeId;
import gvpl.common.Var;
import gvpl.common.VarInfo;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

public class Function extends AstLoader {

	private GraphNode _return_node = null;
	private TypeId _returnType = null;

	private String _externalName = "";
	private Map<IBinding, FuncParameter> _parametersMap = new LinkedHashMap<IBinding, FuncParameter>();
	private List<FuncParameter> _parametersList = new ArrayList<FuncParameter>();
	protected String _funcName;

	public Function(Graph gvplGraph, AstLoader parent, AstInterpreter astInterpreter) {
		super(new Graph(-1), parent, astInterpreter);
		
		//TODO FIX!!
		_returnType = _astInterpreter.getPrimitiveType();
	}
	
	public void loadDeclaration(CPPASTFunctionDeclarator decl, int startingLine) {
		IASTParameterDeclaration[] parameters = decl.getParameters();
		IASTName name_binding = decl.getName();
		// Gets the name of the function
		if (name_binding instanceof CPPASTQualifiedName)
			_funcName = ((CPPASTQualifiedName) name_binding).getNames()[1].toString();
		else
			_funcName = name_binding.toString();

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
			ErrorOutputter.fatalError("Work here.");
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
			IASTDeclarator parameter_var_decl = parameter.getDeclarator();
			IASTDeclSpecifier decl_spec = parameter.getDeclSpecifier();
			TypeId type = _astInterpreter.getType(decl_spec);
			Var var_decl = loadVarDecl(parameter_var_decl, type);
			IBinding binding = parameter_var_decl.getName().resolveBinding();

			FuncParameter.IndirectionType parameterVarType = null;
			parameterVarType = getIndirectionType(parameter.getDeclarator().getPointerOperators());

			addParameter(binding, new FuncParameter(var_decl, parameterVarType));
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
			ErrorOutputter.fatalError("Number of parameters differs from func declaration!");

		for (int i = 0; i < callingParameters.size(); ++i) {
			Var declaredParameter = _parametersList.get(i).getVar();
			FuncParameter callingParameter = callingParameters.get(i);

			Var receivedVar = callingParameter.getVar();
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

		return internalToMainGraphMap.get(_return_node);
	}

	void bindInParameter(Map<GraphNode, GraphNode> internalToMainGraphMap, Var callingParameter,
			Var declaredParameter, int startingLine) {
		if (callingParameter instanceof ClassVar) {
			ClassVar callingParameterClass = (ClassVar) callingParameter;
			ClassVar declaredParameterClass = (ClassVar) declaredParameter;
			for (MemberId memberId : callingParameterClass.getClassDecl().getMemberIds()) {
				Var callingParameterChild = callingParameterClass.getMember(memberId);
				Var declaredParameterChild = declaredParameterClass.getMember(memberId);

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
		if (callingParameterNode != null) {
			callingParameterNode.addDependentNode(declParamNodeInMainGraph, startingLine);
		}
	}

	void bindOutParameter(Map<GraphNode, GraphNode> internalToMainGraphMap, Var callingParameter,
			Var declaredParameter, int startingLine) {
		if (callingParameter instanceof ClassVar) {
			ClassVar callingParameterClass = (ClassVar) callingParameter;
			ClassVar declaredParameterClass = (ClassVar) declaredParameter;
			for (MemberId memberId : callingParameterClass.getClassDecl().getMemberIds()) {
				Var callingParameterChild = callingParameterClass.getMember(memberId);
				Var declaredParameterChild = declaredParameterClass.getMember(memberId);

				bindOutParameter(internalToMainGraphMap, callingParameterChild.getVarInMem(),
						declaredParameterChild.getVarInMem(), startingLine);
			}
			return;
		}

		GraphNode declParamNodeInMainGraph = internalToMainGraphMap.get(declaredParameter
				.getCurrentNode(startingLine));

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
		_return_node = returnNode;
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
		ErrorOutputter.fatalError("error not expected");
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
	protected Var getVarFromBinding(IBinding binding) {
		FuncParameter funcParameter = _parametersMap.get(binding);
		if(funcParameter != null)
			return funcParameter.getVar().getVarInMem();
		
		Var var = super.getVarFromBinding(binding);
		if(var != null)
			return var;
		
		return null;
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

}
