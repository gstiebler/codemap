package gvpl.cdt;

import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.DirectVarDecl;
import gvpl.graph.GraphBuilder.FuncDecl;
import gvpl.graph.GraphBuilder.FuncId;
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

public class LoadFunction extends AstLoader {
	
	protected FuncDecl _func_decl = null;
	private FuncId _funcId = null;

	public LoadFunction(GraphBuilder graph_builder, AstLoader parent, CppMaps cppMaps,
			AstInterpreter astInterpreter) {
		super(graph_builder, parent, cppMaps, astInterpreter);
	}
	
	/**
	 * Loads the member function definition
	 * 
	 * @param fd
	 *            The ast function definition
	 * @return The binding of the loaded function member
	 */
	public IBinding load(IASTFunctionDefinition fd) {
		LoadBasicBlock basicBlockLoader = new LoadBasicBlock(this, _astInterpreter);

		CPPASTFunctionDeclarator decl = (CPPASTFunctionDeclarator) fd.getDeclarator();
		IASTParameterDeclaration[] parameters = decl.getParameters();
		IASTName name_binding = decl.getName();
		String function_name = name_binding.toString();

		_funcId = _graph_builder.new FuncId();
		IBinding member_func_binding = name_binding.resolveBinding();
		_func_decl = _graph_builder.new FuncDecl(_funcId, function_name);

		loadFuncParameters(parameters, _func_decl, basicBlockLoader);

		_graph_builder.enter_function(_func_decl);

		IASTStatement body = fd.getBody();

		// TODO tratar o else
		if (body instanceof IASTCompoundStatement) {
			basicBlockLoader.load((IASTCompoundStatement) body);
		}

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
	public void loadFuncParameters(IASTParameterDeclaration[] parameters, FuncDecl func_decl,
			LoadBasicBlock basicBlockLoader) {
		for (IASTParameterDeclaration parameter : parameters) {
			IASTDeclarator parameter_var_decl = parameter.getDeclarator();
			IASTDeclSpecifier decl_spec = parameter.getDeclSpecifier();
			TypeId type = _astInterpreter.getType(decl_spec);
			DirectVarDecl var_decl = basicBlockLoader.load_var_decl(parameter_var_decl, type);
			func_decl._parameters.add(var_decl);
		}
	}

	public FuncId getFuncId() {
		return _funcId;
	}
	
	@Override
	public FuncDecl getFuncDecl() {
		return _func_decl;
	}

}
