package gvpl.cdt;

import gvpl.cdt.function.Function;
import gvpl.clang.CPPClassInstance;
import gvpl.clang.CPPClassSpecialization;
import gvpl.clang.CPPClassTemplate;
import gvpl.common.AstInterpreter;
import gvpl.common.CodeLocation;
import gvpl.common.FuncParameter;
import gvpl.common.IVar;
import gvpl.common.ScopeManager;
import gvpl.common.ScriptManager;
import gvpl.common.TypeId;
import gvpl.common.Var;
import gvpl.exceptions.ClassNotImplementedException;
import gvpl.graph.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType.CPPClassTypeDelegate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownClass;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;

import debug.DebugOptions;
import debug.ExecTreeLogger;

public class AstInterpreterCDT extends AstInterpreter {
	
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
	
	static Logger logger = LogManager.getLogger(AstInterpreterCDT.class.getName());

	CppFile _currCppFile = new CppFile();
	Map<CodeLocation, Function> _funcByLocation = new TreeMap<CodeLocation, Function>();
	Map<CodeLocation, ClassDeclCDT> _classByLocation = new TreeMap<CodeLocation, ClassDeclCDT>();
	Map<IBinding, IBinding> _typedefBindings = new HashMap<IBinding, IBinding>();
	//TODO check if this set is really necessary
	Set<IBinding> _functionTypedefs = new LinkedHashSet<IBinding>();
	Function _mainFunction = null;
	List<EventFunction> _eventFunctions = new ArrayList<EventFunction>();
	ScriptManager _scriptManager = null;
	Map<IBinding, IVar> _globalVars = new LinkedHashMap<IBinding, IVar>();
	Map<CodeLocation, IVar> _globalVarsByCodeLoc = new TreeMap<CodeLocation, IVar>();
	
	public AstInterpreterCDT(Graph gvplGraph) {
		_gvplGraph = gvplGraph;
		CppMaps.initialize();
	}
	
	public void loadDeclarations(IASTTranslationUnit root) {
		ScopeManager.addScope(this);
		IASTDeclaration[] declarations = root.getDeclarations();
		loadDeclarations(declarations);
		ScopeManager.removeScope(this);
	}
	
	private void loadDeclarations(IASTDeclaration[] declarations) {
		// Iterate through function, class e structs declarations
		for (IASTDeclaration declaration : declarations) {
			if (declaration instanceof IASTProblemDeclaration) {
				IASTProblemDeclaration problem = (IASTProblemDeclaration) declaration;
				logger.debug("problem declaration {}", problem.getContainingFilename());
				continue;
			}

			logger.debug("Location of declaration: {}",
					CodeLocationCDT.NewFromFileLocation(declaration));
			try {
				loadDeclaration(declaration);
			} catch(Exception e) {
				logger.fatal("Critical error. Code location: {}, Stack trace: {}, message: {}", DebugOptions.getCurrCodeLocation(), e.getStackTrace(), e.getMessage());
			}
		}
	}
	
	private void loadDeclaration(IASTDeclaration declaration) {
		// If the declaration is a function
		if (declaration instanceof IASTFunctionDefinition) {
			Function func = loadFunction((IASTFunctionDefinition) declaration);
			if (func != null)
				_mainFunction = func;
		} else if (declaration instanceof IASTSimpleDeclaration) {// if it's
																	// a
																	// class/struct
			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
			DebugOptions.setStartingLine(declaration.getFileLocation().getStartingLineNumber());
			IASTDeclSpecifier declSpec = simpleDecl.getDeclSpecifier();
			logger.debug(declSpec.getClass());

			if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
				loadClassImplementation((ICPPASTCompositeTypeSpecifier) declSpec);
			} else if (declSpec instanceof ICPPASTElaboratedTypeSpecifier) {
				// forward declaration (always?)
				loadClassDecl((ICPPASTElaboratedTypeSpecifier) declSpec);
			} else if (declSpec instanceof IASTSimpleDeclSpecifier) {
				IASTDeclarator[] declarators = simpleDecl.getDeclarators();
				for (IASTDeclarator declarator : declarators) {
					if (declarator instanceof ICPPASTFunctionDeclarator) {
						ICPPASTFunctionDeclarator funcDecl = (ICPPASTFunctionDeclarator) declarator;
						loadFunctionDeclaration(funcDecl);
					} else if (declarator instanceof IASTDeclarator) {
						IASTName name = declarator.getName();
						IBinding binding = name.resolveBinding();
						if (binding instanceof ICPPVariable) {
							addGlobalVar(declarator, declSpec);
						} else if (binding instanceof ICPPField) {
							initializeGlobalVar(binding, declarator);
						} else if (binding instanceof ITypedef) {
							logger.info("Not implemented CPPTypedef: {}", binding.getName());
						} else {
							logger.error("Not implemented: {}", binding.getClass());
						}
					} else
						logger.error("you're doing it wrong. {}", declarator.getClass());
				}
			} else if (declSpec instanceof IASTNamedTypeSpecifier) {
				IASTDeclarator[] declarators = simpleDecl.getDeclarators();
				for (IASTDeclarator declarator : declarators) {
					if(declarator instanceof ICPPASTFunctionDeclarator) {
						loadFunctionDeclaration((ICPPASTFunctionDeclarator) declarator);
						return;
					}
					
					IASTName name = declarator.getName();
					IBinding binding = name.resolveBinding();
					if(binding instanceof ITypedef) {
						IASTName dsName = ((IASTNamedTypeSpecifier) declSpec).getName();
						IBinding originalClassBinding = dsName.resolveBinding();
						_typedefBindings.put(binding, originalClassBinding);
					} else
						initializeGlobalVar(binding, declarator);
				}
			} else if (declSpec instanceof IASTEnumerationSpecifier) {
				EnumCDT.loadEnum((IASTEnumerationSpecifier) declSpec, this);
			} else
				logger.fatal("you're doing it wrong. {}. CodeLoc: {}", declSpec.getClass(),
						DebugOptions.getCurrCodeLocation());
		} else if (declaration instanceof ICPPASTUsingDirective) {// if it's a class/struct
			logger.info("Using directive: {}", declaration.getClass());
		} else if (declaration instanceof ICPPASTNamespaceDefinition) {
			ICPPASTNamespaceDefinition nd = (ICPPASTNamespaceDefinition) declaration;
			loadDeclarations(nd.getDeclarations());
		} else if (declaration instanceof ICPPASTLinkageSpecification) {// extern  "C"
			ICPPASTLinkageSpecification ls = (ICPPASTLinkageSpecification) declaration;
			loadDeclarations(ls.getDeclarations());
			logger.warn("TODO? CPPASTLinkageSpecification: {}", declaration.getClass());
		} else if (declaration instanceof ICPPASTUsingDeclaration) {
			ICPPASTUsingDeclaration ud = (ICPPASTUsingDeclaration) declaration;
			logger.info("Not implemented CPPASTUsingDeclaration: {}", ud.getName());
		} else if (declaration instanceof ICPPASTTemplateDeclaration) {
			ICPPASTTemplateDeclaration td = (ICPPASTTemplateDeclaration) declaration;
			loadDeclaration(td.getDeclaration());
		} else if ( declaration instanceof ICPPASTTemplateSpecialization ){
			ICPPASTTemplateSpecialization ts = (ICPPASTTemplateSpecialization) declaration;
			loadDeclaration(ts.getDeclaration());
		} else if ( declaration instanceof GPPASTExplicitTemplateInstantiation ){
			GPPASTExplicitTemplateInstantiation ti = (GPPASTExplicitTemplateInstantiation) declaration;
			logger.warn("Not implemented GPPASTExplicitTemplateInstantiation: {}", ti.getRawSignature());
		} else
			logger.error("Deu merda aqui. {}", declaration.getClass());
	}
	
	private void initializeGlobalVar(IBinding binding, IASTDeclarator declarator) {
		IASTName name = declarator.getName();
		CodeLocation codeLocation = CodeLocationCDT.NewFromFileLocation(name);
		//TODO get correct type
		IVar var = getGlobalVar(binding, codeLocation);
		if(var == null) {
			logger.error("Global var not found: {}", binding.getName());
			return;
		}
		InstructionLine il = new InstructionLine(_gvplGraph, null, this);
		
		CodeLocation previousCL = DebugOptions.getCurrCodeLocation();
		DebugOptions.setCurrCodeLocation(codeLocation);
		il.LoadVariableInitialization(var, declarator);
		DebugOptions.setCurrCodeLocation(previousCL);
	}
	
	public void loadMain() {
		ScopeManager.addScope(this);
		//_currCppFile = _cppFiles.get(root);
		logger.debug(" *** Loading definitions of main ***");
		_mainFunction.addFuncRef(new ArrayList<FuncParameter>(), _gvplGraph, this);
		callEventFunctions();
		ScopeManager.removeScope(this);
	}

	/**
	 * Loads a function from the AST
	 * 
	 * @param funcDefinition
	 * @return A instance of Function
	 */
	private Function loadFunction(IASTFunctionDefinition funcDefinition) {
		ICPPASTFunctionDeclarator declarator = (ICPPASTFunctionDeclarator) funcDefinition.getDeclarator();
		IASTName name = declarator.getName();

		// method function
		if (name instanceof ICPPASTQualifiedName) {
			loadMethod(funcDefinition, (ICPPASTQualifiedName) name);
		} // a function that is not a method
		else if (name instanceof IASTName) {
			Function function = loadSimpleFunction((ICPPASTFunctionDeclarator) declarator, funcDefinition);
			if (function.getName().equals("main"))
				return function;
		} else
			logger.fatal("Problem");

		return null;
	}

	private void loadMethod(IASTFunctionDefinition declaration, ICPPASTQualifiedName qn) {
		IASTName[] names = qn.getNames();
		IASTName className = names[0];
		IBinding classBinding = className.resolveBinding();
		if(classBinding instanceof IProblemBinding) {
			logger.error("problem binding: {}", ((IProblemBinding)classBinding).getMessage());
			return;
		} else if(classBinding instanceof CPPDeferredClassInstance) {
			logger.warn("Not implemented CPPDeferredClassInstance: {}", ((CPPDeferredClassInstance)classBinding).getName());
			return;
		} else if(classBinding instanceof ICPPNamespace) {
			logger.info("Not implemented CPPNamespace: {}", ((ICPPNamespace)classBinding).getName());
			return;
		} else if (classBinding instanceof CPPClassSpecialization ) {
			logger.warn("Not implemented CPPClassSpecialization: {}", ((CPPClassSpecialization)classBinding).getName());
			return;
		} else if (classBinding instanceof ICPPClassType ) {
			logger.info("CPPClassType: {}", ((ICPPClassType)classBinding).getName());
		} else if (classBinding instanceof IASTName ) {
			logger.warn("Not implemented CPPASTName: {}", ((IASTName)classBinding));
			return;
		} else
			logger.error("Not implemented {}", classBinding.getClass());
		
		ClassDeclCDT classDecl = _currCppFile._typeBindingToClass.get(classBinding);
		if( classDecl == null ) {
			logger.error("Class decl not found: {}", classBinding);
			return;
		}
		classDecl.loadMemberFunc(declaration, this);
	}
	
	private Function loadFunctionDeclaration(ICPPASTFunctionDeclarator decl) {
		CodeLocation funcLocation = CodeLocationCDT.NewFromFileLocation(decl);
		IBinding binding = decl.getName().resolveBinding();
		if(binding instanceof ITypedef) {
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
	
	private Function loadSimpleFunction(ICPPASTFunctionDeclarator funcDeclarator, IASTFunctionDefinition funcDefinition) {
		DebugOptions.setStartingLine(funcDeclarator.getFileLocation().getStartingLineNumber());
		Function function = loadFunctionDeclaration(funcDeclarator);

		DebugOptions.setStartingLine(funcDeclarator.getFileLocation().getStartingLineNumber());
		function.initializeDefinition(funcDeclarator.getConstructorChain(), funcDefinition);
		return function;
	}
	
	public void addClassDeclInMaps(ClassDeclCDT classDecl, IBinding binding) {
		_currCppFile._typeBindingToClass.put(binding, classDecl);
		_typeIdToClass.put(classDecl.getTypeId(), classDecl);
		_classByLocation.put(classDecl.getCodeLocation(), classDecl);
	}
	
	private ClassDeclCDT loadClassDecl(ICPPASTDeclSpecifier strDecl) {
		IASTName name = null;
		if(strDecl instanceof ICPPASTCompositeTypeSpecifier)
			name = ((ICPPASTCompositeTypeSpecifier)strDecl).getName();
		else if(strDecl instanceof ICPPASTElaboratedTypeSpecifier)
			name = ((ICPPASTElaboratedTypeSpecifier)strDecl).getName();
		else if(strDecl instanceof IASTNamedTypeSpecifier)
			name = ((IASTNamedTypeSpecifier)strDecl).getName();
		else
			logger.fatal("you're doing it wrong. {}", strDecl.getClass());
		
		IBinding binding = name.resolveBinding();
		ClassDeclCDT classDecl = _currCppFile._typeBindingToClass.get(binding);
		if(classDecl != null)
			return classDecl;
		
		CodeLocation classLocation = CodeLocationCDT.NewFromFileLocation(strDecl);
		classDecl = _classByLocation.get(classLocation);
		// the class declaration has already been loaded by other .h file
		if(classDecl != null) {
			addClassDeclInMaps(classDecl, binding);
			return classDecl;
		}
		
		classDecl = new ClassDeclCDT(this, classLocation, binding);
		classDecl.setBinding(name);
		addClassDeclInMaps(classDecl, binding);
		return classDecl;
	}
	
	public boolean isFunctionTypedef(IASTDeclSpecifier declSpec) {
		IBinding binding;
		try {
			binding = bindingFromDeclSpec(declSpec);
		} catch (ClassNotImplementedException e) {
			return false;
		}
		return _functionTypedefs.contains(binding);
	}

	/**
	 * Loads a class or structure from the AST
	 * 
	 * @param strDecl
	 */
	private void loadClassImplementation(ICPPASTCompositeTypeSpecifier strDecl) {		
		CodeLocation classLocation = CodeLocationCDT.NewFromFileLocation(strDecl);
		ClassDeclCDT classDecl = _classByLocation.get(classLocation);
		if(classDecl == null) {
			classDecl = loadClassDecl(strDecl);
			classDecl.loadAstDecl(strDecl, _gvplGraph);
		} else {
			addClassDeclInMaps(classDecl, strDecl.getName().resolveBinding());
			classDecl.updateBindings(strDecl);
		}
	}
	
	IBinding bindingFromDeclSpec(IASTDeclSpecifier declSpec) throws ClassNotImplementedException {
		if(declSpec instanceof IASTNamedTypeSpecifier) {
			IASTName name = ((IASTNamedTypeSpecifier) declSpec).getName();
			if(name instanceof ICPPASTQualifiedName) {
				IASTName[] names = ((ICPPASTQualifiedName)name).getNames();
				if(names.length > 1)
					name = names[1];
			}
			if(name instanceof ICPPASTTemplateId) {
				ICPPASTTemplateId tid = (ICPPASTTemplateId) name;
				IBinding binding = tid.resolveBinding();
				IASTName templateName = null;
				if(binding instanceof CPPClassInstance) {
					ICPPInternalBinding classSpecialization = (ICPPInternalBinding) binding;
					templateName = (IASTName) classSpecialization.getDefinition();
				} else if (binding instanceof CPPUnknownClass) {
					logger.error("CPPUnknownClass, {}", binding.getName());
					return null;
				} else if (binding instanceof CPPDeferredClassInstance) {
					CPPDeferredClassInstance dci = (CPPDeferredClassInstance) binding;
					CPPClassTemplate classTemplate = (CPPClassTemplate) dci.getSpecializedBinding();
					logger.error("work here");
					IASTNode node = null;
					//classTemplate.getDefinition();
					if(node instanceof IASTName) {
						templateName = (IASTName) node;
					} else if (node == null)
						return null;
					else {
						logger.error("Problem loading class {}, {}", node.getClass());
						return null;
					}
				} else if (binding instanceof CPPClassSpecialization) {
					return binding;
				} else  {
					logger.error("Not implemented: {}", binding.getClass());
					return null;
				}
				
				if(templateName == null)
					return null;
				
				IBinding templateBinding = templateName.resolveBinding();
				return templateBinding;
			}
			IBinding binding = name.resolveBinding();
			if(binding instanceof IProblemBinding) {
				logger.error("ProblemBinding: {}", name.getRawSignature());
			}
			if(binding instanceof ITypedef) {
				IBinding newBinding = _typedefBindings.get(binding);
				if(newBinding != null)
					return newBinding;
			} else if (binding instanceof CPPClassTypeDelegate) {
				CPPClassTypeDelegate ctd = (CPPClassTypeDelegate) binding;
				binding = ctd.getBinding();
			}
			return binding;
		}
		else if(declSpec instanceof ICPPASTElaboratedTypeSpecifier)
			return ((ICPPASTElaboratedTypeSpecifier) declSpec).getName().resolveBinding();
		else if(declSpec instanceof ICPPASTSimpleDeclSpecifier) {
			return null;
		} else if(declSpec instanceof IASTEnumerationSpecifier) {
			return null;
		} else {
			throw new ClassNotImplementedException(declSpec.getClass().toString(), declSpec.getRawSignature().toString());
		}
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
		
		CodeLocation codeLocation = CodeLocationCDT.NewFromFileLocation(name);
		IBinding binding = name.resolveBinding();
		//TODO get correct type
		IVar var = BaseScopeCDT.addVarDecl(name.toString(), type, 
				declarator.getPointerOperators(), _gvplGraph, this);
		addGlobalVar(binding, codeLocation, var);
		
		InstructionLine il = new InstructionLine(_gvplGraph, null, this);
		
		CodeLocation previousCL = DebugOptions.getCurrCodeLocation();
		DebugOptions.setCurrCodeLocation(codeLocation);
		il.LoadVariableInitialization(var, declarator);
		DebugOptions.setCurrCodeLocation(previousCL);
	}
	
	public void addGlobalVar( IBinding binding, CodeLocation fileLoc, IVar var ) {
		ExecTreeLogger.log(binding.getName());
		logger.debug(binding.getName());
		_globalVars.put(binding, var);
		_globalVarsByCodeLoc.put(fileLoc, var);
	}
	
	public IVar getGlobalVar(IBinding binding, CodeLocation fileLoc) {
		if(binding == null) {
			logger.error("Binding can't be null");
			return new Var(_gvplGraph, "ERROR", _primitiveType);
		}
		ExecTreeLogger.log(binding.getName());
		logger.debug(binding.getName());
		IVar var = _globalVars.get(binding);
		if(var != null)
			return var;
		if(fileLoc != null)
			return _globalVarsByCodeLoc.get(fileLoc);
		
		return null;
	}

	/**
	 * Gets the id of a type from the declaration in AST
	 * 
	 * @param declSpec
	 * @return The id of the type
	 */
	public TypeId getType(IASTDeclSpecifier declSpec) {
		IBinding binding = null;
		try {
			binding = bindingFromDeclSpec(declSpec);
		} catch (ClassNotImplementedException e) {
			return _primitiveType;
		}
		if(binding == null)
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
	public IVar getVarFromBinding(IBinding binding, CodeLocation codeLoc) {
		ExecTreeLogger.log(binding.getName());
		return getGlobalVar(binding, codeLoc);
	}
	
}
