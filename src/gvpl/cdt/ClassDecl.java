package gvpl.cdt;

import gvpl.common.ClassMember;
import gvpl.common.ErrorOutputter;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.common.MemberId;
import gvpl.common.TypeId;
import gvpl.graph.Graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

	private Map<IBinding, ClassMember> _memberIdMap = new LinkedHashMap<IBinding, ClassMember>();
	private Map<IBinding, MemberFunc> _memberFuncIdMap = new LinkedHashMap<IBinding, MemberFunc>();
	private Map<MemberId, ClassMember> _memberVarGraphNodes;
	private List<ClassDecl> _parentClasses = new ArrayList<ClassDecl>();

	private MemberFunc _constructorFunc = null;

	public ClassDecl(Graph gvplGraph, AstLoader parent, AstInterpreter astInterpreter,
			CPPASTCompositeTypeSpecifier classDecl) {
		int startingLine = classDecl.getFileLocation().getStartingLineNumber();

		_typeId = new TypeId();
		loadBaseClasses(classDecl.getBaseSpecifiers(), astInterpreter);
		_memberVarGraphNodes = new LinkedHashMap<MemberId, ClassMember>();

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
			TypeId param_type = astInterpreter.getType(decl_spec);
			IASTDeclarator[] declarators = simple_decl.getDeclarators();
			// for each variable declared in a line
			for (IASTDeclarator declarator : declarators) {
				if (declarator instanceof IASTFunctionDeclarator) {
					ErrorOutputter.warning("work here?");
					continue;
				}

				IASTName decl_name = declarator.getName();
				MemberId member_id = new MemberId();

				//TODO insert the correct IndirectionType
				ClassMember struct_member = new ClassMember(member_id, decl_name.toString(),
						param_type, IndirectionType.E_VARIABLE);
				addMember(struct_member);

				_memberIdMap.put(decl_name.resolveBinding(), struct_member);
			}
		}

		for (IASTDeclaration member : members) {
			if (!(member instanceof IASTFunctionDefinition)) {
			
				/*IASTSimpleDeclaration simple_decl = (IASTSimpleDeclaration) member;
				IASTDeclSpecifier decl_spec = simple_decl.getDeclSpecifier();
				TypeId param_type = astInterpreter.getType(decl_spec);
				IASTDeclarator[] declarators = simple_decl.getDeclarators();
				
				for (IASTDeclarator declarator : declarators) {
					if (declarator instanceof IASTFunctionDeclarator) {
						IBinding binding = declarator.getName().resolveBinding();
						int x = 5;
						continue;
					}
				}*/
				
				continue;
			}

			loadMemberFunc((IASTFunctionDefinition) member, astInterpreter);
		}
	}
	
	void loadBaseClasses(ICPPASTBaseSpecifier[] baseSpecs, AstInterpreter astInterpreter) {
		for(ICPPASTBaseSpecifier baseSpec : baseSpecs) {
			IBinding binding = baseSpec.getName().resolveBinding();
			TypeId type = astInterpreter.getTypeFromBinding(binding);
			ClassDecl parentClass = astInterpreter.getClassDecl(type);
			_parentClasses.add(parentClass);
		}
	}

	public void loadMemberFunc(IASTFunctionDefinition member, AstInterpreter astInterpreter) {
		int startingLine = member.getFileLocation().getStartingLineNumber();
		MemberFunc memberFunc = new MemberFunc(this, astInterpreter, startingLine);

		IASTDeclarator declarator = member.getDeclarator();
		CPPASTFunctionDeclarator funcDeclarator = (CPPASTFunctionDeclarator) declarator;
		IBinding memberFuncBinding = memberFunc.loadDeclaration(funcDeclarator, startingLine);
		memberFunc.loadDefinition(funcDeclarator.getConstructorChain(), member.getBody());

		_memberFuncIdMap.put(memberFuncBinding, memberFunc);
	}

	public TypeId getTypeId() {
		return _typeId;
	}

	IBinding getBinding() {
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
		
		for(ClassDecl parent : _parentClasses) {
			memberFunc = parent.getMemberFunc(binding);
			if(memberFunc != null)
				return memberFunc;
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
	
	public Iterable<MemberId> getMemberIds() {
		return _memberVarGraphNodes.keySet();
	}
	
	public Iterable<ClassDecl> getParentClasses() {
		return _parentClasses;
	}
}
