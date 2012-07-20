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

	private String _name;
	public List<VarDecl> _parameters;

	public Function(GraphBuilder graph_builder, AstLoader parent, CppMaps cppMaps,
			AstInterpreter astInterpreter) {
		super(new GraphBuilder(), parent, cppMaps, astInterpreter);

		_name = "";
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
		BasicBlock basicBlockLoader = new BasicBlock(this, _astInterpreter);

		// The declaration of the function
		CPPASTFunctionDeclarator decl = (CPPASTFunctionDeclarator) fd.getDeclarator();
		IASTParameterDeclaration[] parameters = decl.getParameters();
		IASTName name_binding = decl.getName();
		// Gets the name of the function
		String function_name = name_binding.toString();

		IBinding member_func_binding = name_binding.resolveBinding();
		_name = function_name;

		loadFuncParameters(parameters);

		for (VarDecl parameter : _parameters) {
			GraphNode var_node = _graph_builder._gvpl_graph.add_graph_node(parameter.getName(),
					NodeType.E_DECLARED_PARAMETER);
			parameter.updateNode(var_node);
		}

		IASTStatement body = fd.getBody();

		if (body instanceof IASTCompoundStatement) {
			basicBlockLoader.load((IASTCompoundStatement) body);
		} else
			ErrorOutputter.fatalError("Work here.");

		return member_func_binding;
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
		Map<GraphNode, GraphNode> internalToMainGraphMap = graphBuilder.addGraph(_graph_builder);
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

			received_parameter._dependent_nodes.add(declParamNodeInMainGraph);
		}

		return internalToMainGraphMap.get(_return_node);
	}

	public String getName() {
		return _name;
	}

	public void setReturnNode(GraphNode returnNode) {
		_return_node = returnNode;
	}

	public Function getFunction() {
		return this;
	}

}
