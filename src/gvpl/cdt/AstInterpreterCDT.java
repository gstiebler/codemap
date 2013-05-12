package gvpl.cdt;

import gvpl.cdt.function.Function;
import gvpl.common.AstInterpreter;
import gvpl.common.CodeLocation;
import gvpl.common.FuncParameter;
import gvpl.common.IScope;
import gvpl.common.IVar;
import gvpl.common.ScriptManager;
import gvpl.common.TypeId;
import gvpl.graph.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBaseDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUsingDirective;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPField;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTypedef;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;

import debug.DebugOptions;
import debug.ExecTreeLogger;

public class AstInterpreterCDT extends AstInterpreter implements IScope {
	
	class CppFile {
		Map<IBinding, Function> _funcIdMap = new LinkedHashMap<IBinding, Function>();
		Map<IBinding, ClassDeclCDT> _typeBindingToClass = new LinkedHashMap<IBinding, ClassDeclCDT>();
	}
	
	class EventFunction {
		Function _function;
		List<FuncParameter> _parameterValues;
		
		EventFunction(Function function, List<FuncParameter> parameterValues) {
			_function = function;
			_parameterValues = parameterValues;
		}
	}
	
	static Logger logger = LogManager.getLogger(Graph.class.getName());

	CppFile _currCppFile = new CppFile();
	//private Map<IASTTranslationUnit, CppFile> _cppFiles = new HashMap<IASTTranslationUnit, CppFile>();
	private Map<CodeLocation, Function> _funcByLocation = new TreeMap<CodeLocation, Function>();
	private Map<CodeLocation, ClassDeclCDT> _classByLocation = new TreeMap<CodeLocation, ClassDeclCDT>();
	//TODO check if this set is really necessary
	Set<IBinding> _functionTypedefs = new HashSet<IBinding>();
	Function _mainFunction = null;
	Graph _gvplGraph;
	List<EventFunction> _eventFunctions = new ArrayList<EventFunction>();
	ScriptManager _scriptManager = null;
	Map<IBinding, IVar> _globalVars = new HashMap<IBinding, IVar>();
	
	public AstInterpreterCDT(Graph gvplGraph) {
		_gvplGraph = gvplGraph;
		CppMaps.initialize();
	}
	
	public void loadDeclarations(IASTTranslationUnit root) {
		//_currCppFile = new CppFile();
		//_cppFiles.put(root, _currCppFile);
		
		IASTDeclaration[] declarations = root.getDeclarations();

		// Iterate through function, class e structs declarations
		for (IASTDeclaration declaration : declarations) {
			logger.debug("Location of declaration: {}", CodeLocationCDT.NewFromFileLocation(declaration.getFileLocation()));
			
			// If the declaration is a function
			if (declaration instanceof IASTFunctionDefinition) {
				Function func = loadFunction((IASTFunctionDefinition) declaration);
				if (func != null)
					_mainFunction = func;
			} else if (declaration instanceof IASTSimpleDeclaration) {// if it's a class/struct
				IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
				DebugOptions.setStartingLine(declaration.getFileLocation().getStartingLineNumber());
				IASTDeclSpecifier declSpec = simpleDecl.getDeclSpecifier();
				logger.debug(declSpec.getClass());

				if(declSpec instanceof CPPASTCompositeTypeSpecifier) {
					loadClassImplementation((CPPASTCompositeTypeSpecifier) declSpec);
				} else if(declSpec instanceof CPPASTElaboratedTypeSpecifier) {
					// forward declaration (always?)
					loadClassDecl((CPPASTElaboratedTypeSpecifier) declSpec);
				} else if(declSpec instanceof CPPASTSimpleDeclSpecifier) {
					IASTDeclarator[] declarators = simpleDecl.getDeclarators();
					for(IASTDeclarator declarator : declarators) {
						if (declarator instanceof CPPASTFunctionDeclarator) {
							CPPASTFunctionDeclarator funcDecl = (CPPASTFunctionDeclarator) declarator;
							loadFunctionDeclaration(funcDecl);
						} else if (declarator instanceof CPPASTDeclarator) {
							IASTName name = declarator.getName();
							IBinding binding = name.resolveBinding();
							if(binding instanceof CPPVariable) {
								addGlobalVar(declarator, declSpec);
							} else if (binding instanceof CPPField) {
								initializeGlobalVar(binding, declarator);
							}
						} else
							logger.error("you're doing it wrong. {}", declarator.getClass());
					}
				} else if(declSpec instanceof CPPASTNamedTypeSpecifier) {
					IASTDeclarator[] declarators = simpleDecl.getDeclarators();
					for(IASTDeclarator declarator : declarators) {
						IASTName name = declarator.getName();
						IBinding binding = name.resolveBinding();
						initializeGlobalVar(binding, declarator);
					}
				} else if(declSpec instanceof IASTEnumerationSpecifier) {
					EnumCDT.loadEnum( (IASTEnumerationSpecifier) declSpec, this );
				} else
					logger.fatal("you're doing it wrong. {}. CodeLoc: {}", 
							declSpec.getClass(), DebugOptions.getCurrCodeLocation() );
			} else if (declaration instanceof CPPASTUsingDirective) {// if it's a class/struct
				logger.error("Not implemented: {}", declaration.getClass());
			} else
				logger.fatal("Deu merda aqui. {}", declaration.getClass());
		}
	}
	
	private void initializeGlobalVar(IBinding binding, IASTDeclarator declarator) {
		//TODO get correct type
		IVar var = _globalVars.get(binding);
		InstructionLine il = new InstructionLine(_gvplGraph, null, this);
		il.LoadVariableInitialization(var, declarator);
	}
	
	public void loadDefinitions() {
		//_currCppFile = _cppFiles.get(root);
		logger.debug(" *** Loading definitions of main ***");
		_mainFunction.addFuncRef(new ArrayList<FuncParameter>(), _gvplGraph, this);
		callEventFunctions();
	}
	
	public IVar getGlobalVar(IBinding binding) {
		ExecTreeLogger.log(binding.getName());
		return _globalVars.get(binding);
	}

	/**
	 * Loads a function from the AST
	 * 
	 * @param funcDefinition
	 * @return A instance of Function
	 */
	private Function loadFunction(IASTFunctionDefinition funcDefinition) {
		CPPASTFunctionDeclarator declarator = (CPPASTFunctionDeclarator) funcDefinition.getDeclarator();
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
		ClassDeclCDT classDecl = _currCppFile._typeBindingToClass.get(classBinding);
		classDecl.loadMemberFunc(declaration, this);
	}
	
	private Function loadFunctionDeclaration(CPPASTFunctionDeclarator decl) {
		CodeLocation funcLocation = CodeLocationCDT.NewFromFileLocation(decl.getFileLocation());
		IBinding binding = decl.getName().resolveBinding();
		if(binding instanceof CPPTypedef) {
			_functionTypedefs.add(binding);
			return null;
		}
		Function function = _currCppFile._funcIdMap.get(binding);
		if(function != null)
			return function;
		
		function = _funcByLocation.get(funcLocation);
		// the function declaration has already been loaded by other .h file
		if(function != null) {
			_currCppFile._funcIdMap.put(binding, function);
			return function;
		}
		
		function = new Function(this, binding);
		_currCppFile._funcIdMap.put(binding, function);
		_funcByLocation.put(funcLocation, function);
		function.loadDeclaration(decl);
		
		return function;
	}
	
	private Function loadSimpleFunction(IASTName name, CPPASTFunctionDeclarator funcDeclarator, IASTFunctionDefinition funcDefinition) {
		DebugOptions.setStartingLine(funcDeclarator.getFileLocation().getStartingLineNumber());
		Function function = loadFunctionDeclaration(funcDeclarator);

		DebugOptions.setStartingLine(funcDeclarator.getFileLocation().getStartingLineNumber());
		function.loadDefinition(funcDeclarator.getConstructorChain(), funcDefinition);
		return function;
	}
	
	public void addClassDeclInMaps(ClassDeclCDT classDecl, IBinding binding) {
		_currCppFile._typeBindingToClass.put(binding, classDecl);
		_typeIdToClass.put(classDecl.getTypeId(), classDecl);
		_classByLocation.put(classDecl.getCodeLocation(), classDecl);
	}
	
	private ClassDeclCDT loadClassDecl(CPPASTBaseDeclSpecifier strDecl) {
		IASTName name = null;
		if(strDecl instanceof CPPASTCompositeTypeSpecifier)
			name = ((CPPASTCompositeTypeSpecifier)strDecl).getName();
		else if(strDecl instanceof CPPASTElaboratedTypeSpecifier)
			name = ((CPPASTElaboratedTypeSpecifier)strDecl).getName();
		else if(strDecl instanceof CPPASTNamedTypeSpecifier)
			name = ((CPPASTNamedTypeSpecifier)strDecl).getName();
		else
			logger.fatal("you're doing it wrong. {}", strDecl.getClass());
		
		IBinding binding = name.resolveBinding();
		ClassDeclCDT classDecl = _currCppFile._typeBindingToClass.get(binding);
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
	
	public boolean isFunctionTypedef(IASTDeclSpecifier declSpec) {
		IBinding binding = bindingFromDeclSpec(declSpec);
		return _functionTypedefs.contains(binding);
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
			classDecl.loadAstDecl(strDecl, _gvplGraph);
		} else {
			addClassDeclInMaps(classDecl, strDecl.getName().resolveBinding());
			classDecl.updateBindings(strDecl);
		}
	}
	
	IBinding bindingFromDeclSpec(IASTDeclSpecifier declSpec) {
		if(declSpec instanceof IASTNamedTypeSpecifier)
			return ((IASTNamedTypeSpecifier) declSpec).getName().resolveBinding();
		else if(declSpec instanceof CPPASTElaboratedTypeSpecifier)
			return ((CPPASTElaboratedTypeSpecifier) declSpec).getName().resolveBinding();
		else
			return null;
	}
	
	void callEventFunctions() {
		for(EventFunction eventFunction : _eventFunctions) {
			eventFunction._function.addFuncRef(eventFunction._parameterValues, _gvplGraph, this);
		}
	}
	
	/**
	 * Add a function that is a event and will be called once
	 *  
	 * @param function The function that will be called
	 * @param parameterValues the parameters that will be passed to the function
	 */
	public void addEventFunction(Function function, List<FuncParameter> parameterValues) {
		_eventFunctions.add(new EventFunction(function, parameterValues));
	}
	
	public void setScriptManager(ScriptManager scriptManager)  {
		_scriptManager = scriptManager;
	}
	
	public boolean scriptFunctionExists(Function func) {
		return _scriptManager.functionExists(func.getName());
	}
	
	public void callScriptFunction(Function func, List<FuncParameter> parameterValues) {
		_scriptManager.callFunc(func.getName(), parameterValues);
	}
	
	public void addGlobalVar(IASTDeclarator declarator, IASTDeclSpecifier declSpec) {
		IASTName name = declarator.getName();
		TypeId type = getType(declSpec);
		if(type == null) {
			logger.error("var from {} not found", declSpec.getRawSignature());
			return;
		}
		IBinding binding = name.resolveBinding();
		//TODO get correct type
		IVar var = BaseScopeCDT.addVarDecl(name.toString(), type, 
				declarator.getPointerOperators(), _gvplGraph, null, this);
		_globalVars.put(binding, var);
		
		InstructionLine il = new InstructionLine(_gvplGraph, null, this);
		il.LoadVariableInitialization(var, declarator);
	}
	
	public void addGlobalVar( IBinding binding, IVar var ) {
		_globalVars.put(binding, var);
	}

	/**
	 * Gets the id of a type from the declaration in AST
	 * 
	 * @param declSpec
	 * @return The id of the type
	 */
	public TypeId getType(IASTDeclSpecifier declSpec) {
		IBinding binding = bindingFromDeclSpec(declSpec);
		if (binding == null)
			return _primitiveType;
		
		ClassDeclCDT classDecl = _currCppFile._typeBindingToClass.get(binding);
		if (classDecl == null)
			return _primitiveType;
		
		return classDecl.getTypeId();
	}
	
	public ClassDeclCDT getClassDecl(IBinding binding) {
		return _currCppFile._typeBindingToClass.get(binding);
	}

	/**
	 * Returns the id of a type from it's binding
	 * 
	 * @param binding
	 *            The binding of the type id
	 * @return The type id from the received binding
	 */
	public TypeId getTypeFromBinding(IBinding binding) {
		ClassDeclCDT classDecl = _currCppFile._typeBindingToClass.get(binding);
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
		logger.debug("idb:{}", binding.hashCode());
		return _currCppFile._funcIdMap.get(binding);
	}
	
	public Graph getGraph() {
		return _gvplGraph;
	}
	
	@Override
	public IVar getVarFromBinding(IBinding binding) {
		ExecTreeLogger.log(binding.getName());
		return getVarFromBindingUnbounded(binding);
	}
	
	@Override
	public IVar getVarFromBindingUnbounded(IBinding binding) {
		ExecTreeLogger.log(binding.getName());
		return _globalVars.get(binding);
	}
	
	@Override
	public boolean hasVarInScope(IBinding binding) {
		return getVarFromBindingUnbounded(binding) != null;
	}
	
}
