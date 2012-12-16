package gvpl.cdt.function;

import gvpl.cdt.AstInterpreterCDT;
import gvpl.cdt.AstLoaderCDT;
import gvpl.cdt.CodeLocationCDT;
import gvpl.cdt.InstructionLine;
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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
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
	private Map<IBinding, FuncParameter> _originalParametersMap = new LinkedHashMap<IBinding, FuncParameter>();
	private List<IBinding> _originalParameters = new ArrayList<IBinding>();
	
	private Map<IBinding, FuncParameter> _parametersMap = null;
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
		
		_declLocation = CodeLocationCDT.NewFromFileLocation(decl.getFileLocation());
	}
	
	public void loadDefinition(ICPPASTConstructorChainInitializer[] ccInitializer, IASTStatement body) {
		_ccInitializer = ccInitializer;
		_body = body;
	}
	
	public void loadDefinition(Graph gvplGraph) {
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

	void loadConstructorChain(Graph graph) {
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
			IBinding binding = parameterVarDecl.getName().resolveBinding();

			FuncParameter.IndirectionType parameterVarType = null;
			parameterVarType = getIndirectionType(parameter.getDeclarator().getPointerOperators());
			FuncParameter funcParameter = new FuncParameter((IVar)null, parameterVarType);
			if(declSpec instanceof CPPASTSimpleDeclSpecifier)
				funcParameter.setType(((CPPASTSimpleDeclSpecifier)declSpec).getType());

			addParameter(binding, funcParameter);
		}
	}

	public GraphNode addFuncRef(List<FuncParameter> parameterValues, Graph extGraph) {
		_gvplGraph = new Graph(_externalName);
		_parametersMap = new LinkedHashMap<>();
		int size = 0;
		if(parameterValues != null)
			size = parameterValues.size();
		for(int i = 0; i < size; ++i) {
			_parametersMap.put(_originalParameters.get(i), parameterValues.get(i));
		}

		loadConstructorChain(_gvplGraph);
		loadDefinition(_gvplGraph);
		extGraph.addSubGraph(_gvplGraph);
		_gvplGraph = null;
		_parametersMap = null;
		
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
	
	@Override
	public String toString() {
		return _externalName;
	}

	@Override
	protected GraphNode getNodeFromExpr(IASTExpression expr) {
		GraphNode node = super.getNodeFromExpr(expr);
		if(node != null)
			return node;
		
		IBinding parameterBinding = getBindingFromExpr(expr);
		FuncParameter funcParameter = _parametersMap.get(parameterBinding);
		if(funcParameter != null)
			return funcParameter.getNode();
		
		logger.fatal("should not be here");
		return null;
	}
	
	@Override
	protected IVar getVarFromBinding(IBinding binding) {
		FuncParameter funcParameter = _originalParametersMap.get(binding);
		if(funcParameter != null)
			return funcParameter.getVar();
		
		return getLocalVar(binding);
	}

	@Override
	protected IVar getVarFromExpr(IASTExpression expr) {
		IVar var = super.getVarFromExpr(expr);

		if (var != null) 
			return var; 
		
		IBinding parameterBinding = getBindingFromExpr(expr);
		FuncParameter funcParameter = _parametersMap.get(parameterBinding);
		if(funcParameter != null)
			return funcParameter.getVar();
		
		return null;
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
