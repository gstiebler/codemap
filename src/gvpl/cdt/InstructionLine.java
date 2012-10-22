package gvpl.cdt;

import gvpl.cdt.CppMaps.eAssignBinOp;
import gvpl.cdt.CppMaps.eBinOp;
import gvpl.common.ClassVar;
import gvpl.common.GeneralOutputter;
import gvpl.common.FuncParameter;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.common.PointerVar;
import gvpl.common.TypeId;
import gvpl.common.IVar;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNewExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;

public class InstructionLine {

	private Graph _gvplGraph;
	private AstInterpreter _astInterpreter;
	private AstLoader _parentBasicBlock;

	public InstructionLine(Graph gvplGraph, AstLoader parent, AstInterpreter astInterpreter) {
		_gvplGraph = gvplGraph;
		_astInterpreter = astInterpreter;
		_parentBasicBlock = parent;
	}

	public void load(IASTStatement statement) {
		int startingLine = statement.getFileLocation().getStartingLineNumber();
		if (statement instanceof IASTDeclarationStatement) {// variable
															// declaration
			IASTDeclarationStatement decl_statement = (IASTDeclarationStatement) statement;
			IASTDeclaration decl = decl_statement.getDeclaration();
			if (!(decl instanceof IASTSimpleDeclaration))
				GeneralOutputter.fatalError("Deu merda aqui.");

			IASTSimpleDeclaration simple_decl = (IASTSimpleDeclaration) decl;
			IASTDeclSpecifier decl_spec = simple_decl.getDeclSpecifier();

			TypeId type = _astInterpreter.getType(decl_spec);

			IASTDeclarator[] declarators = simple_decl.getDeclarators();
			for (IASTDeclarator declarator : declarators) {
				// possibly more than one variable per line
				IVar var_decl = _parentBasicBlock.loadVarDecl(declarator, type);
				LoadVariableInitialization(var_decl, declarator);
			}
		} else if (statement instanceof IASTExpression)
			loadValue((IASTExpression) statement);
		else if (statement instanceof IASTForStatement)
			load_for_stmt((IASTForStatement) statement);
		else if (statement instanceof IASTExpressionStatement) {
			IASTExpressionStatement exprStat = (IASTExpressionStatement) statement;
			IASTExpression expr = exprStat.getExpression();
			if (expr instanceof IASTBinaryExpression)
				loadAssignBinOp((IASTBinaryExpression) expr);
			else if (expr instanceof IASTFunctionCallExpression)
				loadFunctionCall((IASTFunctionCallExpression) expr);
		} else if (statement instanceof IASTReturnStatement) {
			loadReturnStatement((IASTReturnStatement) statement);
		} else if (statement instanceof IASTIfStatement) {
			IfCondition.loadIfCondition((IASTIfStatement) statement, this);
		} else if (statement instanceof IASTCompoundStatement) {
			BasicBlock basicBlockLoader = new BasicBlock(_parentBasicBlock, _astInterpreter);
			basicBlockLoader.load(statement);
			basicBlockLoader.addToExtGraph(startingLine);
			basicBlockLoader.bindSettedPointers();
		} else
			GeneralOutputter.fatalError("Node type not found!! Node: " + statement.toString());
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
		int startingLine = decl.getFileLocation().getStartingLineNumber();

		IASTInitializer initializer = decl.getInitializer();	
		if(initializer == null) //variable is not initialized
			return;
		
		if (initializer instanceof ICPPASTConstructorInitializer) { 
			// format: int a(5);
			IASTExpression initExpr = ((ICPPASTConstructorInitializer) initializer).getExpression();
			loadConstructorInitializer(lhsVar, initExpr, startingLine);														
			return;
		}
		
		IASTInitializerExpression init_exp = (IASTInitializerExpression) initializer;
		IASTExpression rhsExpr = init_exp.getExpression();

		if (lhsVar instanceof PointerVar) {
			loadRhsPointer((PointerVar) lhsVar, rhsExpr);
			return;
		}

		GraphNode rhsValue = loadValue(rhsExpr);
		lhsVar.receiveAssign(NodeType.E_VARIABLE, rhsValue, startingLine);
	}
	
	void loadConstructorInitializer(IVar lhsVar, IASTExpression initExpr, int startingLine) {
		List<FuncParameter> parameterValues = null;
		if(lhsVar instanceof ClassVar) {
			ClassVar classVar = (ClassVar) lhsVar;
			Function constructorFunc = classVar.getClassDecl().getConstructorFunc();
			parameterValues = loadFunctionParameters(constructorFunc, initExpr);
		} else {
			parameterValues = new ArrayList<FuncParameter>();
			GraphNode node = loadValue(initExpr);
			FuncParameter funcParameter = new FuncParameter(node, IndirectionType.E_INDIFERENT);
			parameterValues.add(funcParameter);
		}
		lhsVar.constructor(parameterValues, NodeType.E_VARIABLE, _gvplGraph,
				_parentBasicBlock, _astInterpreter, startingLine);
	}

	/*
	 * @brief Alguma coisa que retorna um valor
	 */
	public GraphNode loadValue(IASTExpression expr) {
		int startingLine = expr.getFileLocation().getStartingLineNumber();
		// Eh uma variavel
		if (expr instanceof IASTIdExpression) {
			IVar var_decl = _parentBasicBlock.getVarFromExpr(expr);
			return var_decl.getCurrentNode(startingLine);
		} else if (expr instanceof IASTBinaryExpression) {// Eh uma expressao
			return loadBinOp((IASTBinaryExpression) expr);
		} else if (expr instanceof IASTLiteralExpression) {// Eh um valor direto
			return loadDirectValue((IASTLiteralExpression) expr);
		} else if (expr instanceof IASTFunctionCallExpression) {// Eh umachamada
																// a funcao
			return loadFunctionCall((IASTFunctionCallExpression) expr);
		} else if (expr instanceof IASTFieldReference) {// reference to field of
														// a struct
			IVar var_decl = _parentBasicBlock.getVarFromFieldRef((IASTFieldReference) expr);
			return var_decl.getCurrentNode(expr.getFileLocation().getStartingLineNumber());
		} else if (expr instanceof IASTUnaryExpression) {
			return loadUnaryExpr((IASTUnaryExpression) expr);
		} else
			GeneralOutputter.fatalError("Node type not found!! Node: " + expr.getClass());

		return null;
	}

	private void load_for_stmt(IASTForStatement node) {
		int startingLine = node.getFileLocation().getStartingLineNumber();
		ForLoop forLoop = new ForLoop(_parentBasicBlock, _astInterpreter, startingLine);
		forLoop.load(node, _gvplGraph, _parentBasicBlock);
	}

	private void loadReturnStatement(IASTReturnStatement statement) {
		IASTReturnStatement return_node = (IASTReturnStatement) statement;

		GraphNode rvalue = loadValue(return_node.getReturnValue());

		Function function = _parentBasicBlock.getFunction();
		TypeId returnType = function.getReturnTypeId();

		// TODO set the correct type of the return value
		GraphNode returnNode = _parentBasicBlock.addReturnStatement(rvalue, returnType,
				function.getName(), statement.getFileLocation().getStartingLineNumber());

		function.setReturnNode(returnNode);
	}

	/**
	 * Loads a binary operation with an assignment
	 * 
	 * @param node
	 *            The cpp node of this operation
	 * @return The graph node of the result of the operation
	 */
	GraphNode loadAssignBinOp(IASTBinaryExpression node) {
		int startingLine = node.getFileLocation().getStartingLineNumber();
		IASTExpression lhsOp = node.getOperand1();
		IVar lhsVar = _parentBasicBlock.getVarFromExpr(lhsOp);
		IASTExpression rhsExpr = node.getOperand2();

		// check if we're trying to read a the instance of a pointer
		if (lhsOp instanceof IASTUnaryExpression) {

		} else if (lhsVar instanceof PointerVar) {
			loadRhsPointer((PointerVar) lhsVar, rhsExpr);
			return null;
		}

		GraphNode rhsValue = loadValue(rhsExpr);

		if (node.getOperator() == IASTBinaryExpression.op_assign) {
			lhsVar.receiveAssign(NodeType.E_VARIABLE, rhsValue, startingLine);
			return null;
		}

		GraphNode lhsValue = loadValue(node.getOperand1());
		eAssignBinOp op = CppMaps.getAssignBinOpTypes(node.getOperator());
		return _gvplGraph.addAssignBinOp(op, lhsVar, lhsValue, rhsValue, _parentBasicBlock,
				startingLine);
	}

	void loadRhsPointer(PointerVar lhsPointer, IASTExpression rhsOp) {
		int startingLine = rhsOp.getFileLocation().getStartingLineNumber();
		if (rhsOp instanceof CPPASTNewExpression) {
			CPPASTNewExpression newExpr = (CPPASTNewExpression) rhsOp;
			IASTDeclSpecifier namedSpec = newExpr.getTypeId().getDeclSpecifier();
			
			ClassDecl classDecl = null;
			if(namedSpec instanceof CPPASTNamedTypeSpecifier) {
				CPPASTNamedTypeSpecifier typeSpec = (CPPASTNamedTypeSpecifier) namedSpec;
				IBinding funcBinding = typeSpec.getName().resolveBinding();
				classDecl = _astInterpreter.getClassFromFuncBinding(funcBinding);
			}
			if (classDecl == null) {
				lhsPointer.initializeGraphNode(NodeType.E_VARIABLE, _gvplGraph, _parentBasicBlock,
						_astInterpreter, startingLine);
				return;
			}

			Function constructorFunc = classDecl.getConstructorFunc();

			List<FuncParameter> parameterValues = null;
			IASTExpression expr = ((CPPASTNewExpression) rhsOp).getNewInitializer();
			parameterValues = loadFunctionParameters(constructorFunc, expr);
			lhsPointer.constructor(parameterValues, NodeType.E_VARIABLE, _gvplGraph,
					_parentBasicBlock, _astInterpreter, classDecl.getTypeId(), startingLine);
			return;
		} else {
			IVar rhsPointer = loadPointedVar(rhsOp, _parentBasicBlock);
			lhsPointer.setPointedVar(rhsPointer);
			return;
		}
	}

	private GraphNode loadFunctionCall(IASTFunctionCallExpression funcCall) {
		int startingLine = funcCall.getFileLocation().getStartingLineNumber();
		
		IASTExpression paramExpr = funcCall.getParameterExpression();
		IASTExpression nameExpr = funcCall.getFunctionNameExpression();
		
		if (nameExpr instanceof IASTIdExpression)
		{
			IASTIdExpression idExpr = (IASTIdExpression) nameExpr;
			IBinding idExprBinding = idExpr.getName().resolveBinding();
			
			if (idExprBinding instanceof CPPMethod) {
				return loadOwnMethod(idExprBinding, paramExpr, startingLine);
			} else if (idExprBinding instanceof CPPFunction) {
				return loadSimpleFunc(idExprBinding, paramExpr, startingLine);
			} else
				GeneralOutputter.fatalError("problem");
		} else if (nameExpr instanceof IASTFieldReference) {
			return loadVarMethod(funcCall, paramExpr);
		} else
			GeneralOutputter.fatalError("problem");

		return null;
	}
	
	/**
	 * Loads a simple function (i.e., a function that is not a instance method)
	 * @param idExprBinding
	 * @param paramExpr
	 * @param stLine
	 * @return
	 */
	private GraphNode loadSimpleFunc(IBinding idExprBinding, IASTExpression paramExpr, int stLine) {
		Function func = _astInterpreter.getFuncId(idExprBinding);

		List<FuncParameter> parameterValues = loadFunctionParameters(func, paramExpr);
		Function loadFunction = _astInterpreter.getFuncId(idExprBinding);
		return loadFunction.addFuncRef(parameterValues, _gvplGraph, stLine);
	}
	
	/**
	 * Used when a method call a method of it's own instance
	 * @param idExprBinding
	 * @param paramExpr
	 * @param stLine
	 * @return The graph node of the result of the function
	 */
	private GraphNode loadOwnMethod(IBinding idExprBinding, IASTExpression paramExpr, int stLine) {
		MemberFunc parentMF = (MemberFunc) _parentBasicBlock;
		Function func = parentMF.getParentClass().getMemberFunc(idExprBinding);
		
		List<FuncParameter> parameterValues = loadFunctionParameters(func, paramExpr);
		MemberFunc memberFunc = (MemberFunc) func;
		ClassVar var = parentMF.getThisReference();
		GraphNode node =  memberFunc.loadMemberFuncRef(var, parameterValues, _gvplGraph,
			_parentBasicBlock, stLine);
		return node;
	}
	
	/**
	 * Used to call the method of a variable
	 * @param funcCall
	 * @param paramExpr
	 * @return The graph node of the result of the function
	 */
	private GraphNode loadVarMethod(IASTFunctionCallExpression funcCall, IASTExpression paramExpr) {
		int startingLine = funcCall.getFileLocation().getStartingLineNumber();
		IASTFieldReference fieldRef = (IASTFieldReference) funcCall.getFunctionNameExpression();
		IASTExpression ownerExpr = fieldRef.getFieldOwner();
		IVar var = _parentBasicBlock.getVarFromExpr(ownerExpr);
		IBinding funcMemberBinding = fieldRef.getFieldName().resolveBinding();
		
		TypeId typeId = var.getType();
		ClassDecl classDecl =_astInterpreter.getClassDecl(typeId);
		MemberFunc memberFunc = classDecl.getMemberFunc(funcMemberBinding);

		List<FuncParameter> parameterValues = loadFunctionParameters(memberFunc, paramExpr);
		return var.loadMemberFuncRef(memberFunc, parameterValues, _gvplGraph,
					_parentBasicBlock, startingLine);
	}

	List<FuncParameter> loadFunctionParameters(Function func, IASTExpression paramExpr) {
		List<FuncParameter> parameter_values = new ArrayList<FuncParameter>();
		if (paramExpr == null)
			return parameter_values;

		IASTExpression[] parameters;
		if (paramExpr instanceof IASTExpressionList) {
			IASTExpressionList expr_list = (IASTExpressionList) paramExpr;
			parameters = expr_list.getExpressions();
		} else {
			parameters = new IASTExpression[1];
			parameters[0] = paramExpr;
		}
		
		if(parameters.length != func.getNumParameters())
			GeneralOutputter.fatalError("Number of parameters are different!");

		for (int i = 0; i < parameters.length; i++) {
			IASTExpression parameter = parameters[i];
			FuncParameter localParameter = null;
			FuncParameter insideFuncParameter = func.getParameter(i);

			if (insideFuncParameter.getType() == IndirectionType.E_POINTER)
				localParameter = new FuncParameter(loadVarInAddress(parameter, _parentBasicBlock),
						IndirectionType.E_POINTER);
			else if (insideFuncParameter.getType() == IndirectionType.E_REFERENCE) {
				IVar var = _parentBasicBlock.getVarFromExpr(parameter);
				localParameter = new FuncParameter(var, IndirectionType.E_REFERENCE);
			} else if (insideFuncParameter.getType() == IndirectionType.E_VARIABLE)
				localParameter = new FuncParameter(loadValue(parameter), IndirectionType.E_VARIABLE);
			else
				GeneralOutputter.fatalError("Work here ");

			parameter_values.add(localParameter);
		}

		return parameter_values;
	}

	GraphNode loadBinOp(IASTBinaryExpression bin_op) {
		int startingLine = bin_op.getFileLocation().getStartingLineNumber();
		eBinOp op = CppMaps.getBinOpType(bin_op.getOperator());
		GraphNode lvalue = loadValue(bin_op.getOperand1());
		GraphNode rvalue = loadValue(bin_op.getOperand2());
		return _gvplGraph.addBinOp(op, lvalue, rvalue, _parentBasicBlock, startingLine);
	}

	GraphNode loadDirectValue(IASTLiteralExpression node) {
		String value = node.toString();
		return _gvplGraph.addDirectVal(CppMaps.eValueType.E_INVALID_TYPE, value, node
				.getFileLocation().getStartingLineNumber());
	}

	/**
	 * Returns the var that is pointed by the address For example, in "b = &d;"
	 * the function receives "&d" and returns the variable of "d"
	 * 
	 * @param address
	 *            Address that contains the variable
	 * @return The var that is pointed by the address
	 */
	public static IVar loadVarInAddress(IASTExpression address, AstLoader astLoader) {
		if (!(address instanceof IASTUnaryExpression)) {
			// it's receiving the address from another pointer, like
			// "int *b; int *a = b;"
			IVar DirectVarDecl = astLoader.getVarFromExpr(address);
			if (!(DirectVarDecl instanceof PointerVar))
				GeneralOutputter.fatalError("not expected here!!");
			return ((PointerVar) DirectVarDecl).getVarInMem();
		}

		// It's getting the address of a reference, like "int a = &b;"

		IASTUnaryExpression unaryExpr = (IASTUnaryExpression) address;
		// Check if the operator is a reference
		if (unaryExpr.getOperator() != IASTUnaryExpression.op_amper)
			GeneralOutputter.fatalError("not expected here!!");

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
		int startingLine = unExpr.getFileLocation().getStartingLineNumber();
		// Check if the operator is a star
		if (unExpr.getOperator() != CPPASTUnaryExpression.op_star)
			GeneralOutputter.fatalError("not implemented");

		IASTExpression opExpr = unExpr.getOperand();
		IVar pointerVar = _parentBasicBlock.getVarFromExpr(opExpr);
		if (!(pointerVar instanceof PointerVar))
			GeneralOutputter.fatalError("not expected here");

		return pointerVar.getCurrentNode(startingLine);
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
	public static IVar loadPointedVar(IASTExpression pointerExpr, AstLoader astLoader) {
		IVar pointerVar = astLoader.getVarFromExpr(pointerExpr);
		if (pointerVar instanceof PointerVar)
			return ((PointerVar) pointerVar).getVarInMem();
		else
			return loadVarInAddress(pointerExpr, astLoader);
	}

	public AstLoader getParentBasicBlock() {
		return _parentBasicBlock;
	}
	
	public AstInterpreter getAstInterpreter() {
		return _astInterpreter;
	}
	
	public Graph getGraph() {
		return _gvplGraph;
	}
}
