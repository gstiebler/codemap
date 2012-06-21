package gvpl.graph;

import gvpl.ErrorOutputter;
import gvpl.graph.Graph.NodeType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphBuilder {

	enum eUnOp {
		E_INVALID_UN_OP,
		E_PLUS_PLUS_OP
	};

	public enum eBinOp {
		E_INVALID_BIN_OP,
		E_ADD_OP,
		E_SUB_OP,
		E_MULT_OP,
		E_DIV_OP,
		E_LESS_THAN_OP,
		E_GREATER_THAN_OP
	}

	public enum eAssignBinOp {
		E_INVALID_A_BIN_OP,
		E_ASSIGN_OP,
		E_PLUS_ASSIGN_OP,
		E_SUB_ASSIGN_OP
	}

	public enum eValueType {
		E_INVALID_TYPE,
		E_INT,
		E_FLOAT,
		E_DOUBLE,
		E_STRING,
		E_BOOL
	}

	enum eForLoopState {
		E_INVALID_LOOP_STATE,
		E_INIT,
		E_CONDITION_EXPR,
		E_POST_EXPR,
		E_BASIC_BLOCK,
		E_OUT_OF_LOOP
	}

	/**
	 * This is basically a typedef
	 */
	public class VarId {}
	
	/**
	 * This is basically a typedef
	 */
	public class FuncId {}

	/**
	 * Structure that holds variable declaration parameters
	 */
	public class VarDecl {
		private VarId _id;
		private String _name;
		public String _type;
		private GraphNode _curr_graph_node;

		public VarId getVarId() {
			return _id;
		}

		public VarDecl(VarId id, String name) {
			_id = id;
			_name = name;
			_curr_graph_node = null;
		}
	}
	
	public class FuncDecl {
		private FuncId _id;
		public String _name;
		public List<VarDecl> _parameters;
		private GraphNode _return_node;
		
		public FuncDecl(FuncId id, String name){
			_id = id;
			_name = name;
			_parameters = new ArrayList<VarDecl>();
			_return_node = null;
		}
		
		public FuncId getFuncId(){
			return _id;
		}
	}

	private Map<eBinOp, String> _bin_op_strings = new EnumMap<eBinOp, String>(eBinOp.class);
	private Map<eUnOp, String> _un_op_strings = new EnumMap<eUnOp, String>(eUnOp.class);
	Map<eAssignBinOp, String> _assign_bin_op_strings = new EnumMap<eAssignBinOp, String>(
			eAssignBinOp.class);

	/** Stores all the graph */
	public Graph _gvpl_graph;

	// TODO clear the variables that aren't in scope anymore
	/** Converts a ast node id to a graph node id */
	private Map<VarId, VarDecl> _var_graph_nodes = new HashMap<VarId, VarDecl>();
	
	private Map<FuncId, FuncDecl> _func_graph_nodes = new HashMap<FuncId, FuncDecl>();

	private List<Graph> _for_loops = new ArrayList<Graph>();

	private eForLoopState _for_loop_state;

	private FuncDecl _current_function;

	/** Converts a ast node id to a VarDecl instance */
	// Map<var_id, VarDecl> _ast_variables;

	public GraphBuilder(Graph gvpl_graph)

	{
		_gvpl_graph = gvpl_graph;
		_for_loop_state = eForLoopState.E_OUT_OF_LOOP;

		// _bin_op_strings[E_ASSIGN_OP] = "=";

		_bin_op_strings.put(eBinOp.E_ADD_OP, "+");
		_bin_op_strings.put(eBinOp.E_SUB_OP, "-");
		_bin_op_strings.put(eBinOp.E_MULT_OP, "*");
		_bin_op_strings.put(eBinOp.E_DIV_OP, "/");
		_bin_op_strings.put(eBinOp.E_LESS_THAN_OP, "<=");
		_bin_op_strings.put(eBinOp.E_GREATER_THAN_OP, ">=");

		_un_op_strings.put(eUnOp.E_PLUS_PLUS_OP, "++");

		_assign_bin_op_strings.put(eAssignBinOp.E_PLUS_ASSIGN_OP, "+");
		_assign_bin_op_strings.put(eAssignBinOp.E_SUB_ASSIGN_OP, "-");
	}

	public void add_var_decl(VarDecl var_decl) {
		_var_graph_nodes.put(var_decl._id, var_decl);
	}

	public GraphNode add_direct_val(eValueType type, String value) {
		return _gvpl_graph.add_graph_node(value, NodeType.E_DIRECT_VALUE);
	}

	public void add_assign_op(VarId lhs, GraphNode rhs_node) {
		VarDecl var_decl = find_var(lhs);
		
		add_assign(var_decl, NodeType.E_VARIABLE, rhs_node);
	}
	
	/**
	 * Creates an assignment
	 * @return New node from assignment, the left from assignment
	 */
	private GraphNode add_assign(VarDecl lhs_var_decl, NodeType lhs_type, GraphNode rhs_node){
		GraphNode lhs_node = _gvpl_graph.add_graph_node(lhs_var_decl._name, lhs_type);
		lhs_var_decl._curr_graph_node = lhs_node;

		rhs_node._dependent_nodes.add(lhs_node);
		return lhs_node;
	}

	GraphNode add_un_op(eUnOp op, GraphNode val_node) {
		GraphNode un_op_node = _gvpl_graph.add_graph_node(_un_op_strings.get(op),
				NodeType.E_OPERATION);

		val_node._dependent_nodes.add(un_op_node);

		return un_op_node;
	}

	public GraphNode add_bin_op(eBinOp op, GraphNode val1_node, GraphNode val2_node) {
		GraphNode bin_op_node = _gvpl_graph.add_graph_node(_bin_op_strings.get(op),
				NodeType.E_OPERATION);

		val1_node._dependent_nodes.add(bin_op_node);
		val2_node._dependent_nodes.add(bin_op_node);

		return bin_op_node;
	}

	public GraphNode add_assign_bin_op(eAssignBinOp op, VarId lhs_var_id, GraphNode lhs_node,
			GraphNode rhs_node) {
		GraphNode bin_op_node = _gvpl_graph.add_graph_node(_assign_bin_op_strings.get(op),
				NodeType.E_OPERATION);

		lhs_node._dependent_nodes.add(bin_op_node);
		rhs_node._dependent_nodes.add(bin_op_node);

		GraphNode result_node = _gvpl_graph.add_graph_node(lhs_node._name, NodeType.E_VARIABLE);
		bin_op_node._dependent_nodes.add(result_node);

		VarDecl var_decl = find_var(lhs_var_id);
		var_decl._curr_graph_node = result_node;

		return result_node;
	}

	public GraphNode add_var_ref(VarId var) {
		VarDecl var_decl = find_var(var);
		return var_decl._curr_graph_node;
	}

	VarDecl find_var(VarId id) {
		VarDecl temp = _var_graph_nodes.get(id);

		if (temp == null)
			ErrorOutputter.fatalError("VarId " + id + " not found.\n");

		return temp;
	}

	void enter_for_init_expr() {
		if ((_for_loop_state != eForLoopState.E_BASIC_BLOCK)
				&& (_for_loop_state != eForLoopState.E_OUT_OF_LOOP))
			ErrorOutputter.fatalError("Invalid state in for loop");
		_for_loop_state = eForLoopState.E_INIT;
	}

	void enter_for_condition_expr() {
		if (_for_loop_state != eForLoopState.E_INIT)
			ErrorOutputter.fatalError("Invalid state in for loop");
		_for_loop_state = eForLoopState.E_CONDITION_EXPR;
	}

	void enter_for_post_expr() {
		if (_for_loop_state != eForLoopState.E_CONDITION_EXPR)
			ErrorOutputter.fatalError("Invalid state in for loop");
		_for_loop_state = eForLoopState.E_POST_EXPR;
	}

	void enter_for_basic_block() {
		if (_for_loop_state != eForLoopState.E_POST_EXPR)
			ErrorOutputter.fatalError("Invalid state in for loop");
		_for_loop_state = eForLoopState.E_BASIC_BLOCK;
	}

	void exit_for_loop() {
		if ((_for_loop_state != eForLoopState.E_BASIC_BLOCK)
				&& (_for_loop_state != eForLoopState.E_OUT_OF_LOOP))
			ErrorOutputter.fatalError("Invalid state in for loop");
		_for_loop_state = eForLoopState.E_OUT_OF_LOOP;
	}

	public void enter_function(FuncDecl func_decl) {
		_current_function = func_decl;

		for (VarDecl parameter : func_decl._parameters) {
			GraphNode var_node = _gvpl_graph.add_graph_node(parameter._name, NodeType.E_DECLARED_PARAMETER);
			parameter._curr_graph_node = var_node;
		}
		
		_func_graph_nodes.put(func_decl._id, func_decl);
	}
	
	public GraphNode addFuncRef(FuncId func_id, List<GraphNode> parameter_values){
		FuncDecl func_decl = _func_graph_nodes.get(func_id);
		
		if (func_decl._parameters.size() != parameter_values.size())
			ErrorOutputter.fatalError("Number of parameters differs from func declaration!");
		
		for (int i = 0; i < parameter_values.size(); ++i){
			VarDecl declared_parameter = func_decl._parameters.get(i);
			GraphNode received_parameter = parameter_values.get(i);

			received_parameter._dependent_nodes.add(declared_parameter._curr_graph_node);
		}
		
		return func_decl._return_node;
	}
	
	public void addReturnStatement(GraphNode rvalue){
		VarDecl var_decl = new VarDecl(new VarId(), _current_function._name);
		add_var_decl(var_decl);

		_current_function._return_node = add_assign(var_decl, NodeType.E_RETURN_VALUE, rvalue);
	}

	public void decrease_depth() {
	}

}
