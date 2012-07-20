package gvpl.cdt;

import gvpl.common.MemberStructInstance;
import gvpl.common.VarDecl;
import gvpl.common.typedefs.VarId;
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
	private Map<MemberId, DirectVarDecl> _var_from_members = new HashMap<MemberId, DirectVarDecl>();

	public MemberFunc(Struct parent) {
		super(new GraphBuilder(), parent, parent._cppMaps, parent._astInterpreter);
		_parentLoadStruct = parent;

		List<StructMember> members = _parentLoadStruct.getMembers();
		for (StructMember member : members) {
			DirectVarDecl member_var = _graph_builder.new DirectVarDecl(new VarId(),
					member.getName(), member.getMemberType());
			member_var.initializeGraphNode();
			_var_from_members.put(member.getMemberId(), member_var);
		}
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
		// Ok, if the function did not returned until here, the variable is a member.

		IASTName name = id_expr.getName();
		IBinding binding = name.resolveBinding();
		StructMember structMember = _parentLoadStruct.getMember(binding);
		MemberId lhs_member_id = structMember.getMemberId();

		DirectVarDecl direct_var_decl = _var_from_members.get(lhs_member_id);

		direct_var_decl.setRead();

		return direct_var_decl;
	}

	/**
	 * Copy the internal graph to the main graph and bind the variables of the
	 * structure to the used variables in the member function
	 * 
	 * @param structVarDecl
	 * @param graphBuilder
	 */
	public void loadMemberFuncRef(StructVarDecl structVarDecl, GraphBuilder graphBuilder) {
		Map<GraphNode, GraphNode> map = graphBuilder.addGraph(_graph_builder);

		for (Map.Entry<MemberId, DirectVarDecl> entry : _var_from_members.entrySet()) {
			DirectVarDecl var_decl = entry.getValue();

			// Binds the variables from the instance of the structure to the
			// internal variables
			if (var_decl.getWritten()) {
				GraphNode firstNode = var_decl.getFirstNode();
				GraphNode firstNodeInNewGraph = map.get(firstNode);
				MemberStructInstance memberInstance = structVarDecl.findMember(entry.getKey());
				memberInstance.getCurrentNode()._dependent_nodes.add(firstNodeInNewGraph);
			}

			// Binds the internal variables to the variables from the instance
			// of the structure
			if (var_decl.getRead()) {
				GraphNode currNode = var_decl.getCurrentNode();
				GraphNode currNodeInNewGraph = map.get(currNode);
				MemberStructInstance memberInstance = structVarDecl.findMember(entry.getKey());
				graphBuilder.add_assign(memberInstance, NodeType.E_VARIABLE, currNodeInNewGraph);
			}
		}
	}

}
