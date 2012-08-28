package gvpl.cdt;

import gvpl.common.ClassMember;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.TypeId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
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

	public ClassDecl(GraphBuilder graph_builder, AstLoader parent, CppMaps cppMaps,
			AstInterpreter astInterpreter, IASTCompositeTypeSpecifier classDecl) {
		int startingLine = classDecl.getFileLocation().getStartingLineNumber();
		
		_typeId = graph_builder.new TypeId();
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
				IASTName decl_name = declarator.getName();
				MemberId member_id = graph_builder.new MemberId();

				ClassMember struct_member = new ClassMember(member_id, decl_name.toString(), param_type);
				addMember(struct_member);

				_memberIdMap.put(decl_name.resolveBinding(), struct_member);
			}
		}

		for (IASTDeclaration member : members) {
			if (!(member instanceof IASTFunctionDefinition))
				continue;
			
			IASTFunctionDefinition func_def = (IASTFunctionDefinition) member;
			MemberFunc memberFunc = new MemberFunc(this, cppMaps, astInterpreter, startingLine);
			IBinding member_func_binding = memberFunc.load(func_def);
			
			_memberFuncIdMap.put(member_func_binding, memberFunc);
		}
	}

	TypeId getTypeId() {
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
}
