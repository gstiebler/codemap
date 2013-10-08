package gvpl.common;

import gvpl.cdt.BaseScopeCDT;
import gvpl.cdt.function.MemberFunc;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import debug.ExecTreeLogger;

/**
 * Variable (instance) of a class
 *
 */
public class ClassVar extends Var implements IClassVar{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7200242002428741153L;

	static Logger logger = LogManager.getLogger(Graph.class.getName());

	Map<MemberId, IVar> _memberInstances = new LinkedHashMap<MemberId, IVar>();
	Map<IVar, MemberId> _memberIdFromInstance = new LinkedHashMap<IVar, MemberId>();
	List<ClassVar> _parentInstances = new ArrayList<ClassVar>();
	
	ClassDecl _classDecl;

	public ClassVar(Graph graph, String name, ClassDecl classDecl, AstInterpreter astInterpreter) {
		super(graph, name, classDecl.getTypeId());
		_classDecl = classDecl;

		// For each member of the struct, create a variable instance of the
		// member
		for (Map.Entry<MemberId, ClassMember> entry : classDecl.getMembersMap()) {
			ClassMember struct_member = entry.getValue();

			String memberName = name + "." + struct_member.getName();
			IVar memberInstance = BaseScopeCDT.addVarDecl(memberName,
					struct_member.getMemberType(), _gvplGraph, astInterpreter);
			addMember(entry.getKey(), memberInstance);
		}
		
		for(ClassDecl parentClass : _classDecl.getParentClasses()) {
			logger.debug("Parent instance of {} from {}", parentClass.getName(), name);
			ClassVar parentInstance = new ClassVar(graph, name, parentClass, astInterpreter);
			_parentInstances.add(parentInstance);
		}
	}
	
	private void addMember(MemberId id, IVar var) {
		_memberInstances.put(id, var);
		_memberIdFromInstance.put(var, id);
	}

	public IVar getMember(MemberId memberId) {
		ExecTreeLogger.log("");
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
	public void initializeVar(NodeType nodeType, Graph graph, AstInterpreter astInterpreter) {
		for(ClassVar parent : _parentInstances) 
			parent.initializeVar(nodeType, graph, astInterpreter);
		
		for (IVar var : _memberInstances.values()) {
			var.initializeVar(NodeType.E_VARIABLE, graph, astInterpreter);
		}
	}
	
	@Override
	public void callConstructor(List<FuncParameter> parameterValues, NodeType nodeType, Graph graph,
			BaseScope astLoader, AstInterpreter astInterpreter) {
		
		int numParameter = 0;
		if(parameterValues != null)
			numParameter = parameterValues.size();
		MemberFunc constructorFunc = _classDecl.getConstructorFunc(numParameter);

		//if (parameterValues == null)
		//	return;
		
		for (Map.Entry<MemberId, IVar> entry : _memberInstances.entrySet()) {
			if((constructorFunc != null) && constructorFunc.memberIsInitialized(entry.getKey()))
				continue;
			
			IVar member = entry.getValue();
			
			member.callConstructor(null, NodeType.E_VARIABLE, graph, astLoader, astInterpreter);
		}
		
		if (constructorFunc == null)
			return;

		constructorFunc.addFuncRef(parameterValues, graph, this, astLoader);
	}

	public void callDestructor(BaseScope astLoader, Graph graph) {
		// TODO call the parent classes destructor

		logger.debug("Destructor of {}", _name);
		MemberFunc destructorFunc = _classDecl.getDestructorFunc();
		if (destructorFunc != null)
			destructorFunc.addFuncRef(new ArrayList<FuncParameter>(), graph, this, astLoader);
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
	
	public Value loadMemberFuncRef(MemberFunc memberFunc, List<FuncParameter> parameterValues,
			Graph graph, BaseScopeCDT astLoader) {
		return memberFunc.addFuncRef(parameterValues, graph, this, astLoader);
	}
}