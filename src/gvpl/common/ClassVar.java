package gvpl.common;

import gvpl.cdt.AstInterpreter;
import gvpl.cdt.AstLoader;
import gvpl.cdt.ClassDecl;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.TypeId;

import java.util.HashMap;
import java.util.Map;

/**
 * Variable (instance) of a class
 *
 */
public class ClassVar extends Var {

	Map<MemberId, Var> _memberInstances = new HashMap<MemberId, Var>();

	public ClassVar(Graph graph, String name, TypeId type, ClassDecl classDecl,
			AstLoader parentAstLoader) {
		super(graph, name, type);

		// For each member of the struct, create a variable instance of the
		// member
		for (Map.Entry<MemberId, ClassMember> entry : classDecl.getMemberVarGraphNodes()) {
			ClassMember struct_member = entry.getValue();

			String memberName = name + "." + struct_member.getName();
			Var member_instance = parentAstLoader.addVarDecl(memberName,
					struct_member.getMemberType(), null);
			_memberInstances.put(entry.getKey(), member_instance);
		}
	}

	public Var findMember(MemberId member_id) {
		Var varDecl = _memberInstances.get(member_id);
		if (varDecl != null)
			return varDecl;

		for (Var var : _memberInstances.values()) {
			if (var instanceof ClassVar) {
				varDecl = ((ClassVar) var).findMember(member_id);
				if (varDecl != null)
					return varDecl;
			}

		}

		return null;
	}

	/**
	 * It is used mainly to be used in function parameters
	 */
	@Override
	public void initializeGraphNode(NodeType nodeType, Graph graph, AstLoader astLoader, 
			AstInterpreter astInterpreter, int startingLine) {
		super.initializeGraphNode(nodeType, graph, astLoader, astInterpreter, startingLine);

		for (Var var : _memberInstances.values())
			var.initializeGraphNode(NodeType.E_VARIABLE, graph, astLoader, astInterpreter, startingLine);
	}
	
	@Override
	public void constructor(NodeType nodeType, Graph graph, AstLoader astLoader, 
			AstInterpreter astInterpreter, int startingLine) {
		for (Var var : _memberInstances.values())
			var.constructor(NodeType.E_VARIABLE, graph, astLoader, astInterpreter, startingLine);
		
		//execConstructorFunction();
	}

	public Map<MemberId, Var> getInternalVariables() {
		Map<MemberId, Var> internalVariables = new HashMap<MemberId, Var>();

		internalVariables.putAll(_memberInstances);

		for (Var var : _memberInstances.values())
			if (var instanceof ClassVar)
				internalVariables.putAll(((ClassVar) var)._memberInstances);

		return internalVariables;
	}
}