package gvpl.cdt;

import gvpl.ErrorOutputter;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphNode;
import gvpl.graph.GraphBuilder.FuncDecl;
import gvpl.graph.GraphBuilder.FuncId;
import gvpl.graph.GraphBuilder.VarDecl;
import gvpl.graph.GraphBuilder.VarId;
import gvpl.graph.GraphBuilder.eAssignBinOp;
import gvpl.graph.GraphBuilder.eBinOp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;

public class AstInterpreter {

	GraphBuilder _graph_builder;

	private Map<IBinding, VarId> _var_id_map = new HashMap<IBinding, VarId>();
	private Map<IBinding, FuncId> _func_id_map = new HashMap<IBinding, FuncId>();
	private Map<Integer, eBinOp> _bin_op_types = new HashMap<Integer, eBinOp>();
	private Map<Integer, eAssignBinOp> _assign_bin_op_types = new HashMap<Integer, eAssignBinOp>();

	public AstInterpreter(GraphBuilder graph_builder, IASTTranslationUnit root) {
		_graph_builder = graph_builder;

		initialize_maps();

		IASTDeclaration[] declarations = root.getDeclarations();

		for (IASTDeclaration declaration : declarations) {
			if (declaration instanceof IASTFunctionDefinition)
				load_function((IASTFunctionDefinition) declaration);
		}
	}

	private void initialize_maps() {
		_bin_op_types.put(IASTBinaryExpression.op_plus, eBinOp.E_ADD_OP);
		_bin_op_types.put(IASTBinaryExpression.op_minus, eBinOp.E_SUB_OP);
		_bin_op_types.put(IASTBinaryExpression.op_multiply, eBinOp.E_MULT_OP);
		_bin_op_types.put(IASTBinaryExpression.op_divide, eBinOp.E_DIV_OP);
		_bin_op_types.put(IASTBinaryExpression.op_lessThan, eBinOp.E_LESS_THAN_OP);
		_bin_op_types.put(IASTBinaryExpression.op_greaterThan, eBinOp.E_GREATER_THAN_OP);

		_assign_bin_op_types.put(IASTBinaryExpression.op_assign, eAssignBinOp.E_ASSIGN_OP);
		_assign_bin_op_types.put(IASTBinaryExpression.op_plusAssign, eAssignBinOp.E_PLUS_ASSIGN_OP);
		_assign_bin_op_types.put(IASTBinaryExpression.op_minusAssign, eAssignBinOp.E_SUB_ASSIGN_OP);
	}

	private void load_function(IASTFunctionDefinition fd) {
		CPPASTFunctionDeclarator decl = (CPPASTFunctionDeclarator) fd.getDeclarator();
		IASTParameterDeclaration[] parameters = decl.getParameters();
		IASTName name_binding = decl.getName();
		String function_name = name_binding.toString();
		
		FuncId func_id = _graph_builder.new FuncId();
		_func_id_map.put(name_binding.resolveBinding(), func_id);
		FuncDecl func_decl = _graph_builder.new FuncDecl(func_id, function_name);
		for (IASTParameterDeclaration parameter : parameters) {
			IASTDeclarator parameter_var_decl = parameter.getDeclarator();
			VarDecl var_decl = load_var_decl(parameter_var_decl);
			func_decl._parameters.add(var_decl);
		}
		_graph_builder.enter_function(func_decl);

		IASTStatement body = fd.getBody();

		// TODO tratar o else
		if (body instanceof IASTCompoundStatement)
			load_basic_block((IASTCompoundStatement) body);

		_graph_builder.decrease_depth();
	}

	private void load_basic_block(IASTCompoundStatement cs) {
		// Block block = (Block)node._ast_item;
		IASTStatement[] statements = cs.getStatements();

		for (IASTStatement statement : statements)
			load_instruction_line(statement);
	}

	private void load_instruction_line(IASTStatement statement) {
		if (statement instanceof IASTDeclarationStatement) {//variable declaration
			IASTDeclarationStatement decl_statement = (IASTDeclarationStatement) statement;
			IASTDeclaration decl = decl_statement.getDeclaration();
			if (!(decl instanceof IASTSimpleDeclaration))
				ErrorOutputter.fatalError("Deu merda aqui.");

			IASTSimpleDeclaration simple_decl = (IASTSimpleDeclaration) decl;
			IASTDeclarator[] declarators = simple_decl.getDeclarators();
			for (IASTDeclarator declarator : declarators)//possibly more than one variable per line
				load_var_decl(declarator);
		} else if (statement instanceof IASTExpression)
			load_value((IASTExpression) statement);
		else if (statement instanceof IASTForStatement)
			load_for_stmt((IASTForStatement) statement);
		else if (statement instanceof IASTExpressionStatement) {
			IASTExpressionStatement expr_stat = (IASTExpressionStatement) statement;
			IASTExpression expr = expr_stat.getExpression();
			if(expr instanceof IASTBinaryExpression)
				load_assign_bin_op_types((IASTBinaryExpression) expr);
			else if (expr instanceof IASTFunctionCallExpression)
				loadFunctionCall((IASTFunctionCallExpression) expr);
		} else if (statement instanceof IASTReturnStatement){
			loadReturnStatement((IASTReturnStatement) statement);
		} else
			ErrorOutputter.fatalError("Node type not found!! Node: " + statement.toString());
	}
	
	private void loadReturnStatement(IASTReturnStatement statement){
		IASTReturnStatement return_node = (IASTReturnStatement) statement;

		GraphNode rvalue = load_value(return_node.getReturnValue());
		_graph_builder.addReturnStatement(rvalue);
	}

	private VarDecl load_var_decl(IASTDeclarator decl) {
		IASTName name = decl.getName();
		IBinding binding = name.resolveBinding();

		VarId id = _graph_builder.new VarId();
		VarDecl var_decl = _graph_builder.new VarDecl(id, name.toString());
		_var_id_map.put(binding, id);

		// var_decl._type = ; TODO pegar o tipo do parent

		_graph_builder.add_var_decl(var_decl);

		IASTInitializerExpression init_exp = (IASTInitializerExpression) decl.getInitializer();

		if (init_exp == null)
			return var_decl;

		GraphNode val = load_value(init_exp.getExpression());
		_graph_builder.add_assign_op(var_decl.getVarId(), val);

		return var_decl;
	}

	GraphNode load_assign_bin_op_types(IASTBinaryExpression node) {

		IASTIdExpression id_expr = (IASTIdExpression) node.getOperand1();
		VarId lhs_var_id = load_lhs(id_expr.getName());
		GraphNode rvalue = load_value(node.getOperand2());

		if (node.getOperator() == IASTBinaryExpression.op_assign) {
			_graph_builder.add_assign_op(lhs_var_id, rvalue);
			return null;
		}

		GraphNode lvalue = load_value(node.getOperand1());
		eAssignBinOp op = _assign_bin_op_types.get(node.getOperator());
		return _graph_builder.add_assign_bin_op(op, lhs_var_id, lvalue, rvalue);
	}

	private VarId load_lhs(IASTName lhs) {
		IBinding binding = lhs.resolveBinding();
		return _var_id_map.get(binding);
	}

	/*
	 * @brief Alguma coisa que retorna um valor
	 */
	private GraphNode load_value(IASTExpression node) {

		// Eh uma variavel
		if (node instanceof IASTIdExpression) {
			IBinding binding = ((IASTIdExpression) node).getName().resolveBinding();
			return _graph_builder.add_var_ref(_var_id_map.get(binding));
		} else if (node instanceof IASTBinaryExpression) {// Eh uma expressao
			return load_bin_op((IASTBinaryExpression) node);
		} else if (node instanceof IASTLiteralExpression) {// Eh um valor direto
			return load_direct_value((IASTLiteralExpression) node);
		} else if (node instanceof IASTFunctionCallExpression) {// Eh uma chamada a funcao
			return loadFunctionCall((IASTFunctionCallExpression) node);
		}  else
			ErrorOutputter.fatalError("Node type not found!! Node: " + node.getClass());
		
		return null;
	}
	
	GraphNode loadFunctionCall(IASTFunctionCallExpression func_call){
		IASTIdExpression expr = (IASTIdExpression) func_call.getFunctionNameExpression();
		FuncId func_id = _func_id_map.get(expr.getName().resolveBinding());
		IASTExpressionList expr_list = (IASTExpressionList) func_call.getParameterExpression();
		IASTExpression[] parameters = expr_list.getExpressions();
		
		List<GraphNode> parameter_values = new ArrayList<GraphNode>();
		for (IASTExpression parameter : parameters)
			parameter_values.add(load_value(parameter));
		
		return _graph_builder.addFuncRef(func_id, parameter_values);
	}

	GraphNode load_bin_op(IASTBinaryExpression bin_op) {
		eBinOp op = _bin_op_types.get(bin_op.getOperator());
		GraphNode lvalue = load_value(bin_op.getOperand1());
		GraphNode rvalue = load_value(bin_op.getOperand2());
		return _graph_builder.add_bin_op(op, lvalue, rvalue);
	}

	private void load_for_stmt(IASTForStatement node) {

	}

	GraphNode load_direct_value(IASTLiteralExpression node) {
		String value = node.toString();
		return _graph_builder.add_direct_val(GraphBuilder.eValueType.E_INVALID_TYPE, value);
	}

}
