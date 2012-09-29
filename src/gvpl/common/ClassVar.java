package gvpl.common;

import gvpl.cdt.AstInterpreter;
import gvpl.cdt.AstLoader;
import gvpl.cdt.ClassDecl;
import gvpl.cdt.MemberFunc;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Variable (instance) of a class
 *
 */
public class ClassVar extends Var {

	Map<MemberId, Var> _memberInstances = new LinkedHashMap<MemberId, Var>();
	Map<Var, MemberId> _memberIdFromInstace = new LinkedHashMap<Var, MemberId>();
	
	ClassDecl _classDecl;

	public ClassVar(Graph graph, String name, ClassDecl classDecl,
			AstLoader parentAstLoader) {
		super(graph, name, classDecl.getTypeId());
		_classDecl = classDecl;

		// For each member of the struct, create a variable instance of the
		// member
		for (Map.Entry<MemberId, ClassMember> entry : classDecl.getMemberVarGraphNodes()) {
			ClassMember struct_member = entry.getValue();

			String memberName = name + "." + struct_member.getName();
			Var member_instance = parentAstLoader.addVarDecl(memberName,
					struct_member.getMemberType(), null);
			addMember(entry.getKey(), member_instance);
		}
	}
	
	private void addMember(MemberId id, Var var) {
		_memberInstances.put(id, var);
		_memberIdFromInstace.put(var, id);
	}

	public Var getMember(MemberId member_id) {
		return _memberInstances.get(member_id);
	}
	
	public MemberId getMember(Var memberInstance) {
		return _memberIdFromInstace.get(memberInstance);
	}

	/**
	 * It is used mainly to be used in function parameters
	 */
	@Override
	public void initializeGraphNode(NodeType nodeType, Graph graph, AstLoader astLoader, 
			AstInterpreter astInterpreter, int startingLine) {
		for (Var var : _memberInstances.values()) {
			var.initializeGraphNode(NodeType.E_VARIABLE, graph, astLoader, astInterpreter, startingLine);
			var.setOwner(this);
		}
	}
	
	@Override
	public void constructor(List<FuncParameter> parameter_values, NodeType nodeType, Graph graph, 
			AstLoader astLoader, AstInterpreter astInterpreter, int startingLine) {
		//TODO só chamar para as variáveis que não foram escritas em constructorFunc.loadMemberFuncRef
		for (Var var : _memberInstances.values()) {
			var.constructor(null, NodeType.E_VARIABLE, graph, astLoader, astInterpreter, startingLine);
			var.setOwner(this);
		}
		
		MemberFunc constructorFunc = _classDecl.getConstructorFunc();
		if(constructorFunc == null)
			return;
		
		if(parameter_values == null)
			return;
		
		constructorFunc.loadMemberFuncRef(this, parameter_values, _gvplGraph, astLoader, startingLine);
	}
	
	public ClassDecl getClassDecl() {
		return _classDecl;
	}
	
	@Override
	public List<Var> getInternalVars() {
		List<Var> internalVars = new ArrayList<>();
		for(Var member : _memberInstances.values()) {
			internalVars.addAll(member.getInternalVars());
		}
		
		return internalVars;
	}
}