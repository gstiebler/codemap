package gvpl.cdt;

import gvpl.ErrorOutputter;
import gvpl.Graph.GraphNode;
import gvpl.GraphBuilder;
import gvpl.GraphBuilder.VarDecl;
import gvpl.GraphBuilder.VarId;
import gvpl.GraphBuilder.eAssignBinOp;
import gvpl.GraphBuilder.eBinOp;
import gvpl.cdt.Visitor.ASTItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class AstInterpreter {

	GraphBuilder _graph_builder;
	
	private int _var_id_gen = 1;
	private Map<IBinding, VarId> _var_id_map = new HashMap<IBinding, VarId>();
	private Map<Integer, eBinOp> _bin_op_types = new HashMap<Integer, eBinOp>();
	private Map<Integer, eAssignBinOp> _assign_bin_op_types = 
				new HashMap<Integer, eAssignBinOp>();

	public AstInterpreter(GraphBuilder graph_builder, ASTItem root) {
		_graph_builder = graph_builder;
		
		initialize_maps();

		for (int i = 0; i < root._AST.size(); ++i) {
			ASTItem node = root._AST.get(i);
			if (node._ast_item instanceof IASTFunctionDefinition)
				load_function(node);
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

	private void load_function(ASTItem node) {
		IASTFunctionDefinition md = (IASTFunctionDefinition) node._ast_item;
		String function_name = md.getRawSignature();
		List<GraphBuilder.VarDecl> list = new ArrayList<GraphBuilder.VarDecl>();
		_graph_builder.enter_function(function_name, list);

		for (int i = 0; i < node._AST.size(); ++i) {
			ASTItem curr_node = node._AST.get(i);
			if (curr_node._ast_item instanceof IASTCompoundStatement)
				load_basic_block(curr_node);
		}

		_graph_builder.decrease_depth();
	}

	private void load_basic_block(ASTItem node) {
		//Block block = (Block)node._ast_item;

		for (int i = 0; i < node._AST.size(); ++i) {
			ASTItem curr_node = node._AST.get(i);
			// if(curr_node._ast_item instanceof Block)
			load_instruction_line(curr_node);
		}
	}

	private void load_instruction_line(ASTItem node) {
		IASTNode ast_node = node._ast_item;

		if (ast_node instanceof IASTDeclarator)
			load_var_decl(node);
		else if (ast_node instanceof IASTExpressionStatement)
			load_value(node);
		else if (ast_node instanceof IASTForStatement)
			load_for_stmt(node);
		else if (ast_node instanceof IASTBinaryExpression)
			load_assign_bin_op_types(node);
		else
			ErrorOutputter.fatalError("Node type not found!! Node: " + ast_node.toString());
	}

	private void load_var_decl(ASTItem node) {
		System.out.println("load_var_decl " + node._ast_item.toString());
		
		VarDecl var_decl = _graph_builder.new VarDecl();

		for (int i = 0; i < node._AST.size(); ++i) {
			ASTItem curr_node = node._AST.get(i);
			if (curr_node._ast_item instanceof IASTName)
			{
				var_decl._name = curr_node._ast_item.toString();
				IBinding binding = ((IASTName)curr_node._ast_item).resolveBinding();
				var_decl._id = _graph_builder.new VarId(_var_id_gen++);
				_var_id_map.put(binding, var_decl._id);
			}
		}

		// var_decl._type = ; TODO pegar o tipo do parent

		_graph_builder.add_var_decl(var_decl);

		for (int i = 0; i < node._AST.size(); ++i) {
			ASTItem curr_node = node._AST.get(i);
			if (curr_node._ast_item instanceof IASTLiteralExpression) {
				GraphNode val = load_direct_value(curr_node);
				_graph_builder.add_assign_op(var_decl._id, val);
			}
		}
	}

	GraphNode load_assign_bin_op_types(ASTItem node) {
	    IASTBinaryExpression assignment = (IASTBinaryExpression)node._ast_item;
		
		VarId lhs_var_id = load_lhs(node._AST.get(0));
		GraphNode rvalue = load_value(node._AST.get(1));
		
		if(assignment.getOperator() == IASTBinaryExpression.op_assign) {
			_graph_builder.add_assign_op(lhs_var_id, rvalue); 
			return null; 
		}
		
		GraphNode lvalue = load_value(node._AST.get(0));
		eAssignBinOp op = _assign_bin_op_types.get(assignment.getOperator());
		return _graph_builder.add_assign_bin_op(op, lhs_var_id, lvalue, rvalue);
	}
	
	private VarId load_lhs(ASTItem lhs) {
		IBinding binding = ((IASTName)lhs._ast_item).resolveBinding();
		return _var_id_map.get(binding);
	}

	private GraphNode load_value(ASTItem node) {
		
		if(node._ast_item instanceof IASTName){
			IBinding binding = ((IASTName)node._ast_item).resolveBinding();
			return _graph_builder.add_var_ref(_var_id_map.get(binding));
		}		
		else if(node._ast_item instanceof IASTBinaryExpression){
			return load_bin_op(node);
		}	
		else if(node._ast_item instanceof IASTLiteralExpression){
			return load_direct_value(node);
		}
		
		return null;
	}
	
	GraphNode load_bin_op(ASTItem node)
	{
		IASTBinaryExpression infex = (IASTBinaryExpression)node._ast_item;
		eBinOp op = _bin_op_types.get(infex.getOperator()); 
		GraphNode lvalue = load_value(node._AST.get(0));
		GraphNode rvalue = load_value(node._AST.get(1));
		return _graph_builder.add_bin_op(op, lvalue, rvalue);
	}

	private void load_for_stmt(ASTItem node) {

	}

	GraphNode load_direct_value(ASTItem node) {
		String value = node._ast_item.toString();
		return _graph_builder.add_direct_val(GraphBuilder.eValueType.E_INVALID_TYPE, value);
	}

}
