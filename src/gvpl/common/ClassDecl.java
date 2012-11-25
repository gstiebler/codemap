package gvpl.common;

import gvpl.cdt.function.MemberFunc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class ClassDecl {

	protected String _name;
	protected TypeId _typeId;
	protected List<MemberFunc> _constructorFuncs = new ArrayList<MemberFunc>();
	protected MemberFunc _destructorFunc = null;
	protected Map<MemberId, ClassMember> _memberVarGraphNodes = new LinkedHashMap<MemberId, ClassMember>();
	protected Map<Integer, MemberFunc> _opOverloadMethods = new LinkedHashMap<Integer, MemberFunc>();
	CodeLocation _location;
	
	public ClassDecl(CodeLocation location) {
		_typeId = new TypeId();
		_location = location;
	}

	public TypeId getTypeId() {
		return _typeId;
	}

	//TODO check parameters types too
	public MemberFunc getConstructorFunc(int numParameters) {
		for(MemberFunc constructor : _constructorFuncs) {
			if(constructor.getNumParameters() == numParameters)
				return constructor;
		}
		
		return null;
	}

	public MemberFunc getDestructorFunc() {
		return _destructorFunc;
	}

	public Iterable<Map.Entry<MemberId, ClassMember>> getMembersMap() {
		return _memberVarGraphNodes.entrySet();
	}

	public String getName() {
		return _name;
	}

	public Set<MemberId> getMemberIds() {
		return getAllMembersIds().keySet();
	}
	
	public void addMember(ClassMember structMember) {
		_memberVarGraphNodes.put(structMember.getMemberId(), structMember);
	}
	
	private Map<MemberId, ClassMember> getAllMembersIds() {
		Map<MemberId, ClassMember> allMembers = new LinkedHashMap<MemberId, ClassMember>();
		allMembers.putAll(_memberVarGraphNodes);
		
		for(ClassDecl parentClass : getParentClasses()) 
			allMembers.putAll(parentClass.getAllMembersIds());
		
		return allMembers;
	}

	public void setConstructorFunc(MemberFunc constructorFunc) {
		_constructorFuncs.add(constructorFunc);
		constructorFunc.setIsConstructor();
	}

	public void setDestructorFunc(MemberFunc destructorFunc) {
		_destructorFunc = destructorFunc;
	}

	/**
	 * 
	 * @param memberFunc
	 * @return
	 */
	public MemberFunc getEquivalentFunc(MemberFunc memberFunc) {
		for(MemberFunc intMemberFunc : getMemberFuncList()) {
			if(memberFunc.isDeclarationEquivalent(intMemberFunc))
				return intMemberFunc;
		}
		
		for(ClassDecl parentClass : getParentClasses()) {
			MemberFunc mfInParent = parentClass.getEquivalentFunc(memberFunc);
			if(mfInParent != null)
				return mfInParent;
		}
		
		return null;
	}
	
	public MemberFunc getOpFunc(int operator) {
		return _opOverloadMethods.get(operator);
	}
	
	public abstract Iterable<ClassDecl> getParentClasses();
	protected abstract Iterable<MemberFunc> getMemberFuncList();
}
