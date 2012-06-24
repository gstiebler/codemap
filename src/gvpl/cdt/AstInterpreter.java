package gvpl.cdt;

import gvpl.ErrorOutputter;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.DirectVarDecl;
import gvpl.graph.GraphBuilder.FuncDecl;
import gvpl.graph.GraphBuilder.FuncId;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.StructDecl;
import gvpl.graph.GraphBuilder.StructMember;
import gvpl.graph.GraphBuilder.TypeId;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;

public class AstInterpreter extends AstLoader {

	private Map<IBinding, TypeId> _type_id_map = new HashMap<IBinding, TypeId>();
	private Map<IBinding, MemberId> _member_id_map = new HashMap<IBinding, MemberId>();
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
		LoadBasicBlock basicBlockLoader = new LoadBasicBlock(_graph_builder, this, _cppMaps, this);
		
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
	
	public FuncId getFuncId(IBinding binding){
		return _func_id_map.get(binding);
	}
	
	public MemberId getMemberId(IBinding binding) {
		return _member_id_map.get(binding);
	}

}
