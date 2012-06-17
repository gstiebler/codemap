package gvpl.cdt;

import gvpl.ErrorOutputter;
import gvpl.Graph.GraphNode;
import gvpl.GraphBuilder;
import gvpl.GraphBuilder.VarDecl;
import gvpl.GraphBuilder.VarId;
import gvpl.GraphBuilder.eAssignBinOp;
import gvpl.GraphBuilder.eBinOp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class AstInterpreter {

	GraphBuilder _graph_builder;
	
	private int _var_id_gen = 1;
	private Map<IBinding, VarId> _var_id_map = new HashMap<IBinding, VarId>();
	private Map<Integer, eBinOp> _bin_op_types = new HashMap<Integer, eBinOp>();
	private Map<Integer, eAssignBinOp> _assign_bin_op_types = 
				new HashMap<Integer, eAssignBinOp>();

	public AstInterpreter(GraphBuilder graph_builder, IASTTranslationUnit root) {
		_graph_builder = graph_builder;
		
		initialize_maps();
		
		IASTDeclaration[] declarations = root.getDeclarations();
		
		for (int i = 0; i < declarations.length; ++i)
		{
			if(declarations[i] instanceof IASTFunctionDefinition)
				load_function((IASTFunctionDefinition) declarations[i]);
		}
	}
	
	private void initialize_maps(){
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
		String function_name = fd.getDeclarator().getName().toString();
		List<GraphBuilder.VarDecl> list = new ArrayList<GraphBuilder.VarDecl>();
		_graph_builder.enter_function(function_name, list);
		
		IASTStatement body = fd.getBody();
		
		//TODO tratar o else
		if(body instanceof IASTCompoundStatement)
			load_basic_block((IASTCompoundStatement) body);

		_graph_builder.decrease_depth();
	}

	private void load_basic_block(IASTCompoundStatement cs) {
		//Block block = (Block)node._ast_item;
		IASTStatement[] statements = cs.getStatements();
		
		for (int i = 0; i < statements.length; ++i)
			load_instruction_line(statements[i]);
	}

	private void load_instruction_line(IASTStatement statement) {
		if (statement instanceof IASTDeclarator)
			load_var_decl((IASTDeclarator) statement);
		else if (statement instanceof IASTExpression)
			load_value((IASTExpression) statement);
		else if (statement instanceof IASTForStatement)
			load_for_stmt((IASTForStatement) statement);
		else if (statement instanceof IASTBinaryExpression)
			load_assign_bin_op_types((IASTBinaryExpression) statement);
		else
			ErrorOutputter.fatalError("Node type not found!! Node: " + statement.toString());
	}

	private void load_var_decl(IASTDeclarator decl) {
		VarDecl var_decl = _graph_builder.new VarDecl();
		
		IASTName name = decl.getName();
		var_decl._name = name.toString();
		IBinding binding = name.resolveBinding();
		var_decl._id = _graph_builder.new VarId(_var_id_gen++);
		_var_id_map.put(binding, var_decl._id);

		// var_decl._type = ; TODO pegar o tipo do parent

		_graph_builder.add_var_decl(var_decl);
		
		IASTInitializerExpression init_exp = (IASTInitializerExpression) decl.getInitializer();
		
		if(init_exp == null)
			return;
		
		//TODO resolver para quando a inicialização não for um literal, pode ser uma expressão
		//O melhor é chamar load_value
		GraphNode val = load_direct_value((IASTLiteralExpression) init_exp.getExpression());
		_graph_builder.add_assign_op(var_decl._id, val);
	}

	GraphNode load_assign_bin_op_types(IASTBinaryExpression node) {
		
		VarId lhs_var_id = load_lhs((IASTName) node.getOperand1());
		GraphNode rvalue = load_value(node.getOperand2());
		
		if(node.getOperator() == IASTBinaryExpression.op_assign) {
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

	private GraphNode load_value(IASTExpression node) {
		
		if(node instanceof IASTName){
			IBinding binding = ((IASTName)node).resolveBinding();
			return _graph_builder.add_var_ref(_var_id_map.get(binding));
		}		
		else if(node instanceof IASTBinaryExpression){
			return load_bin_op((IASTBinaryExpression) node);
		}	
		else if(node instanceof IASTLiteralExpression){
			return load_direct_value((IASTLiteralExpression) node);
		}
		
		return null;
	}
	
	GraphNode load_bin_op(IASTBinaryExpression bin_op)
	{
		eBinOp op = _bin_op_types.get(bin_op.getOperator()); 
		GraphNode lvalue = load_value(bin_op.getOperand1());
		GraphNode rvalue = load_value(bin_op.getOperand1());
		return _graph_builder.add_bin_op(op, lvalue, rvalue);
	}

	private void load_for_stmt(IASTForStatement node) {

	}

	GraphNode load_direct_value(IASTLiteralExpression node) {
		String value = node.toString();
		return _graph_builder.add_direct_val(GraphBuilder.eValueType.E_INVALID_TYPE, value);
	}

}
