package gvpl.cdt;

import gvpl.common.DirectVarDecl;
import gvpl.common.ErrorOutputter;
import gvpl.common.FuncParameter;
import gvpl.common.PointerVarDecl;
import gvpl.common.VarDecl;
import gvpl.common.FuncParameter.eParameterType;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.TypeId;
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
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;

public class Function extends AstLoader {
	
	private GraphNode _return_node;

	private String _externalName = "";
	public List<FuncParameter> _parameters;

	public Function(GraphBuilder graph_builder, AstLoader parent, CppMaps cppMaps,
			AstInterpreter astInterpreter) {
		super(new GraphBuilder(cppMaps), parent, cppMaps, astInterpreter);

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
		String function_name = name_binding.toString();

		IBinding member_func_binding = name_binding.resolveBinding();
		setName(calcName(function_name));

		loadFuncParameters(parameters);

		for (FuncParameter parameter : _parameters) {
			parameter.getVar().initializeGraphNode(NodeType.E_DECLARED_PARAMETER, startingLine);
		}

		IASTStatement body = fd.getBody();

		if (body instanceof IASTCompoundStatement) {
			BasicBlock basicBlockLoader = new BasicBlock(this, _astInterpreter, null);
			basicBlockLoader.load(body);
		} else
			ErrorOutputter.fatalError("Work here.");

		return member_func_binding;
	}

	protected String calcName(String internalName) {
		return internalName;
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
			DirectVarDecl var_decl = loadVarDecl(parameter_var_decl, type);
			
			FuncParameter.eParameterType parameterVarType = null;
			parameterVarType = getFuncParameterType(parameter.getDeclarator().getPointerOperators());
			
			_parameters.add(new FuncParameter(var_decl, parameterVarType));
		}
	}

	public GraphNode addFuncRef(List<FuncParameter> parameter_values, GraphBuilder graphBuilder, int startingLine) {
		Map<GraphNode, GraphNode> internalToMainGraphMap = graphBuilder._gvplGraph.addSubGraph(_graphBuilder._gvplGraph, this, startingLine);
		return addParametersReferenceAndReturn(parameter_values, internalToMainGraphMap, startingLine);
	}

	protected GraphNode addParametersReferenceAndReturn(List<FuncParameter> parameter_values,
			Map<GraphNode, GraphNode> internalToMainGraphMap, int startingLine) {
		if (_parameters.size() != parameter_values.size())
			ErrorOutputter.fatalError("Number of parameters differs from func declaration!");

		for (int i = 0; i < parameter_values.size(); ++i) {
			VarDecl declared_parameter = _parameters.get(i).getVar();
			GraphNode declParamNodeInMainGraph = internalToMainGraphMap.get(declared_parameter
					.getFirstNode());
			
			FuncParameter funcParameter = parameter_values.get(i);
			GraphNode received_parameter = funcParameter.getNode(startingLine);

			//Point the received values to the received parameters ([in] parameters)
			if(received_parameter != null) {
				received_parameter.addDependentNode(declParamNodeInMainGraph, this, startingLine);
			}

			//Writes the written pointer parameter values to the pointed variables in the main graph
			// ([out] parameters)
			if(funcParameter.getType() == eParameterType.E_POINTER) {
				if(!(declared_parameter instanceof PointerVarDecl))
					ErrorOutputter.fatalError("problem!");
				
				VarDecl pointedVar = ((PointerVarDecl)declared_parameter).getPointedVarDecl();
				GraphNode pointedNode = internalToMainGraphMap.get(pointedVar.getCurrentNode(startingLine));

				VarDecl varDecl = funcParameter.getVar();
				varDecl.receiveAssign(NodeType.E_VARIABLE, pointedNode, null, startingLine);
			}
		}
		
		

		return internalToMainGraphMap.get(_return_node);
	}

	private void setName(String name) {
		_externalName = name;
		_graphBuilder._gvplGraph.setLabel(name);
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
	
	public static eParameterType getFuncParameterType(IASTPointerOperator[] pointerOps) {
		if(pointerOps == null)
			return eParameterType.E_VARIABLE;
		
		if(pointerOps.length > 0) {
			if(pointerOps[0] instanceof IASTPointer)
				return eParameterType.E_POINTER;
			else if (pointerOps[0] instanceof IASTReference)
				return eParameterType.E_REFERENCE;
		}
		else
			return eParameterType.E_VARIABLE;
		
		return null;
	}

}
