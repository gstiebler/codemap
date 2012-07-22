package gvpl.cdt;

import gvpl.common.MemberStructInstance;
import gvpl.common.VarDecl;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.DirectVarDecl;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.StructMember;
import gvpl.graph.GraphBuilder.StructVarDecl;
import gvpl.graph.GraphNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class MemberFunc extends Function {

	private Struct _parentLoadStruct;
	private Map<MemberId, DirectVarDecl> _var_from_members_map = new HashMap<MemberId, DirectVarDecl>();
	private Map<VarDecl, MemberId> _memberFromVar = new HashMap<VarDecl, MemberId>();
	private Map<VarDecl, MemberId> _writtenMembers = new HashMap<VarDecl, MemberId>();
	private Map<VarDecl, MemberId> _readMembers = new HashMap<VarDecl, MemberId>();

	public MemberFunc(Struct parent) {
		super(new GraphBuilder(), parent, parent._cppMaps, parent._astInterpreter);
		_parentLoadStruct = parent;

		List<StructMember> members = _parentLoadStruct.getMembers();
		//declare a variable for each member of the struct
		for (StructMember member : members) {
			DirectVarDecl member_var = _graph_builder.new DirectVarDecl(member.getName(),
					member.getMemberType());
			member_var.initializeGraphNode(NodeType.E_VARIABLE);
			_var_from_members_map.put(member.getMemberId(), member_var);
			_memberFromVar.put(member_var, member.getMemberId());
		}
	}
	
	protected String calcName(String internalName) {
		return _parentLoadStruct.getName() + "::" + internalName;
	}

	@Override
	public IBinding load(IASTFunctionDefinition fd) {
		return super.load(fd);
	}

	@Override
	/**
	 * Returns the VarDecl of the reference to a variable
	 * @return The VarDecl of the reference to a variable
	 */
	public VarDecl getVarDeclOfReference(IASTIdExpression id_expr) {
		// Check if the variable is declared inside the own block
		VarDecl var_decl = getVarDeclOfLocalReference(id_expr);
		if (var_decl != null)
			return var_decl;
		// Ok, if the function did not returned until here, the variable is a
		// member.

		IASTName name = id_expr.getName();
		IBinding binding = name.resolveBinding();
		StructMember structMember = _parentLoadStruct.getMember(binding);
		MemberId lhs_member_id = structMember.getMemberId();

		DirectVarDecl direct_var_decl = _var_from_members_map.get(lhs_member_id);
		_readMembers.put(direct_var_decl, lhs_member_id);

		return direct_var_decl;
	}
	
	@Override
	public void varWrite(VarDecl var) {
		if (_parent != null) 
			_parent.varWrite(var);
		
		if(_memberFromVar.containsKey(var))
			_writtenMembers.put(var, _memberFromVar.get(var));
	}

	/**
	 * Copy the internal graph to the main graph and bind the variables of the
	 * structure to the used variables in the member function
	 * 
	 * @param structVarDecl
	 * @param graphBuilder
	 */
	public GraphNode loadMemberFuncRef(StructVarDecl structVarDecl,
			List<GraphNode> parameter_values, GraphBuilder graphBuilder) {
		Map<GraphNode, GraphNode> map = graphBuilder._gvpl_graph.addSubGraph(_graph_builder._gvpl_graph);

		for (Map.Entry<VarDecl, MemberId> entry : _readMembers.entrySet()) {
			VarDecl varDecl = entry.getKey();
			GraphNode firstNode = varDecl.getFirstNode();
			GraphNode firstNodeInNewGraph = map.get(firstNode);
			MemberStructInstance memberInstance = structVarDecl.findMember(entry.getValue());
			memberInstance.getCurrentNode().addDependentNode(firstNodeInNewGraph);
		}

		for (Map.Entry<VarDecl, MemberId> entry : _writtenMembers.entrySet()) {
			VarDecl varDecl = entry.getKey();
			GraphNode currNode = varDecl.getCurrentNode();
			GraphNode currNodeInNewGraph = map.get(currNode);
			MemberStructInstance memberInstance = structVarDecl.findMember(entry.getValue());
			graphBuilder.add_assign(memberInstance, NodeType.E_VARIABLE, currNodeInNewGraph, null);
		}

		return addParametersReferenceAndReturn(parameter_values, map);
	}

}
