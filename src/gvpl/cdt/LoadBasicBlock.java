package gvpl.cdt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

import gvpl.ErrorOutputter;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphNode;
import gvpl.graph.GraphBuilder.DirectVarDecl;
import gvpl.graph.GraphBuilder.FuncId;
import gvpl.graph.GraphBuilder.TypeId;
import gvpl.graph.GraphBuilder.VarDecl;
import gvpl.graph.GraphBuilder.VarId;
import gvpl.graph.GraphBuilder.eAssignBinOp;
import gvpl.graph.GraphBuilder.eBinOp;

public class LoadBasicBlock {

	private GraphBuilder _graph_builder;
	private AstLoader _parent;
	private CppMaps _cppMaps;

	public LoadBasicBlock(GraphBuilder graph_builder, AstLoader parent, CppMaps cppMaps) {
		_graph_builder = graph_builder;
		_parent = parent;
		_cppMaps = cppMaps;
	}
	
	public void load(IASTCompoundStatement cs) {
		IASTStatement[] statements = cs.getStatements();

		for (IASTStatement statement : statements)
			load_instruction_line(statement);
	}
	
	public DirectVarDecl load_var_decl(IASTDeclarator decl, TypeId type) {
		IASTName name = decl.getName();

		VarId id = _graph_builder.new VarId();
		DirectVarDecl var_decl = _graph_builder.new DirectVarDecl(id, name.toString(), type);
		_parent.addVarDecl(name.resolveBinding(), id);
		_graph_builder.add_var_decl(var_decl);

		IASTInitializerExpression init_exp = (IASTInitializerExpression) decl.getInitializer();

		if (init_exp == null)
			return var_decl;

		GraphNode val = load_value(init_exp.getExpression());
		_graph_builder.add_assign_op(var_decl, val);

		return var_decl;
	}

	private void load_instruction_line(IASTStatement statement) {
		if (statement instanceof IASTDeclarationStatement) {// variable
															// declaration
			IASTDeclarationStatement decl_statement = (IASTDeclarationStatement) statement;
			IASTDeclaration decl = decl_statement.getDeclaration();
			if (!(decl instanceof IASTSimpleDeclaration))
				ErrorOutputter.fatalError("Deu merda aqui.");

			IASTSimpleDeclaration simple_decl = (IASTSimpleDeclaration) decl;
			IASTDeclSpecifier decl_spec = simple_decl.getDeclSpecifier();

			TypeId type = _parent.getType(decl_spec);

			IASTDeclarator[] declarators = simple_decl.getDeclarators();
			for (IASTDeclarator declarator : declarators)
				// possibly more than one variable per line
				load_var_decl(declarator, type);
		} else if (statement instanceof IASTExpression)
			load_value((IASTExpression) statement);
		else if (statement instanceof IASTForStatement)
			load_for_stmt((IASTForStatement) statement);
		else if (statement instanceof IASTExpressionStatement) {
			IASTExpressionStatement expr_stat = (IASTExpressionStatement) statement;
			IASTExpression expr = expr_stat.getExpression();
			if (expr instanceof IASTBinaryExpression)
				load_assign_bin_op_types((IASTBinaryExpression) expr);
			else if (expr instanceof IASTFunctionCallExpression)
				loadFunctionCall((IASTFunctionCallExpression) expr);
		} else if (statement instanceof IASTReturnStatement) {
			loadReturnStatement((IASTReturnStatement) statement);
		} else
			ErrorOutputter.fatalError("Node type not found!! Node: " + statement.toString());
	}

	/*
	 * @brief Alguma coisa que retorna um valor
	 */
	private GraphNode load_value(IASTExpression node) {

		// Eh uma variavel
		if (node instanceof IASTIdExpression) {
			VarDecl var_decl = _parent.getVarDecl(node);
			return _graph_builder.add_var_ref(var_decl);
		} else if (node instanceof IASTBinaryExpression) {// Eh uma expressao
			return load_bin_op((IASTBinaryExpression) node);
		} else if (node instanceof IASTLiteralExpression) {// Eh um valor direto
			return load_direct_value((IASTLiteralExpression) node);
		} else if (node instanceof IASTFunctionCallExpression) {// Eh umachamada
																// a funcao
			return loadFunctionCall((IASTFunctionCallExpression) node);
		} else if (node instanceof IASTFieldReference) {// reference to field of
														// a struct
			VarDecl var_decl = _parent.getVarDecl(node);
			return _graph_builder.add_var_ref(var_decl);
		} else
			ErrorOutputter.fatalError("Node type not found!! Node: " + node.getClass());

		return null;
	}

	private void load_for_stmt(IASTForStatement node) {

	}

	private void loadReturnStatement(IASTReturnStatement statement) {
		IASTReturnStatement return_node = (IASTReturnStatement) statement;

		GraphNode rvalue = load_value(return_node.getReturnValue());
		// TODO set the correct type of the return value
		_graph_builder.addReturnStatement(rvalue, null);
	}

	GraphNode load_assign_bin_op_types(IASTBinaryExpression node) {
		IASTExpression expr = node.getOperand1();
		VarDecl var_decl = _parent.getVarDecl(expr);

		GraphNode rvalue = load_value(node.getOperand2());

		if (node.getOperator() == IASTBinaryExpression.op_assign) {
			_graph_builder.add_assign_op(var_decl, rvalue);
			return null;
		}

		GraphNode lvalue = load_value(node.getOperand1());
		eAssignBinOp op = _cppMaps.getAssignBinOpTypes(node.getOperator());
		return _graph_builder.add_assign_bin_op(op, var_decl, lvalue, rvalue);
	}

	GraphNode loadFunctionCall(IASTFunctionCallExpression func_call) {
		IASTIdExpression expr = (IASTIdExpression) func_call.getFunctionNameExpression();
		FuncId func_id = _parent.getFuncId(expr.getName().resolveBinding());
		IASTExpressionList expr_list = (IASTExpressionList) func_call.getParameterExpression();
		IASTExpression[] parameters = expr_list.getExpressions();

		List<GraphNode> parameter_values = new ArrayList<GraphNode>();
		for (IASTExpression parameter : parameters)
			parameter_values.add(load_value(parameter));

		return _graph_builder.addFuncRef(func_id, parameter_values);
	}

	GraphNode load_bin_op(IASTBinaryExpression bin_op) {
		eBinOp op = _cppMaps.getBinOpType(bin_op.getOperator());
		GraphNode lvalue = load_value(bin_op.getOperand1());
		GraphNode rvalue = load_value(bin_op.getOperand2());
		return _graph_builder.add_bin_op(op, lvalue, rvalue);
	}

	GraphNode load_direct_value(IASTLiteralExpression node) {
		String value = node.toString();
		return _graph_builder.add_direct_val(GraphBuilder.eValueType.E_INVALID_TYPE, value);
	}
}
