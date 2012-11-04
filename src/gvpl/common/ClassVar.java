package gvpl.common;

import gvpl.cdt.AstLoaderCDT;
import gvpl.cdt.MemberFunc;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Variable (instance) of a class
 *
 */
public class ClassVar extends Var implements IClassVar{
	
	static Logger logger = LogManager.getLogger(Graph.class.getName());

	Map<MemberId, IVar> _memberInstances = new LinkedHashMap<MemberId, IVar>();
	Map<IVar, MemberId> _memberIdFromInstance = new LinkedHashMap<IVar, MemberId>();
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
			IVar member_instance = parentAstLoader.addVarDecl(memberName,
					struct_member.getMemberType());
			addMember(entry.getKey(), member_instance);
		}
		
		for(ClassDecl parentClass : _classDecl.getParentClasses()) {
			logger.debug("Parent instance of {} from {}", parentClass.getName(), name);
			ClassVar parentInstance = new ClassVar(graph, name, parentClass, parentAstLoader);
			_parentInstances.add(parentInstance);
		}
	}
	
	private void addMember(MemberId id, IVar var) {
		_memberInstances.put(id, var);
		_memberIdFromInstance.put(var, id);
	}

	public IVar getMember(MemberId memberId) {
		IVar member = _memberInstances.get(memberId);
		if(member != null)
			return member;
		
		for(ClassVar parent : _parentInstances) {
			member = parent.getMember(memberId);
			if(member != null)
				return member;
		}
		
		return null;
	}
	
	public MemberId getMember(IVar memberInstance) {
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
	public void initializeVar(NodeType nodeType, Graph graph, AstLoader astLoader, 
			AstInterpreter astInterpreter, int startingLine) {
		for(ClassVar parent : _parentInstances) 
			parent.initializeVar(nodeType, graph, astLoader, astInterpreter, startingLine);
		
		for (IVar var : _memberInstances.values()) {
			var.initializeVar(NodeType.E_VARIABLE, graph, astLoader, astInterpreter, startingLine);
			var.setOwner(this);
		}
	}
	
	@Override
	public void callConstructor(List<FuncParameter> parameterValues, NodeType nodeType, Graph graph,
			AstLoader astLoader, AstInterpreter astInterpreter, int startingLine) {

		// TODO call only the variables that wasn't written in constructorFunc.loadMemberFuncRef
		for (IVar var : _memberInstances.values()) {
			var.callConstructor(null, NodeType.E_VARIABLE, graph, astLoader, astInterpreter,
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

	public void callDestructor(AstLoader astLoader, Graph graph, int startingLine) {
		// TODO call the parent classes destructor

		logger.debug("Destructor of {}", _name);
		MemberFunc destructorFunc = _classDecl.getDestructorFunc();
		if (destructorFunc != null)
			destructorFunc.loadMemberFuncRef(this, new ArrayList<FuncParameter>(), graph,
					astLoader, startingLine);
	}
	
	public ClassDecl getClassDecl() {
		return _classDecl;
	}
	
	@Override
	public List<IVar> getInternalVars() {
		List<IVar> internalVars = new ArrayList<>();
		
		for (ClassVar parent : _parentInstances) {
			internalVars.addAll(parent.getInternalVars());
		}
		
		for(IVar member : _memberInstances.values()) {
			internalVars.addAll(member.getInternalVars());
		}
		
		return internalVars;
	}
	
	@Override
	public void setGraph(Graph graph) {
		super.setGraph(graph);
		List<IVar> internalVars = getInternalVars();
		for(IVar internalVar : internalVars)
			internalVar.setGraph(graph);
	}
	
	public GraphNode loadMemberFuncRef(MemberFunc memberFunc, List<FuncParameter> parameterValues,
			Graph graph, AstLoaderCDT astLoader, int startingLine) {
		return memberFunc.loadMemberFuncRef(this, parameterValues, graph, astLoader, startingLine);
	}
}