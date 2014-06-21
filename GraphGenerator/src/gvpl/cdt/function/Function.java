package gvpl.cdt.function;

import gvpl.cdt.AstInterpreterCDT;
import gvpl.cdt.BaseScopeCDT;
import gvpl.cdt.ClassDeclCDT;
import gvpl.cdt.CodeLocationCDT;
import gvpl.cdt.InstructionLine;
import gvpl.common.BaseScope;
import gvpl.common.CodeLocation;
import gvpl.common.FuncParameter;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.common.IVar;
import gvpl.common.MemAddressVar;
import gvpl.common.PointerVar;
import gvpl.common.ScopeManager;
import gvpl.common.TypeId;
import gvpl.common.Value;
import gvpl.exceptions.NotFoundException;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReferenceOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
//import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction; //CDT_CLANG
import gvpl.clang.CPPFunction; //CDT_CLANG

import debug.DebugOptions;
import debug.ExecTreeLogger;

public class Function extends BaseScopeCDT {
	
	static Logger logger = LogManager.getLogger(Function.class.getName());
	
	private IVar _returnVar = null;
	private IASTPointerOperator[] _returnPointerOps;
	private TypeId _returnType = null;
	private boolean _isStatic = false;

	private String _externalName = "";
	private Map<IBinding, FuncParameter> _originalParametersMap;
	private List<IBinding> _originalParameters;
	
	private Map<IBinding, FuncParameter> _parametersMap = null;
	protected String _funcName;
	protected IBinding _ownBinding;
	CodeLocation _declLocation = null;
	CodeLocation _implLocation = null;
	
	ICPPASTConstructorChainInitializer[] _ccInitializer;
	IASTStatement _body;
	String _bodyFileName;

	public Function(AstInterpreterCDT astInterpreter, IBinding ownBinding) {
		super(astInterpreter, null);
		
		_ownBinding = ownBinding;
		
		if(ownBinding instanceof ICPPFunction) {
			IASTNode node = ((CPPFunction)ownBinding).getDefinition();
			if(node != null) {
				node = node.getParent();
				IASTDeclSpecifier declSpec = ((IASTFunctionDefinition)node).getDeclSpecifier();
				if(declSpec instanceof IASTNamedTypeSpecifier) {
					IBinding binding = ((IASTNamedTypeSpecifier)declSpec).getName().resolveBinding();
					ClassDeclCDT returnClassDecl = _astInterpreter.getClassDecl(binding);
					if(returnClassDecl != null) {
						_returnType = returnClassDecl.getTypeId();
						return;
					}
				}
			}
		}
		
		_returnType = _astInterpreter.getPrimitiveType();
	}
	
	public void loadDeclaration(ICPPASTFunctionDeclarator decl) {
		_returnPointerOps = decl.getPointerOperators();
		IASTNode parentNode = decl.getParent();
		if( parentNode instanceof CPPASTSimpleDeclaration ) {
			CPPASTSimpleDeclaration simpleDecl = (CPPASTSimpleDeclaration) parentNode;
			_isStatic = simpleDecl.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_static;
		} else if ( parentNode instanceof IASTFunctionDefinition ) {
			IASTFunctionDefinition funcDef = (IASTFunctionDefinition) parentNode;
			_isStatic = funcDef.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_static;
		} else {
			logger.error("not good");
		}
		IASTName astName = decl.getName();
		loadFuncParameters(decl.getParameters());
		// Gets the name of the function
		if (astName instanceof ICPPASTQualifiedName)
			_funcName = ((ICPPASTQualifiedName) astName).getNames()[1].toString();
		else
			_funcName = astName.toString();

		logger.debug("Loading declaration of func {}", _funcName);
		
		setName(calcName());
		logger.debug("Storing func parameter. Func: {}, File: {}", _externalName, DebugOptions.getCurrCpp());
		
		_declLocation = CodeLocationCDT.NewFromFileLocation(decl);
	}
	
	public void initializeDefinition(ICPPASTConstructorChainInitializer[] ccInitializer, IASTFunctionDefinition funcDefinition) {
		_body = funcDefinition.getBody();
		_bodyFileName = CodeLocation.getCurrentFileName();
		_ccInitializer = ccInitializer;
		ICPPASTFunctionDeclarator declarator = (ICPPASTFunctionDeclarator) funcDefinition.getDeclarator();
		loadFuncParameters(declarator.getParameters());

		logger.debug("Storing body. Func: {}, File: {}", _externalName, DebugOptions.getCurrCpp());
	}
	
	public void loadDefinition(Graph gvplGraph) {
		ScopeManager.addScope(this);
		if (_body instanceof IASTCompoundStatement) {
			IASTStatement[] statements = ((IASTCompoundStatement)_body).getStatements();
			for (IASTStatement statement : statements) {
				InstructionLine instructionLine = new InstructionLine(gvplGraph, this, _astInterpreter);	
				try {
					instructionLine.load(statement);
				} catch(Exception e) {
					logger.fatal("Critical error. Code location: {}, Stack trace: {}, Msg: {}", 
							DebugOptions.getCurrCodeLocation(), e.getStackTrace(), e.getMessage());
				}
			}
		} else
			logger.fatal("Work here.");
		
		_implLocation = CodeLocationCDT.NewFromFileLocation(_body);
		
		lostScope();
	}
	
	public TypeId getReturnTypeId() {
		return _returnType;
	}

	void loadConstructorChain(Graph graph, BaseScope caller) {
	}

	protected String calcName() {
		return _funcName;
	}

	/**
	 * Reads the parameters from a function declaration
	 * 
	 * @param parameters
	 *            Parameters of the function
	 */
	public void loadFuncParameters(IASTParameterDeclaration[] parameters) {
		_originalParameters = new ArrayList<IBinding>();
		_originalParametersMap = new LinkedHashMap<IBinding, FuncParameter>();
		for (IASTParameterDeclaration parameter : parameters) {
			IASTDeclarator parameterVarDecl = parameter.getDeclarator();
			IASTDeclSpecifier declSpec = parameter.getDeclSpecifier();
			IBinding binding = parameterVarDecl.getName().resolveBinding();
			FuncParameter funcParameter = null;
			if(_astInterpreter.isFunctionTypedef(declSpec)) {
				funcParameter = new FuncParameter(IndirectionType.E_FUNCTION_POINTER);
			} else {
				// TODO use the type of the parameter
				//TypeId type = _astInterpreter.getType(declSpec);
				FuncParameter.IndirectionType parameterVarType = null;
				parameterVarType = getIndirectionType(parameter.getDeclarator().getPointerOperators());
				funcParameter = new FuncParameter(parameterVarType);
				if(declSpec instanceof IASTSimpleDeclSpecifier)
					funcParameter.setType(((IASTSimpleDeclSpecifier)declSpec).getType());
			}

			addParameter(binding, funcParameter);
		}
	}
	
	private void loadHeaderOnlyFunc(List<FuncParameter> parameterValues, Graph extGraph) {
		logger.info("Header only function: {}.", this);
		IndirectionType returnIndirectionType = getIndirectionType(_returnPointerOps);
		_returnVar = BaseScope.instanceVar(returnIndirectionType, _externalName, _returnType, _gvplGraph, _astInterpreter);
		GraphNode returnNode = new GraphNode(_externalName, NodeType.E_RETURN_VALUE);
		if(_returnVar instanceof PointerVar) {
			((PointerVar)_returnVar).setPointedVar(BaseScope.instanceVar(IndirectionType.E_VARIABLE, _externalName, _returnType, _gvplGraph, _astInterpreter));
		} else
			_returnVar.receiveAssign(NodeType.E_RETURN_VALUE, new Value(returnNode), _gvplGraph);
		
		if(parameterValues == null){
			logger.info("Header only function {} received no params.", this);
			return;
		}
		
		for(FuncParameter funcParameter : parameterValues) {
			funcParameter.getValue().getNode().addDependentNode(_returnVar.getCurrentNode());
		}
	}

	public Value addFuncRef(List<FuncParameter> parameterValues, Graph extGraph, BaseScope caller) {
		_parent = caller;
		resetBaseScope();
		System.out.println("addFuncRef " + this);
		logger.debug(" -- Add func ref {}: {}", this, DebugOptions.getCurrCodeLocation());
		_gvplGraph = new Graph(_externalName);
		IndirectionType returnIndirectionType = getIndirectionType(_returnPointerOps);
		_returnVar = BaseScope.instanceVar(returnIndirectionType, _externalName, _returnType, _gvplGraph, _astInterpreter);
		
		if(_body != null) {
			String previousFileName = CodeLocation.getCurrentFileName();
			CodeLocation.setCurrentFileName(_bodyFileName);
			_parametersMap = new LinkedHashMap<IBinding, FuncParameter>();
			int size = 0;
			if(parameterValues != null)
				size = parameterValues.size();
			for(int i = 0; i < size; ++i) {
				FuncParameter callerParam = parameterValues.get(i);
				_parametersMap.put(_originalParameters.get(i), callerParam);
			}

			loadConstructorChain(_gvplGraph, caller);
			loadDefinition(_gvplGraph);

			CodeLocation.setCurrentFileName(previousFileName);
		}
		else {
			if(_astInterpreter.scriptFunctionExists(this))
				_astInterpreter.callScriptFunction(this, parameterValues);
			else
				loadHeaderOnlyFunc(parameterValues, extGraph);
		}

		extGraph.addSubGraph(_gvplGraph);
		
		_gvplGraph = null;
		_parametersMap = null;
		
		return new Value(_returnVar);
	}

	private void setName(String name) {
		_externalName = name;
		//_gvplGraph.setLabel(name);
	}

	public String getName() {
		return _externalName;
	}

	public void setReturnValue(Value returnValue) {
		if(_returnVar instanceof MemAddressVar)
			((MemAddressVar)_returnVar).setPointedVar(returnValue.getVar());
		else
			_returnVar.receiveAssign(NodeType.E_RETURN_VALUE, returnValue, _gvplGraph);
	}

	@Override
	public Function getFunction() {
		return this;
	}

	public static IndirectionType getIndirectionType(IASTPointerOperator[] pointerOps) {
		if (pointerOps == null)
			return IndirectionType.E_VARIABLE;

		if (pointerOps.length > 0) {
			if (pointerOps[0] instanceof IASTPointer)
				return IndirectionType.E_POINTER;
			else if (pointerOps[0] instanceof CPPASTReferenceOperator)
				return IndirectionType.E_REFERENCE;
		} else
			return IndirectionType.E_VARIABLE;
		Function.logger.fatal("error not expected");
		return null;
	}
	
	private void addParameter(IBinding binding, FuncParameter parameter) {
		_originalParametersMap.put(binding, parameter);
		_originalParameters.add(binding);
	}
	
	public FuncParameter getOriginalParameter(int index) {
		return _originalParametersMap.get(_originalParameters.get(index));
	}
	
	public int getNumParameters() {
		return _originalParameters.size();
	}
	
	//TODO improve! Can be done with bindings? There is a function in Eclipse IDE that
	// do this. It points the implementations of a function
	/**
	 * Returns true if the declarations are equivalent. It's used to verify if one function
	 * is the implementation of the other in a derived class 
	 * @param other Other function to compare
	 * @return True if the two functions are equivalent
	 */
	public boolean isDeclarationEquivalent(Function other) {
		if(!_funcName.equals(other._funcName))
			return false;
		
		if(_returnType != other._returnType)
			return false;
		
		if(_originalParameters.size() != other._originalParameters.size())
			return false;
		
		//TODO review if this have to be done every time
		List<FuncParameter> parameters = new ArrayList<FuncParameter>();
		for(int i = 0; i < getNumParameters(); i++)
			parameters.add(getOriginalParameter(i));
			
		return isEquivalentParameterList(parameters);
	}
	
	public boolean isEquivalentParameterList(List<FuncParameter> parametersList) {
		for(int i = 0; i < _originalParameters.size(); ++i) {
			FuncParameter internal = getOriginalParameter(i);
			FuncParameter external = parametersList.get(i);
			if(!internal.isEquivalent(external))
				return false;
		}
		
		return true;
	}
	
	public IBinding getBinding() {
		return _ownBinding;
	}

	public Value addReturnStatement(Value rvalue, TypeId type, String functionName, Graph graph) {
		IVar varDecl = addVarDecl(functionName, type, IndirectionType.E_VARIABLE, graph, _astInterpreter);
		varDecl.receiveAssign(NodeType.E_RETURN_VALUE, rvalue, _gvplGraph);
		return new Value(varDecl);
	}
	
	@Override
	public String toString() {
		return _externalName;
	}

	@Override
	protected GraphNode getNodeFromExpr(IASTNode expr) {
		//if it's a local var, return it's node
		GraphNode node = super.getNodeFromExpr(expr);
		if(node != null)
			return node;
		
		//if it's a function parameter, return it's node
		IBinding parameterBinding;
		try {
			parameterBinding = getBindingFromExpr(expr);
		} catch (NotFoundException e) {
			return _gvplGraph.addGraphNode("PROBLEM_NODE_" + e.getItemName(), NodeType.E_INVALID_NODE_TYPE);
		}
		FuncParameter funcParameter = _parametersMap.get(parameterBinding);
		if(funcParameter != null) {
			return funcParameter.getValue().getNode();
		}
		
		logger.error("should not be here, wtf is '{}'?", expr.getRawSignature());
		return null;
	}
	
	@Override
	public IVar getVarFromBinding(IBinding binding, CodeLocation codeLoc) {
		ExecTreeLogger.log(binding.getName());
		FuncParameter funcParameter = _parametersMap.get(binding);
		if(funcParameter != null) {
			Value value = funcParameter.getValue();
			if(value == null)
				return null;
			return value.getVar();
		}
		
		IVar var = getLocalVar(binding);
		if(var != null)
			return var;
		
		if(_parent != null)
			return _parent.getVarFromBinding(binding, codeLoc);
		
		return null;
	}
	
	public boolean getIsStatic() {
		return _isStatic;
	}

}
