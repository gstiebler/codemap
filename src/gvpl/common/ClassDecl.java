package gvpl.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import gvpl.cdt.ClassDeclCDT;
import gvpl.cdt.MemberFunc;


public abstract class ClassDecl {

	protected String _name;
	protected TypeId _typeId;
	protected MemberFunc _constructorFunc = null;
	protected Map<MemberId, ClassMember> _memberVarGraphNodes = new LinkedHashMap<MemberId, ClassMember>();
	
	public ClassDecl() {
		_typeId = new TypeId();
	}

	public TypeId getTypeId() {
		return _typeId;
	}

	public MemberFunc getConstructorFunc() {
		return _constructorFunc;
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
	
	private Map<MemberId, ClassMember> getAllMembersIds() {
		Map<MemberId, ClassMember> allMembers = new LinkedHashMap<MemberId, ClassMember>();
		allMembers.putAll(_memberVarGraphNodes);
		
		for(ClassDecl parentClass : getParentClasses()) 
			allMembers.putAll(parentClass.getAllMembersIds());
		
		return allMembers;
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
	
	public abstract Iterable<ClassDecl> getParentClasses();
	protected abstract Iterable<MemberFunc> getMemberFuncList();
}
