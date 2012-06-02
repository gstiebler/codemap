package gvpl.jdt;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.*;

import gvpl.ErrorOutputter;
import gvpl.GraphBuilder;
import gvpl.Graph.GraphNode;
import gvpl.GraphBuilder.VarDecl;
import gvpl.GraphBuilder.VarId;
import gvpl.GraphBuilder.eAssignBinOp;
import gvpl.GraphBuilder.eBinOp;
import gvpl.jdt.Visitor.ASTItem;

public class AstInterpreter {

	GraphBuilder _graph_builder;
	
	private int _var_id_gen = 1;
	private Map<IBinding, VarId> _var_id_map = new HashMap<IBinding, VarId>();
	private Map<InfixExpression.Operator, eBinOp> _bin_op_types = new HashMap<InfixExpression.Operator, eBinOp>();

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
	}

	private void load_function(ASTItem node) {
		MethodDeclaration md = (MethodDeclaration) node._ast_item;
		String function_name = md.getName().toString();
		System.out.println(function_name);
		List<GraphBuilder.VarDecl> list = new ArrayList<GraphBuilder.VarDecl>();
		_graph_builder.enter_function(function_name, list);

		for (int i = 0; i < node._AST.size(); ++i) {
			ASTItem curr_node = node._AST.get(i);
			if (curr_node._ast_item instanceof Block)
				load_basic_block(curr_node);
		}

		_graph_builder.decrease_depth();
	}

	private void load_basic_block(ASTItem node) {
		Block block = (Block)node._ast_item;

		for (int i = 0; i < node._AST.size(); ++i) {
			ASTItem curr_node = node._AST.get(i);
			// if(curr_node._ast_item instanceof Block)
			load_instruction_line(curr_node);
		}
	}

	private void load_instruction_line(ASTItem node) {
		ASTNode ast_node = node._ast_item;

		System.out.println("load_instruction_line " + ast_node.toString());

		if (ast_node instanceof VariableDeclarationFragment)
			load_var_decl(node);
		else if (ast_node instanceof ExpressionStatement)
			load_value(node);
		else if (ast_node instanceof ForStatement)
			load_for_stmt(node);
		else if (ast_node instanceof Assignment)
			load_assign_bin_op_types(node);
		else if (ast_node instanceof SimpleName)
			ErrorOutputter.warning("Não sei pq cai aqui. " + ast_node.toString());
		else
			ErrorOutputter.fatalError("Node type not found!! Node: " + ast_node.toString());
	}

	private void load_var_decl(ASTItem node) {
		System.out.println("load_var_decl " + node._ast_item.toString());
		
		VarDecl var_decl = _graph_builder.new VarDecl();

		for (int i = 0; i < node._AST.size(); ++i) {
			ASTItem curr_node = node._AST.get(i);
			if (curr_node._ast_item instanceof SimpleName)
			{
				var_decl._name = curr_node._ast_item.toString();
				IBinding binding = ((Name)curr_node._ast_item).resolveBinding();
				var_decl._id = _graph_builder.new VarId(_var_id_gen++);
				_var_id_map.put(binding, var_decl._id);
			}
		}

		// var_decl._type = ; TODO pegar o tipo do parent

		_graph_builder.add_var_decl(var_decl);

		for (int i = 0; i < node._AST.size(); ++i) {
			ASTItem curr_node = node._AST.get(i);
			if (curr_node._ast_item instanceof NumberLiteral) {
				GraphNode val = load_direct_value(curr_node);
				_graph_builder.add_assign_op(var_decl._id, val);
			}
		}
	}

	GraphNode load_assign_bin_op_types(ASTItem node) {
		VarId lhs_var_id = load_lhs(node._AST.get(0));
		GraphNode rvalue = load_value(node._AST.get(1));
		
		if(true) { //TODO verificar se o assign é = e não é += 
			_graph_builder.add_assign_op(lhs_var_id, rvalue); 
			return null; 
		}
		
		GraphNode lvalue = load_value(node._AST.get(0));
		eAssignBinOp op = eAssignBinOp.E_PLUS_ASSIGN_OP; //TODO verificar qual o verdadeiro
		return _graph_builder.add_assign_bin_op(op, lhs_var_id, lvalue, rvalue);
	}
	
	private VarId load_lhs(ASTItem lhs) {
		IBinding binding = ((Name)lhs._ast_item).resolveBinding();
		return _var_id_map.get(binding);
	}

	private GraphNode load_value(ASTItem node) {
		
		System.out.println("load_value " + node._ast_item.getClass());
		
		if(node._ast_item instanceof SimpleName){
			IBinding binding = ((Name)node._ast_item).resolveBinding();
			return _graph_builder.add_var_ref(_var_id_map.get(binding));
		}		
		else if(node._ast_item instanceof InfixExpression){
			return load_bin_op(node);
		}	
		else if(node._ast_item instanceof NumberLiteral){
			return load_direct_value(node);
		}
		
		return null;
		
		/*
		  if(node->m_name == "SgCastExp")
			    node = &node->m_children[0];

			  else if(belongs_to(node->m_name, m_un_op_types))
			    return load_un_op(node);
			  else if(belongs_to(node->m_name, m_assign_bin_op_types))
			    return load_assign_bin_op_types(node);
			  else
			    cErrorOutputter::fatal_error("Node type %s not found!!", node->m_name.c_str());*/
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
