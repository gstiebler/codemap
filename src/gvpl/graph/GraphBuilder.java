package gvpl.graph;

import gvpl.common.MemberStructInstance;
import gvpl.common.VarDecl;
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

	/** typedef */
	public class TypeId {
	}

	/** typedef */
	public class MemberId {
	}

	/** typedef */
	public class MemberInstanceId {
	}

	/**
	 * Structure that holds variable declaration parameters
	 */
	public class DirectVarDecl extends VarDecl {
		protected String _name;

		public DirectVarDecl(String name, TypeId type) {
			super(type);
			_name = name;
		}
		
		public String getName() {
			return _name;
		}
		
		public void initializeGraphNode() {
			updateNode(_gvpl_graph.add_graph_node(_name, NodeType.E_VARIABLE));
		}
	}
	
	public class StructVarDecl extends DirectVarDecl {
		
		Map<MemberId, MemberStructInstance> _member_instances = new HashMap<MemberId, MemberStructInstance>();

		public StructVarDecl(String name, TypeId type, StructDecl structDecl) {
			super(name, type);
			
			//For each member of the struct, create a variable instance of the member
			for (Map.Entry<MemberId, StructMember> entry : structDecl._member_var_graph_nodes.entrySet()){
				StructMember struct_member = entry.getValue();
				
				MemberStructInstance member_instance = new MemberStructInstance(struct_member, this);
				_member_instances.put(entry.getKey(), member_instance);
			}
		}

		public MemberStructInstance findMember(MemberId member_id) {
			return _member_instances.get(member_id);
		}
		
	}
	
	public class StructMember{
		private MemberId _id;
		private String _name;
		private TypeId _type;
		//private StructDecl _parent;
		
		public StructMember(StructDecl parent, MemberId id, String name, TypeId type) {
			//_parent = parent;
			_id = id;
			_name = name;
			_type = type;
		}
		
		public MemberId getMemberId() {
			return _id;
		}
		
		public TypeId getMemberType() {
			return _type;
		}
		
		public String getName() {
			return _name;
		}
	}

	public class StructDecl {
		public TypeId _id;
		//private String _name;
		public Map<MemberId, StructMember> _member_var_graph_nodes;

		public StructDecl(TypeId id, String name) {
			_id = id;
			//_name = name;
			_member_var_graph_nodes = new HashMap<MemberId, StructMember>();
		}
		
		public void addMember(StructMember structMember) {
			_member_var_graph_nodes.put(structMember._id, structMember);
		}
	}

	private Map<eBinOp, String> _bin_op_strings = new EnumMap<eBinOp, String>(eBinOp.class);
	private Map<eUnOp, String> _un_op_strings = new EnumMap<eUnOp, String>(eUnOp.class);
	Map<eAssignBinOp, String> _assign_bin_op_strings = new EnumMap<eAssignBinOp, String>(
			eAssignBinOp.class);

	/** Stores all the graph */
	public Graph _gvpl_graph = new Graph();

	/** Converts a ast node id to a VarDecl instance */
	// Map<var_id, VarDecl> _ast_variables;

	public GraphBuilder()
	{
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

	public GraphNode add_direct_val(eValueType type, String value) {
		return _gvpl_graph.add_graph_node(value, NodeType.E_DIRECT_VALUE);
	}

	public void add_assign_op(VarDecl var_decl_lhs, GraphNode rhs_node) {
		add_assign(var_decl_lhs, NodeType.E_VARIABLE, rhs_node);
	}

	/**
	 * Creates an assignment
	 * 
	 * @return New node from assignment, the left from assignment
	 */
	public GraphNode add_assign(VarDecl lhs_var_decl, NodeType lhs_type, GraphNode rhs_node) {
		GraphNode lhs_node = _gvpl_graph.add_graph_node(lhs_var_decl.getName(), lhs_type);
		lhs_var_decl.updateNode(lhs_node);

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

	public GraphNode add_assign_bin_op(eAssignBinOp op, VarDecl lhs_var_decl, GraphNode lhs_node,
			GraphNode rhs_node) {
		GraphNode bin_op_node = _gvpl_graph.add_graph_node(_assign_bin_op_strings.get(op),
				NodeType.E_OPERATION);

		lhs_node._dependent_nodes.add(bin_op_node);
		rhs_node._dependent_nodes.add(bin_op_node);

		GraphNode result_node = _gvpl_graph.add_graph_node(lhs_node._name, NodeType.E_VARIABLE);
		bin_op_node._dependent_nodes.add(result_node);

		lhs_var_decl.updateNode(result_node);

		return result_node;
	}

	public GraphNode add_var_ref(VarDecl var_decl) {
		return var_decl.getCurrentNode();
	}
	
	/**
	 * Add one graph into another
	 * @param graphBuilder
	 * @return The map between the nodes in the old graph and in the new
	 */
	public Map<GraphNode, GraphNode> addGraph(GraphBuilder graphBuilder){
		return _gvpl_graph.merge(graphBuilder._gvpl_graph);
	}

}
