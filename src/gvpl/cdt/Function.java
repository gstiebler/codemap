package gvpl.cdt;

import gvpl.common.ClassVar;
import gvpl.common.ErrorOutputter;
import gvpl.common.FuncParameter;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.common.MemAddressVar;
import gvpl.common.MemberId;
import gvpl.common.TypeId;
import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
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

	private GraphNode _return_node;

	private String _externalName = "";
	public List<FuncParameter> _parameters;
	protected String _funcName;

	public Function(Graph gvplGraph, AstLoader parent, AstInterpreter astInterpreter) {
		super(new Graph(-1), parent, astInterpreter);

		_parameters = new ArrayList<FuncParameter>();
		_return_node = null;
	}

	/**
	 * Loads the member function definition
	 * 
	 * @param fd
	 *            The ast function definition
	 * @return The binding of the loaded function member
	 */
	public IBinding load(IASTFunctionDefinition fd) {
		int startingLine = fd.getFileLocation().getStartingLineNumber();

		// The declaration of the function
		CPPASTFunctionDeclarator decl = (CPPASTFunctionDeclarator) fd.getDeclarator();
		IASTParameterDeclaration[] parameters = decl.getParameters();
		IASTName name_binding = decl.getName();
		// Gets the name of the function
		if (name_binding instanceof CPPASTQualifiedName) {
			_funcName = ((CPPASTQualifiedName) name_binding).getNames()[1].toString();
		} else {
			_funcName = name_binding.toString();
		}

		IBinding member_func_binding = name_binding.resolveBinding();
		setName(calcName());

		loadFuncParameters(parameters);

		for (FuncParameter parameter : _parameters) {
			parameter.getVar().initializeGraphNode(NodeType.E_DECLARED_PARAMETER, _gvplGraph, this,
					_astInterpreter, startingLine);
		}

		loadConstructorChain(decl.getConstructorChain());

		IASTStatement body = fd.getBody();
		if (body instanceof IASTCompoundStatement) {
			BasicBlock basicBlockLoader = new BasicBlock(this, _astInterpreter, null);
			basicBlockLoader.load(body);
		} else
			ErrorOutputter.fatalError("Work here.");

		return member_func_binding;
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

			FuncParameter.IndirectionType parameterVarType = null;
			parameterVarType = getIndirectionType(parameter.getDeclarator().getPointerOperators());

			_parameters.add(new FuncParameter(var_decl, parameterVarType));
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
		if (_parameters.size() != callingParameters.size())
			ErrorOutputter.fatalError("Number of parameters differs from func declaration!");

		for (int i = 0; i < callingParameters.size(); ++i) {
			Var declaredParameter = _parameters.get(i).getVar();
			FuncParameter callingParameter = callingParameters.get(i);

			Var receivedVar = callingParameter.getVar();
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
							.addDependentNode(declParamNodeInMainGraph, this, startingLine);
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

		callingParameter.receiveAssign(NodeType.E_VARIABLE, declParamNodeInMainGraph, null, startingLine);
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
			callingParameterNode.addDependentNode(declParamNodeInMainGraph, this, startingLine);
		}
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

}
