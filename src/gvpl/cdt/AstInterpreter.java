package gvpl.cdt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gvpl.ErrorOutputter;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.*;
import gvpl.graph.GraphNode;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;

public class AstInterpreter implements AstLoader {

	GraphBuilder _graph_builder;
	
	CppMaps _cppMaps;

	private Map<IBinding, VarId> _var_id_map = new HashMap<IBinding, VarId>();
	private Map<IBinding, FuncId> _func_id_map = new HashMap<IBinding, FuncId>();
	private Map<IBinding, TypeId> _type_id_map = new HashMap<IBinding, TypeId>();
	private Map<IBinding, MemberId> _member_id_map = new HashMap<IBinding, MemberId>();

	public AstInterpreter(GraphBuilder graph_builder, IASTTranslationUnit root) {
		_graph_builder = graph_builder;

		_cppMaps = new CppMaps();

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

	private void loadStructureDecl(IASTCompositeTypeSpecifier strDecl) {
		IASTName name = strDecl.getName();
		IASTDeclaration[] members = strDecl.getMembers();

		TypeId struct_type = _graph_builder.new TypeId();
		_type_id_map.put(name.resolveBinding(), struct_type);

		StructDecl struct_decl = _graph_builder.new StructDecl(struct_type, name.toString());

		// for each line of members declaration
		for (IASTDeclaration member : members) {
			if (member instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simple_decl = (IASTSimpleDeclaration) member;
				IASTDeclSpecifier decl_spec = simple_decl.getDeclSpecifier();
				TypeId param_type = getType(decl_spec);
				IASTDeclarator[] declarators = simple_decl.getDeclarators();
				// for each variable declared in a line
				for (IASTDeclarator declarator : declarators) {
					IASTName decl_name = declarator.getName();
					MemberId member_id = _graph_builder.new MemberId();

					StructMember struct_member = _graph_builder.new StructMember(struct_decl, member_id,
							decl_name.toString(), param_type);
					struct_decl.addMember(struct_member);
					
					_member_id_map.put(decl_name.resolveBinding(), member_id);
				}
			} else if (member instanceof IASTFunctionDefinition) {
				IASTFunctionDefinition func_def = (IASTFunctionDefinition) member;
				load_function(func_def); 
			}
		}

		_graph_builder.addStructDecl(struct_decl);
	}

	public TypeId getType(IASTDeclSpecifier decl_spec) {
		if (decl_spec instanceof IASTNamedTypeSpecifier) {
			IASTNamedTypeSpecifier named_type = (IASTNamedTypeSpecifier) decl_spec;
			return _type_id_map.get(named_type.getName().resolveBinding());
		}

		return null;
	}

	private void load_function(IASTFunctionDefinition fd) {
		LoadBasicBlock basicBlockLoader = new LoadBasicBlock(_graph_builder, this, _cppMaps);
		
		CPPASTFunctionDeclarator decl = (CPPASTFunctionDeclarator) fd.getDeclarator();
		IASTParameterDeclaration[] parameters = decl.getParameters();
		IASTName name_binding = decl.getName();
		String function_name = name_binding.toString();

		FuncId func_id = _graph_builder.new FuncId();
		_func_id_map.put(name_binding.resolveBinding(), func_id);
		FuncDecl func_decl = _graph_builder.new FuncDecl(func_id, function_name);
		for (IASTParameterDeclaration parameter : parameters) {
			IASTDeclarator parameter_var_decl = parameter.getDeclarator();
			IASTDeclSpecifier decl_spec = parameter.getDeclSpecifier();
			TypeId type = getType(decl_spec);
			DirectVarDecl var_decl = basicBlockLoader.load_var_decl(parameter_var_decl, type);
			func_decl._parameters.add(var_decl);
		}
		_graph_builder.enter_function(func_decl);

		IASTStatement body = fd.getBody();

		// TODO tratar o else
		if (body instanceof IASTCompoundStatement){
			basicBlockLoader.load((IASTCompoundStatement) body);
		}

		_graph_builder.decrease_depth();
	}

	public VarDecl getVarDecl(IASTExpression expr) {
		if (expr instanceof IASTIdExpression) {
			IASTIdExpression id_expr = (IASTIdExpression) expr;
			IBinding binding = id_expr.getName().resolveBinding();
			VarId lhs_var_id = _var_id_map.get(binding);

			return _graph_builder.find_var(lhs_var_id);
		} else if (expr instanceof IASTFieldReference) {
			CPPASTFieldReference field_ref = (CPPASTFieldReference) expr;
			IASTIdExpression owner = (IASTIdExpression) field_ref.getFieldOwner();

			IBinding field_binding = field_ref.getFieldName().resolveBinding();
			MemberId member_id = _member_id_map.get(field_binding);

			IBinding owner_binding = owner.getName().resolveBinding();
			VarId var_id = _var_id_map.get(owner_binding);
			
			return _graph_builder.findMember(var_id, member_id);
		} else
			ErrorOutputter.fatalError("Work here " + expr.getClass());

		return null;
	}
	
	public void addVarDecl(IBinding binding, VarId id){
		_var_id_map.put(binding, id);
	}
	
	public FuncId getFuncId(IBinding binding){
		return _func_id_map.get(binding);
	}

}
