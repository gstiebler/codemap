package gvpl.cdt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gvpl.ErrorOutputter;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.*;
import gvpl.graph.GraphNode;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;

public class AstInterpreter {

	GraphBuilder _graph_builder;

	private Map<IBinding, VarId> _var_id_map = new HashMap<IBinding, VarId>();
	private Map<IBinding, FuncId> _func_id_map = new HashMap<IBinding, FuncId>();
	private Map<IBinding, TypeId> _type_id_map = new HashMap<IBinding, TypeId>();
	private Map<IBinding, MemberId> _member_id_map = new HashMap<IBinding, MemberId>();
	private Map<Integer, eBinOp> _bin_op_types = new HashMap<Integer, eBinOp>();
	private Map<Integer, eAssignBinOp> _assign_bin_op_types = new HashMap<Integer, eAssignBinOp>();

	public AstInterpreter(GraphBuilder graph_builder, IASTTranslationUnit root) {
		_graph_builder = graph_builder;

		initialize_maps();

		IASTDeclaration[] declarations = root.getDeclarations();

		for (IASTDeclaration declaration : declarations) {
			if (declaration instanceof IASTFunctionDefinition)
				load_function((IASTFunctionDefinition) declaration);
			else if (declaration instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simple_decl = (IASTSimpleDeclaration) declaration;
				loadStructureDecl((IASTCompositeTypeSpecifier) simple_decl.getDeclSpecifier());
			} else
				ErrorOutputter.fatalError("Deu merda aqui.");

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

	private void loadStructureDecl(IASTCompositeTypeSpecifier strDecl) {
		IASTName name = strDecl.getName();
		IASTDeclaration[] members = strDecl.getMembers();

		TypeId struct_type = _graph_builder.new TypeId();
		_type_id_map.put(name.resolveBinding(), struct_type);

		StructDecl struct_decl = _graph_builder.new StructDecl(struct_type, name.toString());

		// for each line of members declaration
		for (IASTDeclaration member : members) {
			IASTSimpleDeclaration simple_decl = (IASTSimpleDeclaration) member;
			IASTDeclSpecifier decl_spec = simple_decl.getDeclSpecifier();
			TypeId param_type = getType(decl_spec);
			IASTDeclarator[] declarators = simple_decl.getDeclarators();
			// for each variable declared in a line
			for (IASTDeclarator declarator : declarators) {
				IASTName decl_name = declarator.getName();
				MemberId member_id = _graph_builder.new MemberId();

				StructMember struct_member = _graph_builder.new StructMember(struct_decl, member_id,
						decl_name.toString(), param_type);
				struct_decl.addMember(struct_member);
				
				_member_id_map.put(decl_name.resolveBinding(), member_id);
			}
		}

		_graph_builder.addStructDecl(struct_decl);
	}

	TypeId getType(IASTDeclSpecifier decl_spec) {
		if (decl_spec instanceof IASTNamedTypeSpecifier) {
			IASTNamedTypeSpecifier named_type = (IASTNamedTypeSpecifier) decl_spec;
			return _type_id_map.get(named_type.getName().resolveBinding());
		}

		return null;
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
			IASTDeclSpecifier decl_spec = parameter.getDeclSpecifier();
			TypeId type = getType(decl_spec);
			DirectVarDecl var_decl = load_var_decl(parameter_var_decl, type);
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
		if (statement instanceof IASTDeclarationStatement) {// variable
															// declaration
			IASTDeclarationStatement decl_statement = (IASTDeclarationStatement) statement;
			IASTDeclaration decl = decl_statement.getDeclaration();
			if (!(decl instanceof IASTSimpleDeclaration))
				ErrorOutputter.fatalError("Deu merda aqui.");

			IASTSimpleDeclaration simple_decl = (IASTSimpleDeclaration) decl;
			IASTDeclSpecifier decl_spec = simple_decl.getDeclSpecifier();

			TypeId type = getType(decl_spec);

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

	private void loadReturnStatement(IASTReturnStatement statement) {
		IASTReturnStatement return_node = (IASTReturnStatement) statement;

		GraphNode rvalue = load_value(return_node.getReturnValue());
		// TODO set the correct type of the return value
		_graph_builder.addReturnStatement(rvalue, null);
	}

	private DirectVarDecl load_var_decl(IASTDeclarator decl, TypeId type) {
		IASTName name = decl.getName();

		VarId id = _graph_builder.new VarId();
		DirectVarDecl var_decl = _graph_builder.new DirectVarDecl(id, name.toString(), type);
		_var_id_map.put(name.resolveBinding(), id);
		_graph_builder.add_var_decl(var_decl);

		IASTInitializerExpression init_exp = (IASTInitializerExpression) decl.getInitializer();

		if (init_exp == null)
			return var_decl;

		GraphNode val = load_value(init_exp.getExpression());
		_graph_builder.add_assign_op(var_decl, val);

		return var_decl;
	}

	GraphNode load_assign_bin_op_types(IASTBinaryExpression node) {
		IASTExpression expr = node.getOperand1();
		VarDecl var_decl = getVarDecl(expr);

		GraphNode rvalue = load_value(node.getOperand2());

		if (node.getOperator() == IASTBinaryExpression.op_assign) {
			_graph_builder.add_assign_op(var_decl, rvalue);
			return null;
		}

		GraphNode lvalue = load_value(node.getOperand1());
		eAssignBinOp op = _assign_bin_op_types.get(node.getOperator());
		return _graph_builder.add_assign_bin_op(op, var_decl, lvalue, rvalue);
	}

	private VarDecl getVarDecl(IASTExpression expr) {
		if (expr instanceof IASTIdExpression) {
			IASTIdExpression id_expr = (IASTIdExpression) expr;
			IBinding binding = id_expr.getName().resolveBinding();
			VarId lhs_var_id = _var_id_map.get(binding);

			return _graph_builder.find_var(lhs_var_id);
		} else if (expr instanceof IASTFieldReference) {
			CPPASTFieldReference field_ref = (CPPASTFieldReference) expr;
			IASTIdExpression owner = (IASTIdExpression) field_ref.getFieldOwner();

			IBinding binding = owner.getName().resolveBinding();
			MemberId member_id = _member_id_map.get(binding);

			VarId var_id = _var_id_map.get(binding);
			
			return _graph_builder.findMember(var_id, member_id);
		} else
			ErrorOutputter.fatalError("Work here " + expr.getClass());

		return null;
	}

	/*
	 * @brief Alguma coisa que retorna um valor
	 */
	private GraphNode load_value(IASTExpression node) {

		// Eh uma variavel
		if (node instanceof IASTIdExpression) {
			VarDecl var_decl = getVarDecl(node);
			return _graph_builder.add_var_ref(var_decl);
		} else if (node instanceof IASTBinaryExpression) {// Eh uma expressao
			return load_bin_op((IASTBinaryExpression) node);
		} else if (node instanceof IASTLiteralExpression) {// Eh um valor direto
			return load_direct_value((IASTLiteralExpression) node);
		} else if (node instanceof IASTFunctionCallExpression) {// Eh uma
																// chamada a
																// funcao
			return loadFunctionCall((IASTFunctionCallExpression) node);
		} else
			ErrorOutputter.fatalError("Node type not found!! Node: " + node.getClass());

		return null;
	}

	GraphNode loadFunctionCall(IASTFunctionCallExpression func_call) {
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
