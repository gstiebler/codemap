package gvpl.cdt;

import gvpl.cdt.CppMaps.eAssignBinOp;
import gvpl.cdt.CppMaps.eBinOp;
import gvpl.common.ClassVar;
import gvpl.common.ErrorOutputter;
import gvpl.common.FuncParameter;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.common.PointerVar;
import gvpl.common.TypeId;
import gvpl.common.Var;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNewExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;

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
		if (statement instanceof IASTDeclarationStatement) {// variable
															// declaration
			IASTDeclarationStatement decl_statement = (IASTDeclarationStatement) statement;
			IASTDeclaration decl = decl_statement.getDeclaration();
			if (!(decl instanceof IASTSimpleDeclaration))
				ErrorOutputter.fatalError("Deu merda aqui.");

			IASTSimpleDeclaration simple_decl = (IASTSimpleDeclaration) decl;
			IASTDeclSpecifier decl_spec = simple_decl.getDeclSpecifier();

			TypeId type = _astInterpreter.getType(decl_spec);

			IASTDeclarator[] declarators = simple_decl.getDeclarators();
			for (IASTDeclarator declarator : declarators) {
				// possibly more than one variable per line
				Var var_decl = _parentBasicBlock.loadVarDecl(declarator, type);
				LoadVariableInitialization(var_decl, declarator);
			}
		} else if (statement instanceof IASTExpression)
			loadValue((IASTExpression) statement);
		else if (statement instanceof IASTForStatement)
			load_for_stmt((IASTForStatement) statement);
		else if (statement instanceof IASTExpressionStatement) {
			IASTExpressionStatement expr_stat = (IASTExpressionStatement) statement;
			IASTExpression expr = expr_stat.getExpression();
			if (expr instanceof IASTBinaryExpression)
				loadAssignBinOp((IASTBinaryExpression) expr);
			else if (expr instanceof IASTFunctionCallExpression)
				loadFunctionCall((IASTFunctionCallExpression) expr);
		} else if (statement instanceof IASTReturnStatement) {
			loadReturnStatement((IASTReturnStatement) statement);
		} else if (statement instanceof IASTIfStatement) {
			loadIfStatement((IASTIfStatement) statement);
		} else if (statement instanceof IASTCompoundStatement) {
			BasicBlock basicBlockLoader = new BasicBlock(_parentBasicBlock, _astInterpreter);
			basicBlockLoader.load(statement);
		} else
			ErrorOutputter.fatalError("Node type not found!! Node: " + statement.toString());
	}

	/**
	 * Loads the value of the variable, like in "int a = b;"
	 * 
	 * @param lhsVar
	 *            The variable to be initialized
	 * @param decl
	 *            Code with the declaration
	 */
	public void LoadVariableInitialization(Var lhsVar, IASTDeclarator decl) {
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
		lhsVar.receiveAssign(NodeType.E_VARIABLE, rhsValue, _parentBasicBlock, startingLine);
	}
	
	void loadConstructorInitializer(Var lhsVar, IASTExpression initExpr, int startingLine) {
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
			Var var_decl = _parentBasicBlock.getVarOfReference(expr);
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
			Var var_decl = _parentBasicBlock.getVarDeclOfFieldRef((IASTFieldReference) expr);
			return var_decl.getCurrentNode(expr.getFileLocation().getStartingLineNumber());
		} else if (expr instanceof IASTUnaryExpression) {
			return loadUnaryExpr((IASTUnaryExpression) expr);
		} else
			ErrorOutputter.fatalError("Node type not found!! Node: " + expr.getClass());

		return null;
	}

	private void load_for_stmt(IASTForStatement node) {
		ForLoop forLoop = new ForLoop(_gvplGraph, _parentBasicBlock, _astInterpreter);
		forLoop.load(node, _gvplGraph);
	}

	private void loadReturnStatement(IASTReturnStatement statement) {
		IASTReturnStatement return_node = (IASTReturnStatement) statement;

		GraphNode rvalue = loadValue(return_node.getReturnValue());

		Function function = _parentBasicBlock.getFunction();

		// TODO set the correct type of the return value
		GraphNode returnNode = _parentBasicBlock.addReturnStatement(rvalue, null,
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
		Var lhsVar = _parentBasicBlock.getVarOfReference(lhsOp);
		IASTExpression rhsExpr = node.getOperand2();

		// check if we're trying to read a the instance of a pointer
		if (lhsOp instanceof IASTUnaryExpression) {

		} else if (lhsVar instanceof PointerVar) {
			loadRhsPointer((PointerVar) lhsVar, rhsExpr);
			return null;
		}

		GraphNode rhsValue = loadValue(rhsExpr);

		if (node.getOperator() == IASTBinaryExpression.op_assign) {
			lhsVar.receiveAssign(NodeType.E_VARIABLE, rhsValue, _parentBasicBlock, startingLine);
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
			ClassDecl classDecl = _astInterpreter.getClassDecl(lhsPointer.getType());
			if (classDecl == null) {
				lhsPointer.initializeGraphNode(NodeType.E_VARIABLE, _gvplGraph, _parentBasicBlock,
						_astInterpreter, startingLine);
				return;
			}

			Function constructorFunc = classDecl.getConstructorFunc();

			List<FuncParameter> parameterValues = null;
			IASTExpression expr = ((CPPASTNewExpression) rhsOp).getNewInitializer();
			if (expr != null) {
				parameterValues = loadFunctionParameters(constructorFunc, expr);
			}
			lhsPointer.constructor(parameterValues, NodeType.E_VARIABLE, _gvplGraph,
					_parentBasicBlock, _astInterpreter, startingLine);
			return;
		} else {
			Var rhsPointer = loadPointedVar(rhsOp, _parentBasicBlock);
			lhsPointer.setPointedVarDecl(rhsPointer);
			return;
		}
	}

	GraphNode loadFunctionCall(IASTFunctionCallExpression func_call) {
		int startingLine = func_call.getFileLocation().getStartingLineNumber();
		Function func = getFunction(func_call);
		IASTExpression paramExpr = func_call.getParameterExpression();

		List<FuncParameter> parameterValues = loadFunctionParameters(func, paramExpr);
		IASTExpression name_expr = func_call.getFunctionNameExpression();
		if (name_expr instanceof IASTIdExpression) {
			IASTIdExpression expr = (IASTIdExpression) name_expr;
			Function loadFunction = _astInterpreter.getFuncId(expr.getName().resolveBinding());
			return loadFunction.addFuncRef(parameterValues, _gvplGraph, startingLine);
		} else if (name_expr instanceof IASTFieldReference) {
			return loadMemberFuncRef(func_call, parameterValues);
		} else
			ErrorOutputter.fatalError("Not treated. " + name_expr.getClass());

		return null;
	}

	List<FuncParameter> loadFunctionParameters(Function func, IASTExpression paramExpr) {
		List<FuncParameter> parameter_values = new ArrayList<FuncParameter>();
		if (paramExpr == null)
			return parameter_values;

		IASTExpression[] parameters;
		if (paramExpr instanceof IASTExpressionList) {
			IASTExpressionList expr_list = (IASTExpressionList) paramExpr;
			parameters = expr_list.getExpressions();
		} else if (true) {
			parameters = new IASTExpression[1];
			parameters[0] = paramExpr;
		}
		
		if(parameters.length != func._parameters.size())
			ErrorOutputter.fatalError("Number of parameters are different!");

		for (int i = 0; i < parameters.length; i++) {
			IASTExpression parameter = parameters[i];
			FuncParameter localParameter = null;
			FuncParameter insideFuncParameter = func._parameters.get(i);

			if (insideFuncParameter.getType() == IndirectionType.E_POINTER)
				localParameter = new FuncParameter(loadVarInAddress(parameter, _parentBasicBlock),
						IndirectionType.E_POINTER);
			else if (insideFuncParameter.getType() == IndirectionType.E_REFERENCE) {
				Var var = _parentBasicBlock.getVarOfReference(parameter);
				localParameter = new FuncParameter(var, IndirectionType.E_REFERENCE);
			} else if (insideFuncParameter.getType() == IndirectionType.E_VARIABLE)
				localParameter = new FuncParameter(loadValue(parameter), IndirectionType.E_VARIABLE);
			else
				ErrorOutputter.fatalError("Work here ");

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

	public GraphNode loadMemberFuncRef(IASTFunctionCallExpression func_call,
			List<FuncParameter> parameter_values) {
		IASTFieldReference field_ref = (IASTFieldReference) func_call.getFunctionNameExpression();

		IBinding func_member_binding = field_ref.getFieldName().resolveBinding();
		MemberFunc member_func = _astInterpreter.getMemberFunc(func_member_binding);

		IASTExpression expr = field_ref.getFieldOwner();
		Var var = _parentBasicBlock.getVarOfReference(expr);
		if (!(var instanceof ClassVar))
			ErrorOutputter.fatalError("Work here.");

		return member_func.loadMemberFuncRef((ClassVar) var, parameter_values, _gvplGraph,
				_parentBasicBlock, func_call.getFileLocation().getStartingLineNumber());
	}

	public void loadIfStatement(IASTIfStatement ifStatement) {
		IASTExpression condition = ifStatement.getConditionExpression();
		GraphNode conditionNode = loadValue(condition);

		{
			IASTStatement thenClause = ifStatement.getThenClause();
			int startingLine = thenClause.getFileLocation().getStartingLineNumber();
			BasicBlock basicBlockLoader = new BasicBlock(_parentBasicBlock, _astInterpreter);
			basicBlockLoader.load(thenClause);
			basicBlockLoader.addIf(conditionNode, startingLine);
		}

		IASTStatement elseClause = ifStatement.getElseClause();
		if (elseClause != null) {
			int startingLine = elseClause.getFileLocation().getStartingLineNumber();
			GraphNode notCondition = _gvplGraph.addNotOp(conditionNode, _parentBasicBlock,
					ifStatement.getFileLocation().getStartingLineNumber());

			BasicBlock basicBlockLoader = new BasicBlock(_parentBasicBlock, _astInterpreter);
			basicBlockLoader.load(elseClause);
			basicBlockLoader.addIf(notCondition, startingLine);
		}
	}

	/**
	 * Returns the var that is pointed by the address For example, in "b = &d;"
	 * the function receives "&d" and returns the variable of "d"
	 * 
	 * @param address
	 *            Address that contains the variable
	 * @return The var that is pointed by the address
	 */
	public static Var loadVarInAddress(IASTExpression address, AstLoader astLoader) {
		if (!(address instanceof IASTUnaryExpression)) {
			// it's receiving the address from another pointer, like
			// "int *b; int *a = b;"
			Var DirectVarDecl = astLoader.getVarOfReference(address);
			if (!(DirectVarDecl instanceof PointerVar))
				ErrorOutputter.fatalError("not expected here!!");
			return ((PointerVar) DirectVarDecl).getVarInMem();
		}

		// It's getting the address of a reference, like "int a = &b;"

		IASTUnaryExpression unaryExpr = (IASTUnaryExpression) address;
		// Check if the operator is a reference
		if (unaryExpr.getOperator() != IASTUnaryExpression.op_amper)
			ErrorOutputter.fatalError("not expected here!!");

		IASTExpression op = unaryExpr.getOperand();
		return astLoader.getVarOfReference(op);
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
			ErrorOutputter.fatalError("not implemented");

		IASTExpression opExpr = unExpr.getOperand();
		Var pointerVar = _parentBasicBlock.getVarOfReference(opExpr);
		if (!(pointerVar instanceof PointerVar))
			ErrorOutputter.fatalError("not expected here");

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
	public static Var loadPointedVar(IASTExpression pointerExpr, AstLoader astLoader) {
		Var pointerVar = astLoader.getVarOfReference(pointerExpr);
		if (pointerVar instanceof PointerVar)
			return ((PointerVar) pointerVar).getVarInMem();
		else
			return loadVarInAddress(pointerExpr, astLoader);
	}

	private Function getFunction(IASTFunctionCallExpression func_call) {
		IASTExpression name_expr = func_call.getFunctionNameExpression();
		if (name_expr instanceof IASTIdExpression) {
			IASTIdExpression expr = (IASTIdExpression) name_expr;
			return _astInterpreter.getFuncId(expr.getName().resolveBinding());
		} else if (name_expr instanceof IASTFieldReference) {
			IASTFieldReference field_ref = (IASTFieldReference) func_call
					.getFunctionNameExpression();

			IBinding func_member_binding = field_ref.getFieldName().resolveBinding();
			return _astInterpreter.getMemberFunc(func_member_binding);
		} else
			ErrorOutputter.fatalError("problem");

		return null;
	}
}
