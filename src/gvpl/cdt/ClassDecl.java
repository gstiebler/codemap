package gvpl.cdt;

import gvpl.common.ClassMember;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.common.MemberId;
import gvpl.common.TypeId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;

public class ClassDecl {

	private TypeId _typeId;
	private IBinding _binding;
	private String _name;
	private AstInterpreterCDT _astInterpreter;

	private Map<IBinding, ClassMember> _memberIdMap = new LinkedHashMap<IBinding, ClassMember>();
	private Map<IBinding, MemberFunc> _memberFuncIdMap = new LinkedHashMap<IBinding, MemberFunc>();
	private Map<MemberId, ClassMember> _memberVarGraphNodes = new LinkedHashMap<MemberId, ClassMember>();
	private List<ClassDecl> _parentClasses = new ArrayList<ClassDecl>();

	private MemberFunc _constructorFunc = null;

	public ClassDecl(AstInterpreterCDT astInterpreter) {
		_typeId = new TypeId();
		_astInterpreter = astInterpreter;
	}
	
	public void loadAstDecl(CPPASTCompositeTypeSpecifier classDecl) {
		loadBaseClasses(classDecl.getBaseSpecifiers(), _astInterpreter);

		IASTName name = classDecl.getName();
		_binding = name.resolveBinding();
		_name = name.toString();

		IASTDeclaration[] members = classDecl.getMembers();

		// load every field
		for (IASTDeclaration member : members) {
			if (!(member instanceof IASTSimpleDeclaration))
				continue;

			IASTSimpleDeclaration simple_decl = (IASTSimpleDeclaration) member;
			IASTDeclSpecifier decl_spec = simple_decl.getDeclSpecifier();
			TypeId param_type = _astInterpreter.getType(decl_spec);
			IASTDeclarator[] declarators = simple_decl.getDeclarators();
			// for each variable declared in a line
			for (IASTDeclarator declarator : declarators) {
				if (declarator instanceof IASTFunctionDeclarator) {
					//Load function declaration
					CPPASTFunctionDeclarator funcDeclarator = (CPPASTFunctionDeclarator) declarator;
					loadMemberFuncDecl(funcDeclarator, _astInterpreter);	
				} else {
					IASTName decl_name = declarator.getName();
					MemberId member_id = new MemberId();

					//TODO insert the correct IndirectionType
					ClassMember struct_member = new ClassMember(member_id, decl_name.toString(),
							param_type, IndirectionType.E_VARIABLE);
					addMember(struct_member);

					_memberIdMap.put(decl_name.resolveBinding(), struct_member);
				}
			}
		}

		for (IASTDeclaration member : members) {
			if (!(member instanceof IASTFunctionDefinition))
				continue;

			loadMemberFunc((IASTFunctionDefinition) member, _astInterpreter);
		}
	}
	
	void loadBaseClasses(ICPPASTBaseSpecifier[] baseSpecs, AstInterpreterCDT astInterpreter) {
		for(ICPPASTBaseSpecifier baseSpec : baseSpecs) {
			IBinding binding = baseSpec.getName().resolveBinding();
			TypeId type = astInterpreter.getTypeFromBinding(binding);
			ClassDecl parentClass = astInterpreter.getClassDecl(type);
			_parentClasses.add(parentClass);
		}
	}
	
	private MemberFunc loadMemberFuncDecl(CPPASTFunctionDeclarator funcDeclarator, AstInterpreterCDT astInterpreter) {
		int startingLine = funcDeclarator.getFileLocation().getStartingLineNumber();

		IBinding memberFuncBinding = funcDeclarator.getName().resolveBinding();
		MemberFunc memberFunc = _memberFuncIdMap.get(memberFuncBinding);
		// check if the function declaration has already been loaded
		if(memberFunc == null) {
			memberFunc = new MemberFunc(this, astInterpreter, memberFuncBinding, startingLine);
			memberFunc.loadDeclaration(funcDeclarator, startingLine);
		}
		_memberFuncIdMap.put(memberFuncBinding, memberFunc);
		
		return memberFunc;
	}

	public void loadMemberFunc(IASTFunctionDefinition member, AstInterpreterCDT astInterpreter) {
		IASTDeclarator declarator = member.getDeclarator();
		CPPASTFunctionDeclarator funcDeclarator = (CPPASTFunctionDeclarator) declarator;
		
		MemberFunc memberFunc = loadMemberFuncDecl(funcDeclarator, astInterpreter);
		memberFunc.loadDefinition(funcDeclarator.getConstructorChain(), member.getBody());
	}

	public TypeId getTypeId() {
		return _typeId;
	}

	public IBinding getBinding() {
		return _binding;
	}

	public ClassMember getMember(IBinding binding) {
		ClassMember member = _memberIdMap.get(binding);
		if(member != null)
			return member;
		
		for(ClassDecl parent : _parentClasses) {
			member = parent.getMember(binding);
			if(member != null)
				return member;
		}
		
		return null;
	}

	public MemberFunc getMemberFunc(IBinding binding) {
		MemberFunc memberFunc = _memberFuncIdMap.get(binding);
		if(memberFunc != null)
			return memberFunc;
		
		for(MemberFunc memberF : _memberFuncIdMap.values()) {
			MemberFunc parentMemberFunc = memberF.getParentMemberFunc();
			if(parentMemberFunc == null)
				continue;
			
			if(parentMemberFunc.getBinding() == binding)
				return memberF;
		}
		
		for(ClassDecl parent : _parentClasses) {
			memberFunc = parent.getMemberFunc(binding);
			if(memberFunc != null)
				return memberFunc;
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param memberFunc
	 * @return
	 */
	public MemberFunc getEquivalentFunc(MemberFunc memberFunc) {
		for(MemberFunc intMemberFunc : _memberFuncIdMap.values()) {
			if(memberFunc.isDeclarationEquivalent(intMemberFunc))
				return intMemberFunc;
		}
		
		for(ClassDecl parentClass : _parentClasses) {
			MemberFunc mfInParent = parentClass.getEquivalentFunc(memberFunc);
			if(mfInParent != null)
				return mfInParent;
		}
		
		return null;
	}

	public String getName() {
		return _name;
	}

	public void addMember(ClassMember structMember) {
		_memberVarGraphNodes.put(structMember.getMemberId(), structMember);
	}

	public Iterable<Map.Entry<MemberId, ClassMember>> getMembersMap() {
		return _memberVarGraphNodes.entrySet();
	}

	public void setConstructorFunc(MemberFunc constructorFunc) {
		_constructorFunc = constructorFunc;
	}

	public MemberFunc getConstructorFunc() {
		return _constructorFunc;
	}
	
	private Map<MemberId, ClassMember> getAllMembersIds() {
		Map<MemberId, ClassMember> allMembers = new LinkedHashMap<MemberId, ClassMember>();
		allMembers.putAll(_memberVarGraphNodes);
		
		for(ClassDecl parentClass : _parentClasses) 
			allMembers.putAll(parentClass.getAllMembersIds());
		
		return allMembers;
	}
	
	public Set<MemberId> getMemberIds() {
		return getAllMembersIds().keySet();
	}
	
	public Iterable<ClassDecl> getParentClasses() {
		return _parentClasses;
	}
}
