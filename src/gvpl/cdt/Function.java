package gvpl.cdt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gvpl.common.ErrorOutputter;
import gvpl.common.VarDecl;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphNode;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder.DirectVarDecl;
import gvpl.graph.GraphBuilder.TypeId;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;

public class Function extends AstLoader {

	private GraphNode _return_node;

	private String _externalName = "";
	public List<VarDecl> _parameters;

	public Function(GraphBuilder graph_builder, AstLoader parent, CppMaps cppMaps,
			AstInterpreter astInterpreter) {
		super(new GraphBuilder(), parent, cppMaps, astInterpreter);

		_parameters = new ArrayList<VarDecl>();
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

		// The declaration of the function
		CPPASTFunctionDeclarator decl = (CPPASTFunctionDeclarator) fd.getDeclarator();
		IASTParameterDeclaration[] parameters = decl.getParameters();
		IASTName name_binding = decl.getName();
		// Gets the name of the function
		String function_name = name_binding.toString();

		IBinding member_func_binding = name_binding.resolveBinding();
		setName(calcName(function_name));

		loadFuncParameters(parameters);

		for (VarDecl parameter : _parameters) {
			parameter.initializeGraphNode(NodeType.E_DECLARED_PARAMETER);
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
			DirectVarDecl var_decl = load_var_decl(parameter_var_decl, type);
			_parameters.add(var_decl);
		}
	}

	public GraphNode addFuncRef(List<GraphNode> parameter_values, GraphBuilder graphBuilder) {
		Map<GraphNode, GraphNode> internalToMainGraphMap = graphBuilder._gvplGraph.addSubGraph(_graphBuilder._gvplGraph, this);
		return addParametersReferenceAndReturn(parameter_values, internalToMainGraphMap);
	}

	protected GraphNode addParametersReferenceAndReturn(List<GraphNode> parameter_values,
			Map<GraphNode, GraphNode> internalToMainGraphMap) {
		if (_parameters.size() != parameter_values.size())
			ErrorOutputter.fatalError("Number of parameters differs from func declaration!");

		for (int i = 0; i < parameter_values.size(); ++i) {
			VarDecl declared_parameter = _parameters.get(i);
			GraphNode declParamNodeInMainGraph = internalToMainGraphMap.get(declared_parameter
					.getFirstNode());
			GraphNode received_parameter = parameter_values.get(i);

			received_parameter.addDependentNode(declParamNodeInMainGraph, this);
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

}
