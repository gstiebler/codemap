package gvpl.graph;

import gvpl.cdt.AstLoader;
import gvpl.common.VarDecl;
import gvpl.graph.Graph.NodeType;

import java.util.EnumMap;
import java.util.HashMap;
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
		E_GREATER_THAN_OP,
		E_LESS_EQUAL_OP,
		E_GREATER_EQUAL_OP
	}

	public enum eAssignBinOp {
		E_INVALID_A_BIN_OP,
		E_ASSIGN_OP,
		E_PLUS_ASSIGN_OP,
		E_SUB_ASSIGN_OP,
		E_DIV_ASSIGN_OP,
		E_MULT_ASSIGN_OP
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
			super(type, _gvplGraph);
			_name = name;
		}
		
		public String getName() {
			return _name;
		}
	}
	
	public class StructVarDecl extends DirectVarDecl {
		
		Map<MemberId, VarDecl> _memberInstances = new HashMap<MemberId, VarDecl>();

		public StructVarDecl(String name, TypeId type, StructDecl structDecl, AstLoader parentAstLoader) {
			super(name, type);
			
			//For each member of the struct, create a variable instance of the member
			for (Map.Entry<MemberId, StructMember> entry : structDecl._member_var_graph_nodes.entrySet()){
				StructMember struct_member = entry.getValue();
				
				String memberName = name + "." + struct_member.getName();
				VarDecl member_instance = parentAstLoader.addVarDecl(memberName, struct_member.getMemberType());
				_memberInstances.put(entry.getKey(), member_instance);
			}
		}

		public VarDecl findMember(MemberId member_id) {
			VarDecl varDecl = _memberInstances.get(member_id);
			if(varDecl != null)
				return varDecl;
			
			for (VarDecl var : _memberInstances.values()){
				if (var instanceof StructVarDecl){
					varDecl = ((StructVarDecl)var).findMember(member_id);
					if (varDecl != null)
						return varDecl;
				}
					
			}
			
			return null;
		}
		
		@Override
		public void initializeGraphNode(NodeType type) {
			super.initializeGraphNode(type);

			for (VarDecl var : _memberInstances.values())
				var.initializeGraphNode(NodeType.E_VARIABLE);
		}
		
		public Map<MemberId, VarDecl> getInternalVariables() {
			Map<MemberId, VarDecl> internalVariables = new HashMap<MemberId, VarDecl>();
			
			internalVariables.putAll(_memberInstances);
			
			for (VarDecl var : _memberInstances.values())
				if (var instanceof StructVarDecl)
					internalVariables.putAll(((StructVarDecl)var)._memberInstances);
			
			return internalVariables;
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
		private String _name;
		private Map<MemberId, StructMember> _member_var_graph_nodes;

		public StructDecl(TypeId id, String name) {
			_id = id;
			_name = name;
			_member_var_graph_nodes = new HashMap<MemberId, StructMember>();
		}
		
		public void addMember(StructMember structMember) {
			_member_var_graph_nodes.put(structMember._id, structMember);
		}
		
		public String getName() {
			return _name;
		}
	}

	private Map<eBinOp, String> _bin_op_strings = new EnumMap<eBinOp, String>(eBinOp.class);
	private Map<eUnOp, String> _un_op_strings = new EnumMap<eUnOp, String>(eUnOp.class);
	Map<eAssignBinOp, String> _assign_bin_op_strings = new EnumMap<eAssignBinOp, String>(
			eAssignBinOp.class);

	/** Stores all the graph */
	public Graph _gvplGraph = new Graph();

	/** Converts a ast node id to a VarDecl instance */
	// Map<var_id, VarDecl> _ast_variables;

	public GraphBuilder()
	{
		// _bin_op_strings[E_ASSIGN_OP] = "=";

		_bin_op_strings.put(eBinOp.E_ADD_OP, "+");
		_bin_op_strings.put(eBinOp.E_SUB_OP, "-");
		_bin_op_strings.put(eBinOp.E_MULT_OP, "*");
		_bin_op_strings.put(eBinOp.E_DIV_OP, "/");
		_bin_op_strings.put(eBinOp.E_LESS_THAN_OP, "<");
		_bin_op_strings.put(eBinOp.E_GREATER_THAN_OP, ">");
		_bin_op_strings.put(eBinOp.E_LESS_EQUAL_OP, "<=");
		_bin_op_strings.put(eBinOp.E_GREATER_EQUAL_OP, ">=");

		_un_op_strings.put(eUnOp.E_PLUS_PLUS_OP, "++");

		_assign_bin_op_strings.put(eAssignBinOp.E_PLUS_ASSIGN_OP, "+");
		_assign_bin_op_strings.put(eAssignBinOp.E_SUB_ASSIGN_OP, "-");
		_assign_bin_op_strings.put(eAssignBinOp.E_DIV_ASSIGN_OP, "/");
		_assign_bin_op_strings.put(eAssignBinOp.E_MULT_ASSIGN_OP, "*");
	}

	public GraphNode add_direct_val(eValueType type, String value) {
		return _gvplGraph.add_graph_node(value, NodeType.E_DIRECT_VALUE);
	}

	public void addAssignOp(VarDecl var_decl_lhs, GraphNode rhs_node, AstLoader astLoader) {
		addAssign(var_decl_lhs, NodeType.E_VARIABLE, rhs_node, astLoader);
	}

	/**
	 * Creates an assignment
	 * 
	 * @return New node from assignment, the left from assignment
	 */
	public GraphNode addAssign(VarDecl lhs_var_decl, NodeType lhs_type, GraphNode rhs_node, AstLoader astLoader) {
		GraphNode lhs_node = _gvplGraph.add_graph_node(lhs_var_decl, lhs_type);
		rhs_node.addDependentNode(lhs_node, astLoader);
		lhs_var_decl.updateNode(lhs_node);

		return lhs_node;
	}

	GraphNode add_un_op(eUnOp op, GraphNode val_node, AstLoader astLoader) {
		GraphNode un_op_node = _gvplGraph.add_graph_node(_un_op_strings.get(op),
				NodeType.E_OPERATION);

		val_node.addDependentNode(un_op_node, astLoader);

		return un_op_node;
	}

	public GraphNode addNotOp(GraphNode val_node, AstLoader astLoader) {
		GraphNode notOpNode = _gvplGraph.add_graph_node("!", NodeType.E_OPERATION);
		val_node.addDependentNode(notOpNode, astLoader);

		return notOpNode;
	} 

	public GraphNode addBinOp(eBinOp op, GraphNode val1_node, GraphNode val2_node, AstLoader astLoader) {
		GraphNode bin_op_node = _gvplGraph.add_graph_node(_bin_op_strings.get(op),
				NodeType.E_OPERATION);

		val1_node.addDependentNode(bin_op_node, astLoader);
		val2_node.addDependentNode(bin_op_node, astLoader);

		return bin_op_node;
	}

	public GraphNode add_assign_bin_op(eAssignBinOp op, VarDecl lhs_var_decl, GraphNode lhs_node,
			GraphNode rhs_node, AstLoader astLoader) {
		GraphNode bin_op_node = _gvplGraph.add_graph_node(_assign_bin_op_strings.get(op),
				NodeType.E_OPERATION);

		lhs_node.addDependentNode(bin_op_node, astLoader);
		rhs_node.addDependentNode(bin_op_node, astLoader);

		return addAssign(lhs_var_decl, NodeType.E_VARIABLE, bin_op_node, astLoader);
	}

	public GraphNode add_var_ref(VarDecl var_decl) {
		return var_decl.getCurrentNode();
	}
	
	public void addIf(VarDecl var, GraphNode ifTrue, GraphNode ifFalse, GraphNode condition, AstLoader astLoader) {
		GraphNode ifOpNode = _gvplGraph.add_graph_node("If", NodeType.E_OPERATION);

		ifTrue.addDependentNode(ifOpNode, astLoader);
		ifFalse.addDependentNode(ifOpNode, astLoader);
		condition.addDependentNode(ifOpNode, astLoader);
		
		addAssign(var, NodeType.E_VARIABLE, ifOpNode, null);
	}

}
