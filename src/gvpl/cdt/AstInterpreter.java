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

/**
 * The class that holds information about the entire software being interpreted
 * 
 * @author stiebler
 * 
 */
public class AstInterpreter extends AstLoader {

	private Map<IBinding, ClassDecl> _typeBindingToClass = new LinkedHashMap<IBinding, ClassDecl>();
	private Map<TypeId, ClassDecl> _typeIdToClass = new LinkedHashMap<TypeId, ClassDecl>();
	private Map<IBinding, Function> _funcIdMap = new LinkedHashMap<IBinding, Function>();
	/** the same for all primitive types */
	private TypeId _primitiveType = new TypeId();

	/**
	 * The base function. It all starts here.
	 * 
	 * @param gvplGraph
	 *            The almight graph
	 * @param root
	 *            The root of the program
	 */
	public AstInterpreter(Graph gvplGraph, IASTTranslationUnit root) {
		super(gvplGraph, null, null);
		CppMaps.initialize();

		IASTDeclaration[] declarations = root.getDeclarations();

		Function mainFunction = null;

		// Iterate through function, class e structs declarations
		for (IASTDeclaration declaration : declarations) {
			// If the declaration is a function
			if (declaration instanceof IASTFunctionDefinition) {
				Function func = loadFunction((IASTFunctionDefinition) declaration);
				if (func != null)
					mainFunction = func;
			} // if it's a class/struct
			else if (declaration instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simple_decl = (IASTSimpleDeclaration) declaration;
				loadStructureDecl((CPPASTCompositeTypeSpecifier) simple_decl.getDeclSpecifier());
			} else
				ErrorOutputter.fatalError("Deu merda aqui." + declaration.getClass());
		}

		_gvplGraph = mainFunction.getGraph();
	}

	/**
	 * Loads a function from the AST
	 * 
	 * @param declaration
	 * @return A instance of Function
	 */
	private Function loadFunction(IASTFunctionDefinition declaration) {
		IASTDeclarator declarator = declaration.getDeclarator();
		IASTName name = declarator.getName();

		// method function
		if (name instanceof CPPASTQualifiedName) {
			loadMethod(declaration, (CPPASTQualifiedName) name);
		} // a function that is not a method
		else if (name instanceof CPPASTName) {
			Function function = loadSimpleFunction(name, (CPPASTFunctionDeclarator) declarator, declaration);
			if (function.getName().equals("main"))
				return function;
		} else
			ErrorOutputter.fatalError("Problem");

		return null;
	}
	
	private void loadMethod(IASTFunctionDefinition declaration, CPPASTQualifiedName qn) {
		IASTName[] names = qn.getNames();
		IASTName className = names[0];
		IBinding classBinding = className.resolveBinding();
		ClassDecl classDecl = _typeBindingToClass.get(classBinding);
		classDecl.loadMemberFunc(declaration, this);
	}
	
	private Function loadSimpleFunction(IASTName name, CPPASTFunctionDeclarator funcDeclarator, IASTFunctionDefinition declaration) {
		int startingLine = funcDeclarator.getFileLocation().getStartingLineNumber();
		IBinding binding = name.resolveBinding();
		Function function = new Function(_gvplGraph, this, this, binding);

		function.loadDeclaration(funcDeclarator, startingLine);
		function.loadDefinition(funcDeclarator.getConstructorChain(), declaration.getBody());
		_funcIdMap.put(binding, function);
		return function;
	}

	/**
	 * Loads a class or structure from the AST
	 * 
	 * @param strDecl
	 */
	private void loadStructureDecl(CPPASTCompositeTypeSpecifier strDecl) {
		ClassDecl classDecl = new ClassDecl(_gvplGraph, this, this, strDecl);

		_typeBindingToClass.put(classDecl.getBinding(), classDecl);
		_typeIdToClass.put(classDecl.getTypeId(), classDecl);
	}

	/**
	 * Gets the id of a type from the declaration in AST
	 * 
	 * @param declSpec
	 * @return The id of the type
	 */
	public TypeId getType(IASTDeclSpecifier declSpec) {
		if (declSpec instanceof IASTNamedTypeSpecifier) {
			IASTNamedTypeSpecifier namedType = (IASTNamedTypeSpecifier) declSpec;
			IBinding binding = namedType.getName().resolveBinding();
			ClassDecl classDecl = _typeBindingToClass.get(binding);
			return classDecl.getTypeId();
		} else
			return _primitiveType;
	}

	/**
	 * Gets the id of the member of a class
	 * 
	 * @param typeBinding
	 *            The type id of the class
	 * @param memberBinding
	 *            The binding of the member
	 * @return The id of the member
	 */
	public MemberId getMemberId(TypeId typeId, IBinding memberBinding) {
		ClassDecl loadStruct = _typeIdToClass.get(typeId);
		ClassMember classMember = loadStruct.getMember(memberBinding);
		return classMember.getMemberId();
	}

	/**
	 * Returns the id of a type from it's binding
	 * 
	 * @param binding
	 *            The binding of the type id
	 * @return The type id from the received binding
	 */
	TypeId getTypeFromBinding(IBinding binding) {
		ClassDecl classDecl = _typeBindingToClass.get(binding);
		if (classDecl == null)
			return null;
		else
			return classDecl.getTypeId();
	}

	/**
	 * Returns the class from it's binding in the AST
	 * 
	 * @param funcBinding
	 * @return The class from it's binding in the AST
	 */
	public ClassDecl getClassFromFuncBinding(IBinding funcBinding) {
		for (ClassDecl classDecl : _typeIdToClass.values()) {
			if (classDecl.getMemberFunc(funcBinding) != null)
				return classDecl;
		}

		return null;
	}

	public Function getFuncId(IBinding binding) {
		return _funcIdMap.get(binding);
	}

	public ClassDecl getClassDecl(TypeId type) {
		return _typeIdToClass.get(type);
	}

	public boolean isPrimitiveType(TypeId type) {
		return type.equals(_primitiveType);
	}

	public TypeId getPrimitiveType() {
		return _primitiveType;
	}

}
