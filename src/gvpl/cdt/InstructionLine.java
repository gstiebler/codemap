package gvpl.cdt;

import gvpl.cdt.CppMaps.eAssignBinOp;
import gvpl.cdt.CppMaps.eBinOp;
import gvpl.cdt.function.Function;
import gvpl.cdt.function.MemberFunc;
import gvpl.common.ClassVar;
import gvpl.common.CodeLocation;
import gvpl.common.FuncParameter;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.common.IVar;
import gvpl.common.MemAddressVar;
import gvpl.common.PointerVar;
import gvpl.common.TypeId;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArraySubscriptExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCaseStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTConstructorInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDefaultStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeleteExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNewExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSwitchStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;

import debug.DebugOptions;

public class InstructionLine {
	
	static Logger logger = LogManager.getLogger(InstructionLine.class.getName());

	private Graph _gvplGraph;
	private AstInterpreterCDT _astInterpreter;
	private AstLoaderCDT _parentAstLoader;

	public InstructionLine(Graph gvplGraph, AstLoaderCDT parent, AstInterpreterCDT astInterpreter) {
		_gvplGraph = gvplGraph;
		_astInterpreter = astInterpreter;
		_parentAstLoader = parent;
	}

	public void load(IASTStatement statement) {
		CodeLocation codeLocation = CodeLocationCDT.NewFromFileLocation(statement.getFileLocation());
		logger.debug(" --- Code location: {}", codeLocation);
		DebugOptions.setStartingLine(codeLocation.getStartingLine());
		logger.debug("statement is: {}", statement.getClass());
		if (statement instanceof IASTDeclarationStatement) {// variable
															// declaration
			IASTDeclarationStatement declStatement = (IASTDeclarationStatement) statement;
			IASTDeclaration decl = declStatement.getDeclaration();
			if (!(decl instanceof IASTSimpleDeclaration))
				logger.fatal("Deu merda aqui.");

			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) decl;
			IASTDeclSpecifier declSpec = simpleDecl.getDeclSpecifier();

			TypeId type = _astInterpreter.getType(declSpec);

			IASTDeclarator[] declarators = simpleDecl.getDeclarators();
			for (IASTDeclarator declarator : declarators) {
				// possibly more than one variable per line
				IVar varDecl = _parentAstLoader.loadVarDecl(declarator, type, _gvplGraph);
				LoadVariableInitialization(varDecl, declarator);
			}
		} else if (statement instanceof IASTExpression)
			loadValue((IASTExpression) statement);
		else if (statement instanceof IASTForStatement)
			loadForStmt((IASTForStatement) statement);
		else if (statement instanceof IASTExpressionStatement) {
			IASTExpressionStatement exprStat = (IASTExpressionStatement) statement;
			IASTExpression expr = exprStat.getExpression();
			if (expr instanceof IASTBinaryExpression)
				loadAssignBinOp((IASTBinaryExpression) expr);
			else if (expr instanceof IASTFunctionCallExpression)
				loadFunctionCall((IASTFunctionCallExpression) expr);
			else
				loadDeleteOp((CPPASTDeleteExpression) expr);
		} else if (statement instanceof IASTReturnStatement) {
			loadReturnStatement((IASTReturnStatement) statement);
		} else if (statement instanceof IASTIfStatement) {
			IfConditionCDT.loadIfCondition((IASTIfStatement) statement, this);
		} else if (statement instanceof IASTCompoundStatement) {
			BasicBlockCDT basicBlockLoader = new BasicBlockCDT(_parentAstLoader, _astInterpreter);
			basicBlockLoader.load(statement);
			basicBlockLoader.addToExtGraph();
			basicBlockLoader.bindSettedPointers();
		} else if (statement instanceof CPPASTSwitchStatement) {
			loadSwitch((CPPASTSwitchStatement) statement);
		} else
			logger.fatal("Node type not found!! Node: " + statement.toString());
	}
	
	private void loadSwitch(CPPASTSwitchStatement switchStatement) {
		IASTExpression controlExpr = switchStatement.getControllerExpression();
		CPPASTCompoundStatement body = (CPPASTCompoundStatement) switchStatement.getBody();
		GraphNode controlNode = loadValue(controlExpr);
		int numStatements = body.getStatements().length;
		
		IASTStatement defaultStatement = null;
		for (int i = 0; i < numStatements; i++) {
			IASTStatement statement = null;
			statement = body.getStatements()[i];
			if (!(statement instanceof CPPASTDefaultStatement)) continue;
			
			defaultStatement = body.getStatements()[i + 1];
		}
		
		boolean first = true;
		for(int i = 0; i < numStatements; ) {
			IASTStatement statement = null;
			GraphNode condExpr = null;
			for(;i < numStatements; i++){
				statement = body.getStatements()[i];
				if(statement instanceof CPPASTCaseStatement) {
					IASTExpression caseExpr = ((CPPASTCaseStatement) statement).getExpression();
					condExpr = loadValue(caseExpr);
				} else if (statement instanceof CPPASTExpressionStatement) {
					i++;
					break;
				}
			}
			
			//TODO deal with default in switch
			if(condExpr == null)
				break;
			
			IASTStatement elseStatement = null;
			if(first == true)
				elseStatement = defaultStatement;
			
			GraphNode compareOpNode = _gvplGraph.addGraphNode("==", NodeType.E_OPERATION);
			condExpr.addDependentNode(compareOpNode);
			controlNode.addDependentNode(compareOpNode);

			IfConditionCDT.loadIfCondition(compareOpNode, statement, elseStatement, this);
			first = false;
		}
	}
	
	void loadDeleteOp(CPPASTDeleteExpression deleteExpr) {
		IASTExpression opExpr = deleteExpr.getOperand();
		IVar var = _parentAstLoader.getVarFromExpr(opExpr);
		MemAddressVar mav = (MemAddressVar) var;
		var = mav.getVarInMem();
		((ClassVar) var).callDestructor(_parentAstLoader, _gvplGraph);
		mav.delete();
	}

	/**
	 * Loads the value of the variable, like in "int a = b;"
	 * 
	 * @param lhsVar
	 *            The variable to be initialized
	 * @param decl
	 *            Code with the declaration
	 */
	public void LoadVariableInitialization(IVar lhsVar, IASTDeclarator decl) {
		IASTInitializer initializer = decl.getInitializer();	
		if(initializer == null) //variable is not initialized
		{
			if(!(lhsVar instanceof ClassVar))
				return;
			
			ClassVar classVar = (ClassVar) lhsVar;
			classVar.callConstructor(new ArrayList<FuncParameter>(), NodeType.E_VARIABLE, _gvplGraph, 
						_parentAstLoader, _astInterpreter);
			return;
		}
		
		if (initializer instanceof CPPASTConstructorInitializer) { 
			// format: int a(5);
			CPPASTConstructorInitializer constrInit = (CPPASTConstructorInitializer) initializer;
			IASTExpression initExpr = constrInit.getExpression();
			loadConstructorInitializer(lhsVar, initExpr);														
			return;
		}
		
		IASTInitializerExpression init_exp = (IASTInitializerExpression) initializer;
		IASTExpression rhsExpr = init_exp.getExpression();

		if (lhsVar instanceof PointerVar) {
			loadRhsPointer((PointerVar) lhsVar, rhsExpr);
			return;
		}

		GraphNode rhsValue = loadValue(rhsExpr);
		lhsVar.receiveAssign(NodeType.E_VARIABLE, rhsValue, _parentAstLoader.getGraph());
	}
	
	public void loadConstructorInitializer(IVar lhsVar, IASTExpression initExpr) {
		List<FuncParameter> parameterValues = null;
		if(lhsVar instanceof ClassVar) {
			ClassVar classVar = (ClassVar) lhsVar;
			int numParameters = getChildExpressions(initExpr).length;
			Function constructorFunc = classVar.getClassDecl().getConstructorFunc(numParameters);
			parameterValues = loadFunctionParameters(constructorFunc, initExpr);
		} else {
			parameterValues = new ArrayList<FuncParameter>();
			GraphNode node = loadValue(initExpr);
			FuncParameter funcParameter = new FuncParameter(node, IndirectionType.E_INDIFERENT);
			parameterValues.add(funcParameter);
		}
		lhsVar.callConstructor(parameterValues, NodeType.E_VARIABLE, _gvplGraph,
				_parentAstLoader, _astInterpreter);
	}

	/*
	 * @brief Alguma coisa que retorna um valor
	 */
	public GraphNode loadValue(IASTExpression expr) {
		logger.debug("Node type {}", expr.getClass());
		// Eh uma variavel
		if (expr instanceof IASTIdExpression) {
			return _parentAstLoader.getNodeFromExpr(expr);
		} else if (expr instanceof IASTBinaryExpression) {// Eh uma expressao
			return loadBinOp((IASTBinaryExpression) expr);
		} else if (expr instanceof IASTLiteralExpression) {// Eh um valor direto
			return loadDirectValue((IASTLiteralExpression) expr);
		} else if (expr instanceof IASTFunctionCallExpression) {// Eh umachamada
																// a funcao
			return loadFunctionCall((IASTFunctionCallExpression) expr);
		} else if (expr instanceof IASTFieldReference) {// reference to field of
														// a struct
			IVar varDecl = _parentAstLoader.getVarFromFieldRef((IASTFieldReference) expr);
			if(varDecl == null)
				return GraphNode.newGarbageNode(_gvplGraph, "INVALID_READ");
			return varDecl.getCurrentNode();
		} else if (expr instanceof IASTUnaryExpression) {
			return loadUnaryExpr((IASTUnaryExpression) expr);
		} else if (expr instanceof CPPASTArraySubscriptExpression) {			
			//It's an array
			CPPASTArraySubscriptExpression arraySubscrExpr = (CPPASTArraySubscriptExpression) expr;
			IASTExpression arrayExpr = arraySubscrExpr.getArrayExpression();
			IVar varDecl = _parentAstLoader.getVarFromExpr(arrayExpr);
			//TODO use the index!!
			//IASTExpression index = arraySubscrExpr.getSubscriptExpression();
			return varDecl.getCurrentNode();
		} else
			logger.fatal("Node type not found!! Node: " + expr.getClass());

		return null;
	}

	private void loadForStmt(IASTForStatement node) {
		ForLoop forLoop = new ForLoop(_parentAstLoader, _astInterpreter);
		forLoop.load(node, _gvplGraph, _parentAstLoader);
	}

	private void loadReturnStatement(IASTReturnStatement statement) {
		IASTReturnStatement returnStat = (IASTReturnStatement) statement;

		GraphNode rvalue = loadValue(returnStat.getReturnValue());

		Function function = _parentAstLoader.getFunction();
		TypeId returnType = function.getReturnTypeId();

		// TODO set the correct type of the return value
		GraphNode returnNode = ((Function)_parentAstLoader).addReturnStatement(rvalue, returnType,
				function.getName(), _gvplGraph);

		function.setReturnNode(returnNode);
	}

	/**
	 * Loads a binary operation with an assignment
	 * 
	 * @param binExpr
	 *            The cpp node of this operation
	 * @return The graph node of the result of the operation
	 */
	GraphNode loadAssignBinOp(IASTBinaryExpression binExpr) {
		IASTExpression lhsOp = binExpr.getOperand1();
		logger.debug("get lhsVar");
		IVar lhsVar = _parentAstLoader.getVarFromExpr(lhsOp);
		
		if(lhsVar instanceof ClassVar) {
			return loadOperatorOverload(binExpr);
		}
		
		IASTExpression rhsExpr = binExpr.getOperand2();

		logger.debug("lhsOp class: {}", lhsOp.getClass());
		// check if we're trying to read a the instance of a pointer
		if (lhsOp instanceof IASTUnaryExpression) {
			logger.warn("not implemented");
		} else if (lhsOp instanceof CPPASTArraySubscriptExpression) {
			logger.warn("not imlemented");
		} else if (lhsVar instanceof PointerVar) {
			loadRhsPointer((PointerVar) lhsVar, rhsExpr);
			return null;
		}

		logger.debug("loading rhs value");
		GraphNode rhsValue = loadValue(rhsExpr);

		if (binExpr.getOperator() == IASTBinaryExpression.op_assign) {
			lhsVar.receiveAssign(NodeType.E_VARIABLE, rhsValue, _parentAstLoader.getGraph());
			return null;
		}

		GraphNode lhsValue = loadValue(binExpr.getOperand1());
		eAssignBinOp op = CppMaps.getAssignBinOpTypes(binExpr.getOperator());
		return _gvplGraph.addAssignBinOp(op, lhsVar, lhsValue, rhsValue, _parentAstLoader);
	}
	
	private GraphNode loadOperatorOverload(IASTBinaryExpression binExpr) {
		IASTExpression lhsOp = binExpr.getOperand1();
		ClassVar lhsVar = (ClassVar) _parentAstLoader.getVarFromExpr(lhsOp);
		
		IASTExpression rhsOp = binExpr.getOperand2();
		IVar rhsVar = _parentAstLoader.getVarFromExpr(rhsOp);
		
		MemberFunc opFunc = lhsVar.getClassDecl().getOpFunc(binExpr.getOperator());
		List<FuncParameter> parameterValues = new ArrayList<FuncParameter>();
		parameterValues.add(new FuncParameter(rhsVar, IndirectionType.E_REFERENCE));
		return opFunc.addFuncRef(parameterValues, _gvplGraph, lhsVar);
	}

	void loadRhsPointer(PointerVar lhsPointer, IASTExpression rhsOp) {
		logger.debug("loading pointer of type: {}", rhsOp.getClass());
		if (rhsOp instanceof CPPASTNewExpression) {
			CPPASTNewExpression newExpr = (CPPASTNewExpression) rhsOp;
			IASTDeclSpecifier namedSpec = newExpr.getTypeId().getDeclSpecifier();
			
			ClassDeclCDT classDecl = null;
			if(namedSpec instanceof CPPASTNamedTypeSpecifier) {
				CPPASTNamedTypeSpecifier typeSpec = (CPPASTNamedTypeSpecifier) namedSpec;
				IBinding funcBinding = typeSpec.getName().resolveBinding();
				classDecl = _astInterpreter.getClassFromFuncBinding(funcBinding);
			}
			
			if (classDecl == null) {
				lhsPointer.initializeVar(NodeType.E_VARIABLE, _gvplGraph, _parentAstLoader,
						_astInterpreter);
				return;
			}


			List<FuncParameter> parameterValues = null;
			IASTExpression expr = ((CPPASTNewExpression) rhsOp).getNewInitializer();
			int numParameters = getChildExpressions(expr).length;
			Function constructorFunc = classDecl.getConstructorFunc(numParameters);
			parameterValues = loadFunctionParameters(constructorFunc, expr);
			lhsPointer.constructor(parameterValues, NodeType.E_VARIABLE, _gvplGraph,
					_parentAstLoader, _astInterpreter, classDecl.getTypeId());
			return;
		} else {
			IVar rhsPointer = loadPointedVar(rhsOp, _parentAstLoader);
			lhsPointer.setPointedVar(rhsPointer);
			return;
		}
	}

	private GraphNode loadFunctionCall(IASTFunctionCallExpression funcCall) {
		IASTExpression paramExpr = funcCall.getParameterExpression();
		IASTExpression nameExpr = funcCall.getFunctionNameExpression();
		
		if (nameExpr instanceof IASTIdExpression)
		{
			IASTIdExpression idExpr = (IASTIdExpression) nameExpr;
			IASTName name = idExpr.getName();
			IBinding idExprBinding = name.resolveBinding();
			
			if (idExprBinding instanceof CPPMethod) {
				if (name instanceof CPPASTQualifiedName) { // static function
					return loadStaticMethod((CPPASTQualifiedName) name, paramExpr);
				} else if (name instanceof CPPASTName) // method from own class
					return loadOwnMethod(idExprBinding, paramExpr);
				else
					logger.fatal("you're doing it wrong");
				return null;
			} else if (idExprBinding instanceof CPPFunction) {
				return loadSimpleFunc(idExprBinding, paramExpr);
			} else
				logger.fatal("problem: instance: {}", idExprBinding.getClass());
		} else if (nameExpr instanceof IASTFieldReference) {
			return loadVarMethod(funcCall, paramExpr);
		} else
			logger.fatal("problem");

		return null;
	}
	
	GraphNode loadStaticMethod(CPPASTQualifiedName qName, IASTExpression paramExpr) {
		IASTName[] names = qName.getNames();
		IASTName className = names[0];
		IASTName funcName = names[1];
		ClassDeclCDT classDecl = _astInterpreter.getClassDecl(className.resolveBinding());
		Function func = classDecl.getMemberFunc(funcName.resolveBinding());
		List<FuncParameter> parameterValues = loadFunctionParameters(func, paramExpr);
		return func.addFuncRef(parameterValues, _gvplGraph);
	}
	
	/**
	 * Loads a simple function (i.e., a function that is not a instance method)
	 * @param idExprBinding
	 * @param paramExpr
	 * @param stLine
	 * @return
	 */
	private GraphNode loadSimpleFunc(IBinding idExprBinding, IASTExpression paramExpr) {
		Function func = _astInterpreter.getFuncId(idExprBinding);

		List<FuncParameter> parameterValues = loadFunctionParameters(func, paramExpr);
		Function loadFunction = _astInterpreter.getFuncId(idExprBinding);
		return loadFunction.addFuncRef(parameterValues, _gvplGraph);
	}
	
	/**
	 * Used when a method call a method of it's own instance
	 * @param idExprBinding
	 * @param paramExpr
	 * @param stLine
	 * @return The graph node of the result of the function
	 */
	private GraphNode loadOwnMethod(IBinding idExprBinding, IASTExpression paramExpr) {
		MemberFunc parentMF = (MemberFunc) _parentAstLoader;
		Function func = parentMF.getParentClass().getMemberFunc(idExprBinding);
		
		List<FuncParameter> parameterValues = loadFunctionParameters(func, paramExpr);
		MemberFunc memberFunc = (MemberFunc) func;
		ClassVar var = parentMF.getThisReference();
		GraphNode node =  memberFunc.addFuncRef(parameterValues, _gvplGraph, var);
		return node;
	}
	
	/**
	 * Used to call the method of a variable
	 * @param funcCall
	 * @param paramExpr
	 * @return The graph node of the result of the function
	 */
	private GraphNode loadVarMethod(IASTFunctionCallExpression funcCall, IASTExpression paramExpr) {
		IASTFieldReference fieldRef = (IASTFieldReference) funcCall.getFunctionNameExpression();
		IASTExpression ownerExpr = fieldRef.getFieldOwner();
		IVar var = _parentAstLoader.getVarFromExpr(ownerExpr);
		IBinding funcMemberBinding = fieldRef.getFieldName().resolveBinding();
		
		TypeId typeId = var.getType();
		ClassDeclCDT classDecl =_astInterpreter.getClassDecl(typeId);
		MemberFunc memberFunc = classDecl.getMemberFunc(funcMemberBinding);

		List<FuncParameter> parameterValues = loadFunctionParameters(memberFunc, paramExpr);
		
		if(var instanceof ClassVar) {
			return ((ClassVar)var).loadMemberFuncRef(memberFunc, parameterValues, _gvplGraph,
					_parentAstLoader);
		} else if(var instanceof MemAddressVar) {
			return ((MemAddressVar)var).loadMemberFuncRef(memberFunc, parameterValues, _gvplGraph,
					_parentAstLoader);
		} 
		return null;
	}

	private IASTExpression[] getChildExpressions(IASTExpression paramExpr) {
		if (paramExpr instanceof IASTExpressionList) {
			IASTExpressionList exprList = (IASTExpressionList) paramExpr;
			return exprList.getExpressions();
		} else {
			IASTExpression[] parameters = new IASTExpression[1];
			parameters[0] = paramExpr;
			return parameters;
		}
	}
	
	public List<FuncParameter> loadFunctionParameters(Function func, IASTExpression paramExpr) {
		List<FuncParameter> parameterValues = new ArrayList<FuncParameter>();
		if (paramExpr == null)
			return parameterValues;

		IASTExpression[] parameters = getChildExpressions(paramExpr);
		if(parameters.length != func.getNumParameters())
			logger.fatal("Number of parameters are different!");

		for (int i = 0; i < parameters.length; i++) {
			IASTExpression parameter = parameters[i];
			FuncParameter localParameter = null;
			FuncParameter insideFuncParameter = func.getOriginalParameter(i);

			if (insideFuncParameter.getType() == IndirectionType.E_POINTER)
				localParameter = new FuncParameter(loadVarInAddress(parameter, _parentAstLoader),
						IndirectionType.E_POINTER);
			else if (insideFuncParameter.getType() == IndirectionType.E_REFERENCE) {
				IVar var = _parentAstLoader.getVarFromExpr(parameter);
				localParameter = new FuncParameter(var, IndirectionType.E_REFERENCE);
			} else if (insideFuncParameter.getType() == IndirectionType.E_VARIABLE)
				localParameter = new FuncParameter(loadValue(parameter), IndirectionType.E_VARIABLE);
			else
				logger.fatal("Work here ");

			parameterValues.add(localParameter);
		}

		return parameterValues;
	}

	GraphNode loadBinOp(IASTBinaryExpression binOp) {
		eBinOp op = CppMaps.getBinOpType(binOp.getOperator());
		GraphNode lvalue = loadValue(binOp.getOperand1());
		GraphNode rvalue = loadValue(binOp.getOperand2());
		return _gvplGraph.addBinOp(op, lvalue, rvalue, _parentAstLoader);
	}

	GraphNode loadDirectValue(IASTLiteralExpression node) {
		String value = node.toString();
		return _gvplGraph.addDirectVal(value);
	}

	/**
	 * Returns the var that is pointed by the address For example, in "b = &d;"
	 * the function receives "&d" and returns the variable of "d"
	 * 
	 * @param address
	 *            Address that contains the variable
	 * @return The var that is pointed by the address
	 */
	public static IVar loadVarInAddress(IASTExpression address, AstLoaderCDT astLoader) {
		if (!(address instanceof IASTUnaryExpression)) {
			// it's receiving the address from another pointer, like
			// "int *b; int *a = b;"
			IVar DirectVarDecl = astLoader.getVarFromExpr(address);
			if (DirectVarDecl instanceof PointerVar)
				return ((PointerVar) DirectVarDecl).getVarInMem();
			else
				return DirectVarDecl;
		}

		// It's getting the address of a reference, like "int a = &b;"

		IASTUnaryExpression unaryExpr = (IASTUnaryExpression) address;
		// Check if the operator is a reference
		if (unaryExpr.getOperator() != IASTUnaryExpression.op_amper)
			logger.fatal("not expected here!!");

		IASTExpression op = unaryExpr.getOperand();
		return astLoader.getVarFromExpr(op);
	}

	/**
	 * Currently used to read the value of "*b"
	 * 
	 * @param unExpr
	 * @return
	 */
	GraphNode loadUnaryExpr(IASTUnaryExpression unExpr) {
		// Check if the operator is a star
		if (unExpr.getOperator() == CPPASTUnaryExpression.op_star){
			IASTExpression opExpr = unExpr.getOperand();
			IVar pointerVar = _parentAstLoader.getVarFromExpr(opExpr);
			if (!(pointerVar instanceof PointerVar))
				logger.fatal("not expected here");

			return pointerVar.getCurrentNode();
		} else if (unExpr.getOperator() == CPPASTUnaryExpression.op_bracketedPrimary) {
			return loadValue(unExpr.getOperand());
		} else
			logger.fatal("not implemented");
		return null;
	}

	/**
	 * Returns the variable that is currently pointed by the received pointer
	 * For example, in "b = &d; c = *b;" in the second line, the function will
	 * receive "b" as pointerExpr and will return the variable of "d"
	 * 
	 * @param pointerExpr
	 *            Expression of a pointer variable
	 * @param astLoader
	 * @return The variable that is currently pointed by the received pointer
	 */
	public static IVar loadPointedVar(IASTExpression pointerExpr, AstLoaderCDT astLoader) {
		IVar pointerVar = astLoader.getVarFromExpr(pointerExpr);
		if (pointerVar instanceof PointerVar)
			return ((PointerVar) pointerVar).getVarInMem();
		else
			return loadVarInAddress(pointerExpr, astLoader);
	}

	public AstLoaderCDT getParentBasicBlock() {
		return _parentAstLoader;
	}
	
	public AstInterpreterCDT getAstInterpreter() {
		return _astInterpreter;
	}
	
	public Graph getGraph() {
		return _gvplGraph;
	}
}
