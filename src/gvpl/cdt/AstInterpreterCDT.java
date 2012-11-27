package gvpl.cdt;

import gvpl.cdt.function.Function;
import gvpl.common.AstInterpreter;
import gvpl.common.ClassMember;
import gvpl.common.CodeLocation;
import gvpl.common.MemberId;
import gvpl.common.TypeId;
import gvpl.graph.Graph;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBaseDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;

import debug.DebugOptions;

public class AstInterpreterCDT extends AstInterpreter {
	
	static Logger logger = LogManager.getLogger(Graph.class.getName());

	private Map<IBinding, Function> _funcIdMap;
	private Map<CodeLocation, Function> _funcByLocation = new TreeMap<CodeLocation, Function>();
	private Map<IBinding, ClassDeclCDT> _typeBindingToClass;
	private Map<CodeLocation, ClassDeclCDT> _classByLocation = new TreeMap<CodeLocation, ClassDeclCDT>();
	
	public AstInterpreterCDT(Graph gvplGraph) {
		super(gvplGraph);
		CppMaps.initialize();
	}
	
	public void execute(IASTTranslationUnit root) {
		_funcIdMap = new LinkedHashMap<IBinding, Function>();
		_typeBindingToClass = new LinkedHashMap<IBinding, ClassDeclCDT>();
		
		IASTDeclaration[] declarations = root.getDeclarations();
		Function mainFunction = null;

		// Iterate through function, class e structs declarations
		for (IASTDeclaration declaration : declarations) {
			logger.debug("Location of declaration: {}", CodeLocationCDT.NewFromFileLocation(declaration.getFileLocation()));
			
			// If the declaration is a function
			if (declaration instanceof IASTFunctionDefinition) {
				Function func = loadFunction((IASTFunctionDefinition) declaration);
				if (func != null)
					mainFunction = func;
			} // if it's a class/struct
			else if (declaration instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
				DebugOptions.setStartingLine(declaration.getFileLocation().getStartingLineNumber());
				IASTDeclSpecifier declSpec = simpleDecl.getDeclSpecifier();
				logger.debug(declSpec.getClass());

				if(declSpec instanceof CPPASTCompositeTypeSpecifier) {
					loadClassImplementation((CPPASTCompositeTypeSpecifier) declSpec);
				} else if(declSpec instanceof CPPASTElaboratedTypeSpecifier) {
					loadClassDecl((CPPASTElaboratedTypeSpecifier) declSpec);
				} else if(declSpec instanceof CPPASTSimpleDeclSpecifier) {
					IASTDeclarator[] declarators = simpleDecl.getDeclarators();
					if(declarators.length > 1)
						logger.fatal("you're doing it wrong");
					
					loadFunctionDeclaration((CPPASTFunctionDeclarator) declarators[0]);
				} else
					logger.fatal("you're doing it wrong. {}", declSpec.getClass());
			} else
				logger.fatal("Deu merda aqui." + declaration.getClass());
		}

		if(mainFunction != null)
			_gvplGraph = mainFunction.getGraph();
	}

	/**
	 * Loads a function from the AST
	 * 
	 * @param funcDefinition
	 * @return A instance of Function
	 */
	private Function loadFunction(IASTFunctionDefinition funcDefinition) {
		IASTDeclarator declarator = funcDefinition.getDeclarator();
		IASTName name = declarator.getName();

		// method function
		if (name instanceof CPPASTQualifiedName) {
			loadMethod(funcDefinition, (CPPASTQualifiedName) name);
		} // a function that is not a method
		else if (name instanceof CPPASTName) {
			Function function = loadSimpleFunction(name, (CPPASTFunctionDeclarator) declarator, funcDefinition);
			if (function.getName().equals("main"))
				return function;
		} else
			logger.fatal("Problem");

		return null;
	}

	private void loadMethod(IASTFunctionDefinition declaration, CPPASTQualifiedName qn) {
		IASTName[] names = qn.getNames();
		IASTName className = names[0];
		IBinding classBinding = className.resolveBinding();
		ClassDeclCDT classDecl = _typeBindingToClass.get(classBinding);
		classDecl.loadMemberFunc(declaration, this);
	}
	
	private Function loadFunctionDeclaration(CPPASTFunctionDeclarator decl) {
		CodeLocation funcLocation = CodeLocationCDT.NewFromFileLocation(decl.getFileLocation());
		IBinding binding = decl.getName().resolveBinding();
		Function function = _funcIdMap.get(binding);
		if(function != null)
			return function;
		
		function = _funcByLocation.get(funcLocation);
		// the function declaration has already been loaded by other .h file
		if(function != null) {
			_funcIdMap.put(binding, function);
			return function;
		}
		
		function = new Function(_gvplGraph, this, this, binding);
		_funcIdMap.put(binding, function);
		_funcByLocation.put(funcLocation, function);
		function.loadDeclaration(decl);
		
		return function;
	}
	
	private Function loadSimpleFunction(IASTName name, CPPASTFunctionDeclarator funcDeclarator, IASTFunctionDefinition funcDefinition) {
		DebugOptions.setStartingLine(funcDeclarator.getFileLocation().getStartingLineNumber());
		Function function = loadFunctionDeclaration(funcDeclarator);

		DebugOptions.setStartingLine(funcDeclarator.getFileLocation().getStartingLineNumber());
		function.loadDefinition(funcDeclarator.getConstructorChain(), funcDefinition.getBody());
		return function;
	}
	
	public void addClassDeclInMaps(ClassDeclCDT classDecl, IBinding binding) {
		_typeBindingToClass.put(binding, classDecl);
		_typeIdToClass.put(classDecl.getTypeId(), classDecl);
		_classByLocation.put(classDecl.getCodeLocation(), classDecl);
	}
	
	private ClassDeclCDT loadClassDecl(CPPASTBaseDeclSpecifier strDecl) {
		IASTName name = null;
		if(strDecl instanceof CPPASTCompositeTypeSpecifier)
			name = ((CPPASTCompositeTypeSpecifier)strDecl).getName();
		else if(strDecl instanceof CPPASTElaboratedTypeSpecifier)
			name = ((CPPASTElaboratedTypeSpecifier)strDecl).getName();
		else
			logger.fatal("you're doing it wrong. {}", strDecl.getClass());
		
		IBinding binding = name.resolveBinding();
		ClassDeclCDT classDecl = _typeBindingToClass.get(binding);
		if(classDecl != null)
			return classDecl;
		
		CodeLocation classLocation = CodeLocationCDT.NewFromFileLocation(strDecl.getFileLocation());
		classDecl = _classByLocation.get(classLocation);
		// the class declaration has already been loaded by other .h file
		if(classDecl != null) {
			addClassDeclInMaps(classDecl, binding);
			return classDecl;
		}
		
		classDecl = new ClassDeclCDT(this, classLocation);
		classDecl.setBinding(name);
		addClassDeclInMaps(classDecl, binding);
		return classDecl;
	}

	/**
	 * Loads a class or structure from the AST
	 * 
	 * @param strDecl
	 */
	private void loadClassImplementation(CPPASTCompositeTypeSpecifier strDecl) {		
		CodeLocation classLocation = CodeLocationCDT.NewFromFileLocation(strDecl.getFileLocation());
		ClassDeclCDT classDecl = _classByLocation.get(classLocation);
		if(classDecl == null) {
			classDecl = loadClassDecl(strDecl);
			classDecl.loadAstDecl(strDecl);
		} else {
			addClassDeclInMaps(classDecl, strDecl.getName().resolveBinding());
			classDecl.updateBindings(strDecl);
		}
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
			ClassDeclCDT classDecl = _typeBindingToClass.get(binding);
			return classDecl.getTypeId();
		} else
			return _primitiveType;
	}
	
	public ClassDeclCDT getClassDecl(IBinding binding) {
		return _typeBindingToClass.get(binding);
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
		ClassDeclCDT loadStruct = _typeIdToClass.get(typeId);
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
	public TypeId getTypeFromBinding(IBinding binding) {
		ClassDeclCDT classDecl = _typeBindingToClass.get(binding);
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
	public ClassDeclCDT getClassFromFuncBinding(IBinding funcBinding) {
		for (ClassDeclCDT classDecl : _typeIdToClass.values()) {
			if (classDecl.getMemberFunc(funcBinding) != null)
				return classDecl;
		}

		return null;
	}

	public Function getFuncId(IBinding binding) {
		return _funcIdMap.get(binding);
	}
}
