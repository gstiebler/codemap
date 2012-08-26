package gvpl.cdt;

import gvpl.cdt.CppMaps.eAssignBinOp;
import gvpl.cdt.CppMaps.eBinOp;
import gvpl.common.DirectVarDecl;
import gvpl.common.ErrorOutputter;
import gvpl.common.FuncParameter;
import gvpl.common.FuncParameter.eParameterType;
import gvpl.common.PointerVarDecl;
import gvpl.common.StructVarDecl;
import gvpl.common.VarDecl;
import gvpl.graph.Graph.NodeType;
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
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNewExpression;
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
		int startingLine = decl.getFileLocation().getStartingLineNumber();
		
		IASTInitializerExpression init_exp = (IASTInitializerExpression) decl.getInitializer();

		if (init_exp == null)
			return;

		IASTExpression expr = init_exp.getExpression();
		
		if(decl.getPointerOperators().length > 0) {
			if(!(var_decl instanceof PointerVarDecl))
				ErrorOutputter.fatalError("not expected here");

			PointerVarDecl pointer = (PointerVarDecl) var_decl;
			
			if(expr instanceof CPPASTNewExpression) {
				pointer.initializeGraphNode(NodeType.E_VARIABLE, startingLine);
				return;
			}

			VarDecl pointedVar = loadPointedVar(expr, _parentBasicBlock);			
			pointer.setPointedVarDecl(pointedVar);
			return;
		}

		GraphNode val = loadValue(expr);
		_graphBuilder.addAssignOp(var_decl, val, _parentBasicBlock, startingLine);
	}

	/*
	 * @brief Alguma coisa que retorna um valor
	 */
	public GraphNode loadValue(IASTExpression node) {

		// Eh uma variavel
		if (node instanceof IASTIdExpression) {
			VarDecl var_decl = _parentBasicBlock.getVarDeclOfReference(node);
			return var_decl.getCurrentNode(node.getFileLocation().getStartingLineNumber());
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
			return var_decl.getCurrentNode(node.getFileLocation().getStartingLineNumber());
		} else if (node instanceof IASTUnaryExpression) {
			return loadUnaryExpr((IASTUnaryExpression) node);
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
		VarDecl lhsVarDecl = _parentBasicBlock.getVarDeclOfReference(lhsOp);
		//check if we're trying to read a the instance of a pointer
		if(lhsOp instanceof IASTUnaryExpression){
			//rvalue = varDecl.getCurrentNode();
		} else {
			//check if the operation is a assignment of a address to a pointer
			if(lhsVarDecl instanceof PointerVarDecl) {
				PointerVarDecl pointer = (PointerVarDecl) lhsVarDecl;
				
				IASTExpression rhsOp = node.getOperand2();
				if(rhsOp instanceof CPPASTNewExpression){
					pointer.initializeGraphNode(NodeType.E_VARIABLE, startingLine);
					return null;
				} else {
					VarDecl rhsPointer = loadVarInAddress(rhsOp, _parentBasicBlock);
					pointer.setPointedVarDecl(rhsPointer);
					return null;
				}
			}	
		}
		
		GraphNode rvalue = loadValue(node.getOperand2());

		if (node.getOperator() == IASTBinaryExpression.op_assign) {
			lhsVarDecl.receiveAssign(NodeType.E_VARIABLE, rvalue, _parentBasicBlock, startingLine);
			return null;
		}

		GraphNode lvalue = loadValue(node.getOperand1());
		eAssignBinOp op = _cppMaps.getAssignBinOpTypes(node.getOperator());
		return _graphBuilder.addAssignBinOp(op, lhsVarDecl, lvalue, rvalue, _parentBasicBlock, startingLine);
	}

	GraphNode loadFunctionCall(IASTFunctionCallExpression func_call) {
		int startingLine = func_call.getFileLocation().getStartingLineNumber();
		Function func = getFunction(func_call);
		
		List<FuncParameter> parameter_values = new ArrayList<FuncParameter>();
		IASTExpression param_expr = func_call.getParameterExpression();
		if (param_expr instanceof IASTExpressionList) {
			IASTExpressionList expr_list = (IASTExpressionList) param_expr;
			IASTExpression[] parameters = expr_list.getExpressions();
			for (int i = 0; i < parameters.length; i++) {
				IASTExpression parameter = parameters[i];
				FuncParameter localParameter = null;
				FuncParameter insideFuncParameter = func._parameters.get(i);
				
				if(insideFuncParameter.getType() == eParameterType.E_POINTER) 
					localParameter = new FuncParameter(loadVarInAddress(parameter, _parentBasicBlock), eParameterType.E_POINTER);
				else if(insideFuncParameter.getType() == eParameterType.E_REFERENCE) {
					VarDecl varDecl = _parentBasicBlock.getVarDeclOfReference(parameter);
					localParameter = new FuncParameter(varDecl, eParameterType.E_REFERENCE);
				}
				else if (insideFuncParameter.getType() == eParameterType.E_VARIABLE)
					localParameter = new FuncParameter(loadValue(parameter), eParameterType.E_VARIABLE);
				else
					ErrorOutputter.fatalError("Work here ");
				
				parameter_values.add(localParameter);
			}
		} else {
			parameter_values.add(new FuncParameter(loadValue(param_expr), eParameterType.E_VARIABLE));
		}

		IASTExpression name_expr = func_call.getFunctionNameExpression();
		if (name_expr instanceof IASTIdExpression) {
			IASTIdExpression expr = (IASTIdExpression) name_expr;
			Function loadFunction = _astInterpreter.getFuncId(expr.getName().resolveBinding());
			return loadFunction.addFuncRef(parameter_values, _graphBuilder, startingLine);
		} else if (name_expr instanceof IASTFieldReference) {
			return loadMemberFuncRef(func_call, parameter_values);
		} else
			ErrorOutputter.fatalError("Not treated. " + name_expr.getClass());

		return null;
	}

	GraphNode loadBinOp(IASTBinaryExpression bin_op) {
		int startingLine = bin_op.getFileLocation().getStartingLineNumber();
		eBinOp op = _cppMaps.getBinOpType(bin_op.getOperator());
		GraphNode lvalue = loadValue(bin_op.getOperand1());
		GraphNode rvalue = loadValue(bin_op.getOperand2());
		return _graphBuilder.addBinOp(op, lvalue, rvalue, _parentBasicBlock, startingLine);
	}

	GraphNode loadDirectValue(IASTLiteralExpression node) {
		String value = node.toString();
		return _graphBuilder.addDirectVal(CppMaps.eValueType.E_INVALID_TYPE, value, node.getFileLocation().getStartingLineNumber());
	}

	public GraphNode loadMemberFuncRef(IASTFunctionCallExpression func_call,
			List<FuncParameter> parameter_values) {
		IASTFieldReference field_ref = (IASTFieldReference) func_call.getFunctionNameExpression();

		IBinding func_member_binding = field_ref.getFieldName().resolveBinding();
		MemberFunc member_func = _astInterpreter.getMemberFunc(func_member_binding);

		IASTExpression expr = field_ref.getFieldOwner();
		VarDecl varDecl = _parentBasicBlock.getVarDeclOfReference(expr);
		if (!(varDecl instanceof StructVarDecl))
			ErrorOutputter.fatalError("Work here.");

		return member_func.loadMemberFuncRef((StructVarDecl) varDecl, parameter_values,
				_graphBuilder, func_call.getFileLocation().getStartingLineNumber());
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
			GraphNode notCondition = _graphBuilder.addNotOp(conditionNode, _parentBasicBlock, ifStatement.getFileLocation().getStartingLineNumber());

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
	public static VarDecl loadVarInAddress(IASTExpression address, AstLoader astLoader)
	{
		if(!(address instanceof IASTUnaryExpression)) {
			//it's receiving the address from another pointer, like "int *b; int *a = b;" 
			VarDecl varDecl = astLoader.getVarDeclOfReference(address);
			if(!(varDecl instanceof PointerVarDecl))
				ErrorOutputter.fatalError("not expected here!!");
			return ((PointerVarDecl)varDecl).getPointedVarDecl();
		}
		
		//It's getting the address of a reference, like "int a = &b;"
		
		IASTUnaryExpression unaryExpr = (IASTUnaryExpression) address;
		//Check if the operator is a reference
		if(unaryExpr.getOperator() != IASTUnaryExpression.op_amper)
			ErrorOutputter.fatalError("not expected here!!");
			
		IASTExpression op = unaryExpr.getOperand();
		return astLoader.getVarDeclOfReference(op);
	}
	
	/**
	 * Currently used to read the value of "*b"
	 * @param unExpr
	 * @return
	 */
	GraphNode loadUnaryExpr(IASTUnaryExpression unExpr) {
		int startingLine = unExpr.getFileLocation().getStartingLineNumber();
		//Check if the operator is a star
		if(unExpr.getOperator() != CPPASTUnaryExpression.op_star)
			ErrorOutputter.fatalError("not implemented");
		
		IASTExpression opExpr = unExpr.getOperand();
		VarDecl pointerVar = _parentBasicBlock.getVarDeclOfReference(opExpr);
		if(!(pointerVar instanceof PointerVarDecl))
			ErrorOutputter.fatalError("not expected here");
		
		return pointerVar.getCurrentNode(startingLine);
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
		if(pointerVar instanceof PointerVarDecl)
			return ((PointerVarDecl)pointerVar).getPointedVarDecl();
		else
			return loadVarInAddress(pointerExpr, astLoader);
	}
	
	private Function getFunction(IASTFunctionCallExpression func_call) {
		IASTExpression name_expr = func_call.getFunctionNameExpression();
		if (name_expr instanceof IASTIdExpression) {
			IASTIdExpression expr = (IASTIdExpression) name_expr;
			return _astInterpreter.getFuncId(expr.getName().resolveBinding());
		} else if (name_expr instanceof IASTFieldReference) {
			IASTFieldReference field_ref = (IASTFieldReference) func_call.getFunctionNameExpression();

			IBinding func_member_binding = field_ref.getFieldName().resolveBinding();
			return _astInterpreter.getMemberFunc(func_member_binding);
		} else 
			ErrorOutputter.fatalError("problem");
		
		return null;
	}
}
