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
	Map<Var, MemberId> _memberIdFromInstance = new LinkedHashMap<Var, MemberId>();
	List<ClassVar> _parentInstances = new ArrayList<ClassVar>();
	
	ClassDecl _classDecl;

	public ClassVar(Graph graph, String name, ClassDecl classDecl,
			AstLoader parentAstLoader) {
		super(graph, name, classDecl.getTypeId());
		_classDecl = classDecl;

		// For each member of the struct, create a variable instance of the
		// member
		for (Map.Entry<MemberId, ClassMember> entry : classDecl.getMembersMap()) {
			ClassMember struct_member = entry.getValue();

			String memberName = name + "." + struct_member.getName();
			Var member_instance = parentAstLoader.addVarDecl(memberName,
					struct_member.getMemberType(), null);
			addMember(entry.getKey(), member_instance);
		}
		
		for(ClassDecl parentClass : _classDecl.getParentClasses()) {
			ClassVar parentInstance = new ClassVar(graph, name, parentClass, parentAstLoader);
			_parentInstances.add(parentInstance);
		}
	}
	
	private void addMember(MemberId id, Var var) {
		_memberInstances.put(id, var);
		_memberIdFromInstance.put(var, id);
	}

	public Var getMember(MemberId memberId) {
		Var member = _memberInstances.get(memberId);
		if(member != null)
			return member;
		
		for(ClassVar parent : _parentInstances) {
			member = parent.getMember(memberId);
			if(member != null)
				return member;
		}
		
		return null;
	}
	
	public MemberId getMember(Var memberInstance) {
		MemberId member = _memberIdFromInstance.get(memberInstance);
		if(member != null)
			return member;
		
		for(ClassVar parent : _parentInstances) {
			member = parent.getMember(memberInstance);
			if(member != null)
				return member;
		}
		
		return null;
	}

	/**
	 * It is used mainly to be used in function parameters
	 */
	@Override
	public void initializeGraphNode(NodeType nodeType, Graph graph, AstLoader astLoader, 
			AstInterpreter astInterpreter, int startingLine) {
		for(ClassVar parent : _parentInstances) 
			parent.initializeGraphNode(nodeType, graph, astLoader, astInterpreter, startingLine);
		
		for (Var var : _memberInstances.values()) {
			var.initializeGraphNode(NodeType.E_VARIABLE, graph, astLoader, astInterpreter, startingLine);
			var.setOwner(this);
		}
	}
	
	@Override
	public void constructor(List<FuncParameter> parameterValues, NodeType nodeType, Graph graph,
			AstLoader astLoader, AstInterpreter astInterpreter, int startingLine) {

		/*for (ClassVar parent : _parentInstances) {
			parent.constructor(parameterValues, nodeType, graph, astLoader, astInterpreter,
					startingLine);
		}*/

		// TODO s� chamar para as vari�veis que n�o foram escritas em
		// constructorFunc.loadMemberFuncRef
		for (Var var : _memberInstances.values()) {
			var.constructor(null, NodeType.E_VARIABLE, graph, astLoader, astInterpreter,
					startingLine);
			var.setOwner(this);
		}

		MemberFunc constructorFunc = _classDecl.getConstructorFunc();
		if (constructorFunc == null)
			return;

		if (parameterValues == null)
			return;

		constructorFunc.loadMemberFuncRef(this, parameterValues, _gvplGraph, astLoader,
				startingLine);
	}
	
	public ClassDecl getClassDecl() {
		return _classDecl;
	}
	
	@Override
	public List<Var> getInternalVars() {
		List<Var> internalVars = new ArrayList<>();
		
		for (ClassVar parent : _parentInstances) {
			internalVars.addAll(parent.getInternalVars());
		}
		
		for(Var member : _memberInstances.values()) {
			internalVars.addAll(member.getInternalVars());
		}
		
		return internalVars;
	}
}