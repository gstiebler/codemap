package gvpl.cdt;

import gvpl.common.ClassMember;
import gvpl.common.ErrorOutputter;
import gvpl.common.MemberId;
import gvpl.common.TypeId;
import gvpl.graph.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class ClassDecl {

	private TypeId _typeId;
	private IBinding _binding;
	private String _name;

	private Map<IBinding, ClassMember> _memberIdMap = new HashMap<IBinding, ClassMember>();
	private Map<IBinding, MemberFunc> _memberFuncIdMap = new HashMap<IBinding, MemberFunc>();
	private Map<MemberId, ClassMember> _memberVarGraphNodes;

	private MemberFunc _constructorFunc = null;

	public ClassDecl(Graph gvplGraph, AstLoader parent, AstInterpreter astInterpreter,
			IASTCompositeTypeSpecifier classDecl) {
		int startingLine = classDecl.getFileLocation().getStartingLineNumber();

		_typeId = new TypeId();
		_memberVarGraphNodes = new HashMap<MemberId, ClassMember>();

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

				ClassMember struct_member = new ClassMember(member_id, decl_name.toString(),
						param_type);
				addMember(struct_member);

				_memberIdMap.put(decl_name.resolveBinding(), struct_member);
			}
		}

		for (IASTDeclaration member : members) {
			if (!(member instanceof IASTFunctionDefinition))
				continue;

			loadMemberFunc(member, astInterpreter, startingLine);
		}
	}

	public void loadMemberFunc(IASTDeclaration member, AstInterpreter astInterpreter,
			int startingLine) {
		IASTFunctionDefinition func_def = (IASTFunctionDefinition) member;
		MemberFunc memberFunc = new MemberFunc(this, astInterpreter, startingLine);
		IBinding member_func_binding = memberFunc.load(func_def);

		_memberFuncIdMap.put(member_func_binding, memberFunc);
	}

	public TypeId getTypeId() {
		return _typeId;
	}

	IBinding getBinding() {
		return _binding;
	}

	public ClassMember getMember(IBinding binding) {
		return _memberIdMap.get(binding);
	}

	public MemberFunc getMemberFunc(IBinding binding) {
		return _memberFuncIdMap.get(binding);
	}

	public List<ClassMember> getMembers() {
		return new ArrayList<ClassMember>(_memberIdMap.values());
	}

	public String getName() {
		return _name;
	}

	public void addMember(ClassMember structMember) {
		_memberVarGraphNodes.put(structMember.getMemberId(), structMember);
	}

	public Iterable<Map.Entry<MemberId, ClassMember>> getMemberVarGraphNodes() {
		return _memberVarGraphNodes.entrySet();
	}

	public void setConstructorFunc(MemberFunc constructorFunc) {
		_constructorFunc = constructorFunc;
	}

	public MemberFunc getConstructorFunc() {
		return _constructorFunc;
	}
}
