package gvpl.cdt;

import gvpl.common.Class;
import gvpl.common.ClassMember;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.*;

public class ClassDecl extends AstLoader {

	private TypeId _typeId;
	private Class _class;
	private IBinding _binding;

	private Map<IBinding, ClassMember> _member_id_map = new HashMap<IBinding, ClassMember>();
	private Map<IBinding, MemberFunc> _member_func_id_map = new HashMap<IBinding, MemberFunc>();
	//private Map<IBinding, LoadMemberFunc> _

	public ClassDecl(GraphBuilder graph_builder, AstLoader parent, CppMaps cppMaps,
			AstInterpreter astInterpreter, IASTCompositeTypeSpecifier strDecl) {
		super(graph_builder, parent, cppMaps, astInterpreter);

		_typeId = _graphBuilder.new TypeId();

		IASTName name = strDecl.getName();
		IASTDeclaration[] members = strDecl.getMembers();

		_binding = name.resolveBinding();

		_class = new Class(_typeId, name.toString());
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
				IASTName decl_name = declarator.getName();
				MemberId member_id = _graphBuilder.new MemberId();

				ClassMember struct_member = new ClassMember(_class,
						member_id, decl_name.toString(), param_type);
				_class.addMember(struct_member);

				_member_id_map.put(decl_name.resolveBinding(), struct_member);
			}
		}

		for (IASTDeclaration member : members) {
			if (!(member instanceof IASTFunctionDefinition))
				continue;
			
			IASTFunctionDefinition func_def = (IASTFunctionDefinition) member;
			MemberFunc memberFunc = new MemberFunc(this, strDecl.getFileLocation().getStartingLineNumber());
			IBinding member_func_binding = memberFunc.load(func_def);
			
			_member_func_id_map.put(member_func_binding, memberFunc);
		}
	}

	TypeId getTypeId() {
		return _typeId;
	}

	IBinding getBinding() {
		return _binding;
	}

	public Class getStructDecl() {
		return _class;
	}

	public ClassMember getMember(IBinding binding) {
		return _member_id_map.get(binding);
	}

	public MemberFunc getMemberFunc(IBinding binding) {
		return _member_func_id_map.get(binding);
	}
	
	public List<ClassMember> getMembers() {
		return new ArrayList<ClassMember>(_member_id_map.values());
	}

	public String getName() {
		return _class.getName();
	}
}
