package gvpl.cdt;

import gvpl.ErrorOutputter;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;

public class AstInterpreter extends AstLoader {

	private Map<IBinding, LoadStruct> _typeBindingToStruct = new HashMap<IBinding, LoadStruct>();
	private Map<TypeId, LoadStruct> _typeIdToStruct = new HashMap<TypeId, LoadStruct>();
	private Map<IBinding, FuncId> _func_id_map = new HashMap<IBinding, FuncId>();

	public AstInterpreter(GraphBuilder graph_builder, IASTTranslationUnit root) {
		super(graph_builder, null, new CppMaps(), null);

		IASTDeclaration[] declarations = root.getDeclarations();

		for (IASTDeclaration declaration : declarations) {
			if (declaration instanceof IASTFunctionDefinition)
				load_function((IASTFunctionDefinition) declaration);
			else if (declaration instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simple_decl = (IASTSimpleDeclaration) declaration;
				loadStructureDecl((IASTCompositeTypeSpecifier) simple_decl.getDeclSpecifier());
			} else
				ErrorOutputter.fatalError("Deu merda aqui.");

		}
	}
	
	private void addStruct(LoadStruct structLoader) {
		_typeBindingToStruct.put(structLoader.getBinding(), structLoader);
		_typeIdToStruct.put(structLoader.getTypeId(), structLoader);
	}

	private void loadStructureDecl(IASTCompositeTypeSpecifier strDecl) {
		LoadStruct structLoader = new LoadStruct(_graph_builder, this, _cppMaps, this, strDecl);

		addStruct(structLoader);
		_graph_builder.addStructDecl(structLoader.getStructDecl());
	}

	public TypeId getType(IASTDeclSpecifier decl_spec) {
		if (decl_spec instanceof IASTNamedTypeSpecifier) {
			IASTNamedTypeSpecifier named_type = (IASTNamedTypeSpecifier) decl_spec;
			return _typeBindingToStruct.get(named_type.getName().resolveBinding()).getTypeId();
		}

		return null;
	}

	private void load_function(IASTFunctionDefinition fd) {
		LoadBasicBlock basicBlockLoader = new LoadBasicBlock(this, this);

		CPPASTFunctionDeclarator decl = (CPPASTFunctionDeclarator) fd.getDeclarator();
		IASTParameterDeclaration[] parameters = decl.getParameters();
		IASTName name_binding = decl.getName();
		String function_name = name_binding.toString();

		FuncId func_id = _graph_builder.new FuncId();
		_func_id_map.put(name_binding.resolveBinding(), func_id);
		
		FuncDecl func_decl = _graph_builder.new FuncDecl(func_id, function_name);
		
		loadFuncParameters(parameters, func_decl, basicBlockLoader);

		_graph_builder.enter_function(func_decl);

		IASTStatement body = fd.getBody();

		// TODO tratar o else
		if (body instanceof IASTCompoundStatement) {
			basicBlockLoader.load((IASTCompoundStatement) body);
		}
	}
	
	public void loadFuncParameters(IASTParameterDeclaration[] parameters, FuncDecl func_decl, LoadBasicBlock basicBlockLoader) {
		for (IASTParameterDeclaration parameter : parameters) {
			IASTDeclarator parameter_var_decl = parameter.getDeclarator();
			IASTDeclSpecifier decl_spec = parameter.getDeclSpecifier();
			TypeId type = getType(decl_spec);
			DirectVarDecl var_decl = basicBlockLoader.load_var_decl(parameter_var_decl, type);
			func_decl._parameters.add(var_decl);
		}
	}

	public FuncId getFuncId(IBinding binding) {
		return _func_id_map.get(binding);
	}

	public MemberId getMemberId(IBinding member_binding, IBinding type_binding) {
		LoadStruct loadStruct = _typeBindingToStruct.get(type_binding);
		return loadStruct.getMemberId(member_binding);
	}

	public MemberId getMemberId(TypeId type_id, IBinding member_binding) {
		LoadStruct loadStruct = _typeIdToStruct.get(type_id);
		return loadStruct.getMemberId(member_binding);
	}

}
