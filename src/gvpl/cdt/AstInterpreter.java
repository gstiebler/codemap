package gvpl.cdt;

import gvpl.common.ClassMember;
import gvpl.common.ErrorOutputter;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphBuilder.TypeId;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;

public class AstInterpreter extends AstLoader {

	private Map<IBinding, ClassDecl> _typeBindingToClass = new HashMap<IBinding, ClassDecl>();
	private Map<TypeId, ClassDecl> _typeIdToClass = new HashMap<TypeId, ClassDecl>();
	private Map<IBinding, Function> _funcIdMap = new HashMap<IBinding, Function>();

	public AstInterpreter(GraphBuilder graph_builder, IASTTranslationUnit root) {
		super(graph_builder, null, null);
		CppMaps.initialize();
		
		IASTDeclaration[] declarations = root.getDeclarations();
		
		Function mainFunction = null;

		for (IASTDeclaration declaration : declarations) {
			if (declaration instanceof IASTFunctionDefinition) {
				Function func = loadFunction((IASTFunctionDefinition) declaration);
				if(func != null)
					mainFunction = func;
			}
			else if (declaration instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simple_decl = (IASTSimpleDeclaration) declaration;
				loadStructureDecl((IASTCompositeTypeSpecifier) simple_decl.getDeclSpecifier());
			} else
				ErrorOutputter.fatalError("Deu merda aqui." + declaration.getClass());
		}
		
		_graphBuilder._gvplGraph = mainFunction.getGraphBuilder()._gvplGraph;
	}
	
	private Function loadFunction(IASTFunctionDefinition declaration) {
		IASTDeclarator declarator = declaration.getDeclarator();
		IASTName name = declarator.getName();
		
		if(name instanceof CPPASTQualifiedName) {
			int startingLine = declaration.getFileLocation().getStartingLineNumber();
			CPPASTQualifiedName qn = (CPPASTQualifiedName) name;
			IASTName[] names = qn.getNames();
			IASTName className = names[0];
			IBinding classBinding = className.resolveBinding();
			ClassDecl classDecl = _typeBindingToClass.get(classBinding);
			classDecl.loadMemberFunc(declaration, this, startingLine);
		} else if (name instanceof CPPASTName) {	
			Function loadFunction = new Function(_graphBuilder, this, this);
			IBinding binding = loadFunction.load(declaration);
			_funcIdMap.put(binding, loadFunction);
			
			if(loadFunction.getName().equals("main"))
				return loadFunction;
		} else
			ErrorOutputter.fatalError("Problem");
		
		return null;
	}

	private void addClass(ClassDecl classDecl) {
		_typeBindingToClass.put(classDecl.getBinding(), classDecl);
		_typeIdToClass.put(classDecl.getTypeId(), classDecl);
	}

	private void loadStructureDecl(IASTCompositeTypeSpecifier strDecl) {
		ClassDecl classDecl = new ClassDecl(_graphBuilder, this, this, strDecl);

		addClass(classDecl);
	}

	public TypeId getType(IASTDeclSpecifier decl_spec) {
		if (decl_spec instanceof IASTNamedTypeSpecifier) {
			IASTNamedTypeSpecifier named_type = (IASTNamedTypeSpecifier) decl_spec;
			return _typeBindingToClass.get(named_type.getName().resolveBinding()).getTypeId();
		}

		return null;
	}

	public Function getFuncId(IBinding binding) {
		return _funcIdMap.get(binding);
	}

	public MemberId getMemberId(IBinding member_binding, IBinding type_binding) {
		ClassDecl loadStruct = _typeBindingToClass.get(type_binding);
		ClassMember structMember = loadStruct.getMember(member_binding);
		return structMember.getMemberId();
	}

	public MemberId getMemberId(TypeId type_id, IBinding member_binding) {
		ClassDecl loadStruct = _typeIdToClass.get(type_id);
		ClassMember classMember = loadStruct.getMember(member_binding);
		return classMember.getMemberId();
	}

	public MemberFunc getMemberFunc(IBinding func_member_binding) {
		for (Map.Entry<TypeId, ClassDecl> entry : _typeIdToClass.entrySet()) {
			ClassDecl loadStruct = entry.getValue();
			MemberFunc member_func = loadStruct.getMemberFunc(func_member_binding);
			if (member_func != null)
				return member_func;
		}

		ErrorOutputter.fatalError("Problem here.");
		return null;
	}
	
	public ClassDecl getClassDecl(TypeId type) {
		return _typeIdToClass.get(type);
	}

}
