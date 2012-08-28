package gvpl.cdt;

import gvpl.common.ErrorOutputter;
import gvpl.common.Class;
import gvpl.common.ClassMember;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.TypeId;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class AstInterpreter extends AstLoader {

	private Map<IBinding, ClassDecl> _typeBindingToStruct = new HashMap<IBinding, ClassDecl>();
	private Map<TypeId, ClassDecl> _typeIdToStruct = new HashMap<TypeId, ClassDecl>();
	private Map<IBinding, Function> _func_id_map = new HashMap<IBinding, Function>();
	

	private Map<TypeId, Class> _struct_graph_nodes = new HashMap<TypeId, Class>();

	public AstInterpreter(GraphBuilder graph_builder, IASTTranslationUnit root) {
		super(graph_builder, null, new CppMaps(), null);
		
		IASTDeclaration[] declarations = root.getDeclarations();
		
		Function mainFunction = null;

		for (IASTDeclaration declaration : declarations) {
			if (declaration instanceof IASTFunctionDefinition) {
				Function loadFunction = new Function(_graphBuilder, this, _cppMaps, this);
				IBinding binding = loadFunction.load((IASTFunctionDefinition) declaration);
				_func_id_map.put(binding, loadFunction);
				
				if(loadFunction.getName().equals("main"))
					mainFunction = loadFunction;
			}
			else if (declaration instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simple_decl = (IASTSimpleDeclaration) declaration;
				loadStructureDecl((IASTCompositeTypeSpecifier) simple_decl.getDeclSpecifier());
			} else
				ErrorOutputter.fatalError("Deu merda aqui." + declaration.getClass());
		}
		
		_graphBuilder._gvplGraph = mainFunction.getGraphBuilder()._gvplGraph;
	}

	private void addStruct(ClassDecl structLoader) {
		_typeBindingToStruct.put(structLoader.getBinding(), structLoader);
		_typeIdToStruct.put(structLoader.getTypeId(), structLoader);
	}

	private void loadStructureDecl(IASTCompositeTypeSpecifier strDecl) {
		ClassDecl structLoader = new ClassDecl(_graphBuilder, this, _cppMaps, this, strDecl);

		addStruct(structLoader);
		addStructDecl(structLoader.getStructDecl());
	}

	public TypeId getType(IASTDeclSpecifier decl_spec) {
		if (decl_spec instanceof IASTNamedTypeSpecifier) {
			IASTNamedTypeSpecifier named_type = (IASTNamedTypeSpecifier) decl_spec;
			return _typeBindingToStruct.get(named_type.getName().resolveBinding()).getTypeId();
		}

		return null;
	}

	public Function getFuncId(IBinding binding) {
		return _func_id_map.get(binding);
	}

	public MemberId getMemberId(IBinding member_binding, IBinding type_binding) {
		ClassDecl loadStruct = _typeBindingToStruct.get(type_binding);
		ClassMember structMember = loadStruct.getMember(member_binding);
		return structMember.getMemberId();
	}

	public MemberId getMemberId(TypeId type_id, IBinding member_binding) {
		ClassDecl loadStruct = _typeIdToStruct.get(type_id);
		ClassMember structMember = loadStruct.getMember(member_binding);
		return structMember.getMemberId();
	}

	public MemberFunc getMemberFunc(IBinding func_member_binding) {
		for (Map.Entry<TypeId, ClassDecl> entry : _typeIdToStruct.entrySet()) {
			ClassDecl loadStruct = entry.getValue();
			MemberFunc member_func = loadStruct.getMemberFunc(func_member_binding);
			if (member_func != null)
				return member_func;
		}

		ErrorOutputter.fatalError("Problem here.");
		return null;
	}

	public void addStructDecl(Class struct_decl) {
		_struct_graph_nodes.put(struct_decl._id, struct_decl);
	}
	
	public Class getStructDecl(TypeId type) {
		return _struct_graph_nodes.get(type);
	}

}
