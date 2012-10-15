package gvpl.cdt;

import gvpl.common.ClassMember;
import gvpl.common.ErrorOutputter;
import gvpl.common.MemberId;
import gvpl.common.TypeId;
import gvpl.graph.Graph;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;

public class AstInterpreter extends AstLoader {

	private Map<IBinding, ClassDecl> _typeBindingToClass = new LinkedHashMap<IBinding, ClassDecl>();
	private Map<TypeId, ClassDecl> _typeIdToClass = new LinkedHashMap<TypeId, ClassDecl>();
	private Map<IBinding, Function> _funcIdMap = new LinkedHashMap<IBinding, Function>();
	private TypeId _primitiveType = new TypeId();//the same for all primitive types

	public AstInterpreter(Graph gvplGraph, IASTTranslationUnit root) {
		super(gvplGraph, null, null);
		CppMaps.initialize();

		IASTDeclaration[] declarations = root.getDeclarations();

		Function mainFunction = null;

		for (IASTDeclaration declaration : declarations) {
			if (declaration instanceof IASTFunctionDefinition) {
				Function func = loadFunction((IASTFunctionDefinition) declaration);
				if (func != null)
					mainFunction = func;
			} else if (declaration instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simple_decl = (IASTSimpleDeclaration) declaration;
				loadStructureDecl((CPPASTCompositeTypeSpecifier) simple_decl.getDeclSpecifier());
			} else
				ErrorOutputter.fatalError("Deu merda aqui." + declaration.getClass());
		}

		_gvplGraph = mainFunction.getGraph();
	}

	private Function loadFunction(IASTFunctionDefinition declaration) {
		int startingLine = declaration.getFileLocation().getStartingLineNumber();
		IASTDeclarator declarator = declaration.getDeclarator();
		IASTName name = declarator.getName();

		if (name instanceof CPPASTQualifiedName) {
			CPPASTQualifiedName qn = (CPPASTQualifiedName) name;
			IASTName[] names = qn.getNames();
			IASTName className = names[0];
			IBinding classBinding = className.resolveBinding();
			ClassDecl classDecl = _typeBindingToClass.get(classBinding);
			classDecl.loadMemberFunc(declaration, this);
		} else if (name instanceof CPPASTName) {
			CPPASTFunctionDeclarator funcDeclarator = (CPPASTFunctionDeclarator) declarator;
			IBinding binding = funcDeclarator.getName().resolveBinding(); 
			Function function = new Function(_gvplGraph, this, this, binding);
			
			function.loadDeclaration(funcDeclarator, startingLine);
			function.loadDefinition(funcDeclarator.getConstructorChain(), declaration.getBody());
			_funcIdMap.put(binding, function);

			if (function.getName().equals("main"))
				return function;
		} else
			ErrorOutputter.fatalError("Problem");

		return null;
	}

	private void addClass(ClassDecl classDecl) {
		_typeBindingToClass.put(classDecl.getBinding(), classDecl);
		_typeIdToClass.put(classDecl.getTypeId(), classDecl);
	}

	private void loadStructureDecl(CPPASTCompositeTypeSpecifier strDecl) {
		ClassDecl classDecl = new ClassDecl(_gvplGraph, this, this, strDecl);

		addClass(classDecl);
	}

	public TypeId getType(IASTDeclSpecifier declSpec) {
		if (declSpec instanceof IASTNamedTypeSpecifier) {
			IASTNamedTypeSpecifier named_type = (IASTNamedTypeSpecifier) declSpec;
			IBinding binding = named_type.getName().resolveBinding();
			ClassDecl classDecl = _typeBindingToClass.get(binding);
			return classDecl.getTypeId();
		} else
			return _primitiveType;
	}

	public Function getFuncId(IBinding binding) {
		return _funcIdMap.get(binding);
	}

	public MemberId getMemberId(IBinding memberBinding, IBinding typeBinding) {
		ClassDecl loadStruct = _typeBindingToClass.get(typeBinding);
		ClassMember structMember = loadStruct.getMember(memberBinding);
		return structMember.getMemberId();
	}

	public MemberId getMemberId(TypeId typeId, IBinding memberBinding) {
		ClassDecl loadStruct = _typeIdToClass.get(typeId);
		ClassMember classMember = loadStruct.getMember(memberBinding);
		return classMember.getMemberId();
	}

	public ClassDecl getClassDecl(TypeId type) {
		return _typeIdToClass.get(type);
	}
	
	TypeId getTypeFromBinding(IBinding binding) {
		ClassDecl classDecl =  _typeBindingToClass.get(binding);
		if(classDecl == null)
			return null;
		else
			return classDecl.getTypeId();
	}
	
	public ClassDecl getClassFromFuncBinding(IBinding funcBinding) {
		for(ClassDecl classDecl : _typeIdToClass.values()) {
			if(classDecl.getMemberFunc(funcBinding) != null)
				return classDecl;
		}
		
		return null;
	}
	
	public boolean isPrimitiveType(TypeId type) {
		return type.equals(_primitiveType);
	}
	
	public TypeId getPrimitiveType() {
		return _primitiveType;
	}

}
