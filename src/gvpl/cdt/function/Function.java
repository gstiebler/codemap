package gvpl.cdt.function;

import gvpl.cdt.AstInterpreterCDT;
import gvpl.cdt.AstLoaderCDT;
import gvpl.cdt.CodeLocationCDT;
import gvpl.cdt.InstructionLine;
import gvpl.common.ClassVar;
import gvpl.common.CodeLocation;
import gvpl.common.FuncParameter;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.common.IVar;
import gvpl.common.TypeId;
import gvpl.common.VarInfo;
import gvpl.graph.Graph;
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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReferenceOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;

public class Function extends AstLoaderCDT {
	
	static Logger logger = LogManager.getLogger(Graph.class.getName());
	
	private GraphNode _returnNode = null;
	private TypeId _returnType = null;

	private String _externalName = "";
	private Map<IBinding, FuncParameter> _parametersMap = new LinkedHashMap<IBinding, FuncParameter>();
	private List<FuncParameter> _parametersList = new ArrayList<FuncParameter>();
	protected String _funcName;
	protected IBinding _ownBinding;
	CodeLocation _declLocation = null;
	CodeLocation _implLocation = null;
	
	ICPPASTConstructorChainInitializer[] _ccInitializer;
	IASTStatement _body;

	public Function(Graph gvplGraph, AstInterpreterCDT astInterpreter, IBinding ownBinding) {
		super(astInterpreter);
		
		_ownBinding = ownBinding;
		//TODO FIX!!
		_returnType = _astInterpreter.getPrimitiveType();
	}
	
	public void loadDeclaration(CPPASTFunctionDeclarator decl) {
		IASTParameterDeclaration[] parameters = decl.getParameters();
		IASTName astName = decl.getName();
		// Gets the name of the function
		if (astName instanceof CPPASTQualifiedName)
			_funcName = ((CPPASTQualifiedName) astName).getNames()[1].toString();
		else
			_funcName = astName.toString();

		logger.debug("Loading declaration of func {}", _funcName);
		
		setName(calcName());

		loadFuncParameters(parameters);
		
		_declLocation = CodeLocationCDT.NewFromFileLocation(decl.getFileLocation());
	}
	
	public void loadDefinition(ICPPASTConstructorChainInitializer[] ccInitializer, IASTStatement body) {
		_ccInitializer = ccInitializer;
		_body = body;
	}
	
	public void loadDefinition(Graph gvplGraph) {
		if(_implLocation != null) //function definition has already been loaded
			return;
		
		loadConstructorChain(_ccInitializer);
		
		if (_body instanceof IASTCompoundStatement) {
			IASTStatement[] statements = ((IASTCompoundStatement)_body).getStatements();
			for (IASTStatement statement : statements) {
				InstructionLine instructionLine = new InstructionLine(gvplGraph, this, _astInterpreter);
				instructionLine.load(statement);
			}
		} else
			logger.fatal("Work here.");
		
		_implLocation = CodeLocationCDT.NewFromFileLocation(_body.getFileLocation());
		
		lostScope();
	}
	
	public TypeId getReturnTypeId() {
		return _returnType;
	}

	void loadConstructorChain(ICPPASTConstructorChainInitializer[] constructorInit, Graph graph, ClassVar thisVar) {
	}

	protected String calcName() {
		return _funcName;
	}

	/**
	 * Reads the parameters from a function declaration
	 * 
	 * @param parameters
	 *            Parameters of the function
	 * @param func_decl
	 *            Declaration of the function
	 * @param basicBlockLoader
	 *            The class which loads the function definition
	 */
	public void loadFuncParameters(IASTParameterDeclaration[] parameters) {
		for (IASTParameterDeclaration parameter : parameters) {
			IASTDeclarator parameterVarDecl = parameter.getDeclarator();
			IASTDeclSpecifier declSpec = parameter.getDeclSpecifier();
			TypeId type = _astInterpreter.getType(declSpec);
			IVar var_decl = loadVarDecl(parameterVarDecl, type);
			IBinding binding = parameterVarDecl.getName().resolveBinding();

			FuncParameter.IndirectionType parameterVarType = null;
			parameterVarType = getIndirectionType(parameter.getDeclarator().getPointerOperators());
			FuncParameter funcParameter = new FuncParameter(var_decl, parameterVarType);
			if(declSpec instanceof CPPASTSimpleDeclSpecifier)
				funcParameter.setType(((CPPASTSimpleDeclSpecifier)declSpec).getType());

			addParameter(binding, funcParameter);
		}
	}

	public GraphNode addFuncRef(List<FuncParameter> parameterValues, Graph extGraph) {
		_gvplGraph = new Graph(_externalName);
		
		private Map<IBinding, FuncParameter> _parametersMap = new LinkedHashMap<IBinding, FuncParameter>();
		private List<FuncParameter> _parametersList = new ArrayList<FuncParameter>();
		
		loadDefinition(funcGraph);
		extGraph.addSubGraph(funcGraph, this);
		_gvplGraph = null;
		
		return _returnNode;
	}

	private void setName(String name) {
		_externalName = name;
		//_gvplGraph.setLabel(name);
	}

	public String getName() {
		return _externalName;
	}

	public void setReturnNode(GraphNode returnNode) {
		_returnNode = returnNode;
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
		_parametersMap.put(binding, parameter);
		_parametersList.add(parameter);
	}
	
	public FuncParameter getParameter(int index) {
		return _parametersList.get(index);
	}
	
	public int getNumParameters() {
		return _parametersList.size();
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
		
		if(_parametersList.size() != other._parametersList.size())
			return false;
		
		return isEquivalentParameterList(other._parametersList);
	}
	
	public boolean isEquivalentParameterList(List<FuncParameter> parametersList) {
		for(int i = 0; i < _parametersList.size(); ++i) {
			FuncParameter internal = _parametersList.get(i);
			FuncParameter external = parametersList.get(i);
			if(!internal.isEquivalent(external))
				return false;
		}
		
		return true;
	}
	
	public IBinding getBinding() {
		return _ownBinding;
	}
	
	@Override
	public String toString() {
		return _externalName;
	}

	@Override
	protected IVar getVarFromBinding(IBinding binding) {
		FuncParameter funcParameter = _parametersMap.get(binding);
		if(funcParameter != null)
			return funcParameter.getVar();
		
		return getLocalVar(binding);
	}

	@Override
	protected IVar getPreLoadedVarFromBinding(IBinding binding) {
		return getVarFromBinding(binding);
	}
	
	@Override
	public VarInfo getTypeFromVarBinding(IBinding binding) {
		return getVarFromBinding(binding).getVarInfo();
	}

}
