package gvpl.jdt;

import gvpl.ErrorOutputter;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphNode;
import gvpl.graph.GraphBuilder.FuncDecl;
import gvpl.graph.GraphBuilder.FuncId;
import gvpl.graph.GraphBuilder.DirectVarDecl;
import gvpl.graph.GraphBuilder.VarId;
import gvpl.graph.GraphBuilder.eAssignBinOp;
import gvpl.graph.GraphBuilder.eBinOp;
import gvpl.jdt.Visitor.ASTItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class AstInterpreter {

	GraphBuilder _graph_builder;
	
	private Map<IBinding, VarId> _var_id_map = new HashMap<IBinding, VarId>();
	private Map<InfixExpression.Operator, eBinOp> _bin_op_types = new HashMap<InfixExpression.Operator, eBinOp>();
	private Map<Assignment.Operator, eAssignBinOp> _assign_bin_op_types = 
				new HashMap<Assignment.Operator, eAssignBinOp>();

	public AstInterpreter(GraphBuilder graph_builder, ASTItem root) {
		_graph_builder = graph_builder;
		
		initialize_maps();

		for (int i = 0; i < root._AST.size(); ++i) {
			ASTItem node = root._AST.get(i);
			if (node._ast_item instanceof MethodDeclaration)
				load_function(node);
		}
	}
	
	private void initialize_maps(){
		_bin_op_types.put(InfixExpression.Operator.PLUS, eBinOp.E_ADD_OP);
		_bin_op_types.put(InfixExpression.Operator.MINUS, eBinOp.E_SUB_OP);
		_bin_op_types.put(InfixExpression.Operator.TIMES, eBinOp.E_MULT_OP);
		_bin_op_types.put(InfixExpression.Operator.DIVIDE, eBinOp.E_DIV_OP);
		_bin_op_types.put(InfixExpression.Operator.LESS, eBinOp.E_LESS_THAN_OP);
		_bin_op_types.put(InfixExpression.Operator.GREATER, eBinOp.E_GREATER_THAN_OP);
		
		_assign_bin_op_types.put(Assignment.Operator.ASSIGN, eAssignBinOp.E_ASSIGN_OP);
		_assign_bin_op_types.put(Assignment.Operator.PLUS_ASSIGN, eAssignBinOp.E_PLUS_ASSIGN_OP);
		_assign_bin_op_types.put(Assignment.Operator.MINUS_ASSIGN, eAssignBinOp.E_SUB_ASSIGN_OP);
	}

	private void load_function(ASTItem node) {
		MethodDeclaration md = (MethodDeclaration) node._ast_item;
		String function_name = md.getName().toString();

		//TODO pegar o ID certo
		FuncId func_id = null;
		FuncDecl func_decl = _graph_builder.new FuncDecl(func_id, function_name);
		_graph_builder.enter_function(func_decl);

		for (int i = 0; i < node._AST.size(); ++i) {
			ASTItem curr_node = node._AST.get(i);
			if (curr_node._ast_item instanceof Block)
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
		ASTNode ast_node = node._ast_item;

		if (ast_node instanceof VariableDeclarationFragment)
			load_var_decl(node);
		else if (ast_node instanceof ExpressionStatement)
			load_value(node);
		else if (ast_node instanceof ForStatement)
			load_for_stmt(node);
		else if (ast_node instanceof Assignment)
			load_assign_bin_op_types(node);
		else
			ErrorOutputter.fatalError("Node type not found!! Node: " + ast_node.toString());
	}

	private void load_var_decl(ASTItem node) {
		System.out.println("load_var_decl " + node._ast_item.toString());

		DirectVarDecl curr_var_decl = null;
		
		for (int i = 0; i < node._AST.size(); ++i) {
			ASTItem curr_node = node._AST.get(i);
			if (curr_node._ast_item instanceof SimpleName)
			{
				IBinding binding = ((Name)curr_node._ast_item).resolveBinding();
				VarId id = _graph_builder.new VarId();
				
				//TODO set the correct type, not always null
				DirectVarDecl var_decl = _graph_builder.new DirectVarDecl(id, curr_node._ast_item.toString(), null);
				_var_id_map.put(binding, id);
				curr_var_decl = var_decl;
			}
		}

		// var_decl._type = ; TODO pegar o tipo do parent

		_graph_builder.add_var_decl(curr_var_decl);

		for (int i = 0; i < node._AST.size(); ++i) {
			ASTItem curr_node = node._AST.get(i);
			if (curr_node._ast_item instanceof NumberLiteral || curr_node._ast_item instanceof BooleanLiteral) {
				GraphNode val = load_direct_value(curr_node);
				//TODO corrigir
				//_graph_builder.add_assign_op(curr_var_decl.getVarId(), val);
			}
		}
	}

	GraphNode load_assign_bin_op_types(ASTItem node) {
		Assignment assignment = (Assignment)node._ast_item;
		
		VarId lhs_var_id = load_lhs(node._AST.get(0));
		GraphNode rvalue = load_value(node._AST.get(1));
		
		if(assignment.getOperator() == Assignment.Operator.ASSIGN) {
			//TODO corrigir
			//_graph_builder.add_assign_op(lhs_var_id, rvalue); 
			return null; 
		}
		
		GraphNode lvalue = load_value(node._AST.get(0));
		eAssignBinOp op = _assign_bin_op_types.get(assignment.getOperator());

		//TODO corrigir
		//return _graph_builder.add_assign_bin_op(op, lhs_var_id, lvalue, rvalue);
		return null;
	}
	
	private VarId load_lhs(ASTItem lhs) {
		IBinding binding = ((Name)lhs._ast_item).resolveBinding();
		return _var_id_map.get(binding);
	}

	private GraphNode load_value(ASTItem node) {
		
		if(node._ast_item instanceof SimpleName){
			IBinding binding = ((Name)node._ast_item).resolveBinding();
			//TODO corrigir
			//return _graph_builder.add_var_ref(_var_id_map.get(binding));
			return null;
		}		
		else if(node._ast_item instanceof InfixExpression){
			return load_bin_op(node);
		}	
		else if(node._ast_item instanceof NumberLiteral){
			return load_direct_value(node);
		}
		
		return null;
	}
	
	GraphNode load_bin_op(ASTItem node)
	{
		InfixExpression infex = (InfixExpression)node._ast_item;
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
