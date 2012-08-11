package gvpl.cdt;

import gvpl.cdt.CppMaps.eAssignBinOp;
import gvpl.cdt.CppMaps.eBinOp;
import gvpl.common.DirectVarDecl;
import gvpl.common.ErrorOutputter;
import gvpl.common.PointerVarDecl;
import gvpl.common.StructVarDecl;
import gvpl.common.VarDecl;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.TypeId;
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
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;

public class InstructionLine {

	private GraphBuilder _graphBuilder;
	private CppMaps _cppMaps;
	private AstInterpreter _astInterpreter;
	private AstLoader _parentBasicBlock;

	public InstructionLine(GraphBuilder graph_builder, AstLoader parent, CppMaps cppMaps,
			AstInterpreter astInterpreter) {
		_graphBuilder = graph_builder;
		_cppMaps = cppMaps;
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
				DirectVarDecl var_decl = _parentBasicBlock.loadVarDecl(declarator, type);
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
			BasicBlock basicBlockLoader = new BasicBlock(_parentBasicBlock, _astInterpreter, null);
			basicBlockLoader.load(statement);
		} else
			ErrorOutputter.fatalError("Node type not found!! Node: " + statement.toString());
	}

	public void LoadVariableInitialization(DirectVarDecl var_decl, IASTDeclarator decl) {
		IASTInitializerExpression init_exp = (IASTInitializerExpression) decl.getInitializer();

		if (init_exp == null)
			return;

		IASTExpression expr = init_exp.getExpression();
		
		if(decl.getPointerOperators().length > 0) {
			if(!(var_decl instanceof PointerVarDecl))
				ErrorOutputter.fatalError("not expected here");
			
			VarDecl pointedVar = loadPointedVar(expr, _parentBasicBlock);
			
			PointerVarDecl pointer = (PointerVarDecl) var_decl;
			pointer.setPointedVarDecl(pointedVar);
		}

		GraphNode val = loadValue(expr);
		_graphBuilder.addAssignOp(var_decl, val, _parentBasicBlock);
	}

	/*
	 * @brief Alguma coisa que retorna um valor
	 */
	public GraphNode loadValue(IASTExpression node) {

		// Eh uma variavel
		if (node instanceof IASTIdExpression) {
			VarDecl var_decl = _parentBasicBlock.getVarDeclOfReference(node);
			return var_decl.getCurrentNode();
		} else if (node instanceof IASTBinaryExpression) {// Eh uma expressao
			return loadBinOp((IASTBinaryExpression) node);
		} else if (node instanceof IASTLiteralExpression) {// Eh um valor direto
			return loadDirectValue((IASTLiteralExpression) node);
		} else if (node instanceof IASTFunctionCallExpression) {// Eh umachamada
																// a funcao
			return loadFunctionCall((IASTFunctionCallExpression) node);
		} else if (node instanceof IASTFieldReference) {// reference to field of
														// a struct
			VarDecl var_decl = _parentBasicBlock.getVarDeclOfFieldRef((IASTFieldReference) node);
			return var_decl.getCurrentNode();
		} else if (node instanceof IASTUnaryExpression) {
			VarDecl varDecl = loadUnaryExpr((IASTUnaryExpression) node);
			return varDecl.getCurrentNode();
		} else
			ErrorOutputter.fatalError("Node type not found!! Node: " + node.getClass());

		return null;
	}

	private void load_for_stmt(IASTForStatement node) {
		ForLoop forLoop = new ForLoop(_graphBuilder, _parentBasicBlock, _cppMaps, _astInterpreter);
		forLoop.load(node, _graphBuilder); 
	}

	private void loadReturnStatement(IASTReturnStatement statement) {
		IASTReturnStatement return_node = (IASTReturnStatement) statement;

		GraphNode rvalue = loadValue(return_node.getReturnValue());

		Function function = _parentBasicBlock.getFunction();

		// TODO set the correct type of the return value
		GraphNode returnNode = _parentBasicBlock.addReturnStatement(rvalue, null,
				function.getName());

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
		IASTExpression op1Expr = node.getOperand1();
		VarDecl varDecl = _parentBasicBlock.getVarDeclOfReference(op1Expr);
		if(varDecl instanceof PointerVarDecl) {
			PointerVarDecl pointer = (PointerVarDecl) varDecl;
			VarDecl rhsPointer = loadVarInAddress(node.getOperand2());
			pointer.setPointedVarDecl(rhsPointer);
			return null;
		}		
		
		GraphNode rvalue = loadValue(node.getOperand2());

		if (node.getOperator() == IASTBinaryExpression.op_assign) {
			_graphBuilder.addAssignOp(varDecl, rvalue, _parentBasicBlock);
			return null;
		}

		GraphNode lvalue = loadValue(node.getOperand1());
		eAssignBinOp op = _cppMaps.getAssignBinOpTypes(node.getOperator());
		return _graphBuilder.addAssignBinOp(op, varDecl, lvalue, rvalue, _parentBasicBlock);
	}

	GraphNode loadFunctionCall(IASTFunctionCallExpression func_call) {
		List<GraphNode> parameter_values = new ArrayList<GraphNode>();
		IASTExpression param_expr = func_call.getParameterExpression();
		if (param_expr instanceof IASTExpressionList) {
			IASTExpressionList expr_list = (IASTExpressionList) param_expr;
			IASTExpression[] parameters = expr_list.getExpressions();
			for (IASTExpression parameter : parameters)
				parameter_values.add(loadValue(parameter));
		} else {
			parameter_values.add(loadValue(param_expr));
		}

		IASTExpression name_expr = func_call.getFunctionNameExpression();
		if (name_expr instanceof IASTIdExpression) {
			IASTIdExpression expr = (IASTIdExpression) func_call.getFunctionNameExpression();
			Function loadFunction = _astInterpreter.getFuncId(expr.getName().resolveBinding());
			return loadFunction.addFuncRef(parameter_values, _graphBuilder);
		} else if (name_expr instanceof IASTFieldReference) {
			return loadMemberFuncRef(func_call, parameter_values);
		} else
			ErrorOutputter.fatalError("Not treated. " + name_expr.getClass());

		return null;
	}

	GraphNode loadBinOp(IASTBinaryExpression bin_op) {
		eBinOp op = _cppMaps.getBinOpType(bin_op.getOperator());
		GraphNode lvalue = loadValue(bin_op.getOperand1());
		GraphNode rvalue = loadValue(bin_op.getOperand2());
		return _graphBuilder.addBinOp(op, lvalue, rvalue, _parentBasicBlock);
	}

	GraphNode loadDirectValue(IASTLiteralExpression node) {
		String value = node.toString();
		return _graphBuilder.addDirectVal(CppMaps.eValueType.E_INVALID_TYPE, value);
	}

	public GraphNode loadMemberFuncRef(IASTFunctionCallExpression func_call,
			List<GraphNode> parameter_values) {
		IASTFieldReference field_ref = (IASTFieldReference) func_call.getFunctionNameExpression();

		IBinding func_member_binding = field_ref.getFieldName().resolveBinding();
		MemberFunc member_func = _astInterpreter.getMemberFunc(func_member_binding);

		IASTExpression expr = field_ref.getFieldOwner();
		VarDecl varDecl = _parentBasicBlock.getVarDeclOfReference(expr);
		if (!(varDecl instanceof StructVarDecl))
			ErrorOutputter.fatalError("Work here.");

		return member_func.loadMemberFuncRef((StructVarDecl) varDecl, parameter_values,
				_graphBuilder);
	}
	
	public void loadIfStatement(IASTIfStatement ifStatement) {
		IASTExpression condition = ifStatement.getConditionExpression();
		GraphNode conditionNode = loadValue(condition);
			
		IASTStatement thenClause = ifStatement.getThenClause();
		{
			BasicBlock basicBlockLoader = new BasicBlock(_parentBasicBlock, _astInterpreter, conditionNode);
			basicBlockLoader.load(thenClause);
		}
		
		IASTStatement elseClause = ifStatement.getElseClause();
		if (elseClause != null)
		{
			GraphNode notCondition = _graphBuilder.addNotOp(conditionNode, _parentBasicBlock);

			BasicBlock basicBlockLoader = new BasicBlock(_parentBasicBlock, _astInterpreter, notCondition);
			basicBlockLoader.load(elseClause);
		}
	}
	
	/**
	 * Returns the var that is pointed by the address
	 * For example, in "b = &d;" the function receives "&d" and returns the variable of "d"
	 * @param address Address that contains the variable
	 * @return The var that is pointed by the address
	 */
	public VarDecl loadVarInAddress(IASTExpression address)
	{
		if(!(address instanceof IASTUnaryExpression))
			ErrorOutputter.fatalError("not expected here!!");
		
		IASTUnaryExpression unaryExpr = (IASTUnaryExpression) address;
		//Check if the operator is a reference
		if(unaryExpr.getOperator() != IASTUnaryExpression.op_amper)
			ErrorOutputter.fatalError("not expected here!!");
			
		IASTExpression op = unaryExpr.getOperand();
		return _parentBasicBlock.getVarDeclOfReference(op);
	}
	
	VarDecl loadUnaryExpr(IASTUnaryExpression unExpr) {
		//Check if the operator is a star
		if(unExpr.getOperator() != CPPASTUnaryExpression.op_star)
			ErrorOutputter.fatalError("not implemented");
		
		IASTExpression opExpr = unExpr.getOperand();
		return loadPointedVar(opExpr, _parentBasicBlock);
	}
	
	/**
	 * Returns the variable that is currently pointed by the received pointer
	 * For example, in "b = &d; c = *b;" in the second line, the function
	 * will receive "b" as pointerExpr and will return the variable of "d"
	 * @param pointerExpr Expression of a pointer variable
	 * @param astLoader
	 * @return The variable that is currently pointed by the received pointer
	 */
	public static VarDecl loadPointedVar(IASTExpression pointerExpr, AstLoader astLoader) {
		VarDecl pointerVar = astLoader.getVarDeclOfReference(pointerExpr);
		if(!(pointerVar instanceof PointerVarDecl))
			ErrorOutputter.fatalError("not expected here");
			
		return ((PointerVarDecl)pointerVar).getPointedVarDecl();
	}
}
