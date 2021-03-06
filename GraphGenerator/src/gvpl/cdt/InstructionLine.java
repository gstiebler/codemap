package gvpl.cdt;

import gvpl.cdt.function.Function;
import gvpl.cdt.function.MemberFunc;
import gvpl.clang.ClangSizeof;
import gvpl.common.AstInterpreter;
import gvpl.common.BaseScope;
import gvpl.common.ClassDecl;
import gvpl.common.ClassVar;
import gvpl.common.CodeLocation;
import gvpl.common.FuncParameter;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.common.IVar;
import gvpl.common.MemAddressVar;
import gvpl.common.PointerVar;
import gvpl.common.TypeId;
import gvpl.common.Value;
import gvpl.common.ifclasses.IfCondition;
import gvpl.exceptions.ClassNotImplementedException;
import gvpl.exceptions.NotFoundException;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
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
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;

import debug.DebugOptions;
import debug.ExecTreeLogger;

public class InstructionLine {
	
	static Logger logger = LogManager.getLogger(InstructionLine.class.getName());

	private Graph _gvplGraph;
	private AstInterpreterCDT _astInterpreter;
	private BaseScopeCDT _parentBaseScope;

	public InstructionLine(Graph gvplGraph, BaseScopeCDT parent, AstInterpreterCDT astInterpreter) {
		_gvplGraph = gvplGraph;
		_astInterpreter = astInterpreter;
		_parentBaseScope = parent;
	}

	public void load(IASTStatement statement) {
		CodeLocation previousCodeLocation = DebugOptions.getCurrCodeLocation();
		
		CodeLocation codeLocation = CodeLocationCDT.NewFromFileLocation(statement);
		DebugOptions.setCurrCodeLocation(codeLocation);
		logger.debug(" --- Code location: {}", codeLocation);
		DebugOptions.setStartingLine(codeLocation.getStartingLine());
		logger.debug("statement is: {}", statement.getClass());
		ExecTreeLogger.log("Linha: " + codeLocation.getStartingLine());
		
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
				IVar varDecl = _parentBaseScope.loadVarDecl(declarator, type, _gvplGraph);
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
			else if (expr instanceof ICPPASTUnaryExpression)
				logger.error("Not implemented: {}", expr.getClass());
			else
				loadDeleteOp((ICPPASTDeleteExpression) expr);
		} else if (statement instanceof IASTReturnStatement) {
			loadReturnStatement((IASTReturnStatement) statement);
		} else if (statement instanceof IASTIfStatement) {
			IfConditionCDT.loadIfCondition((IASTIfStatement) statement, this);
		} else if (statement instanceof IASTCompoundStatement) {
			BasicBlockCDT basicBlockLoader = new BasicBlockCDT(_parentBaseScope, _astInterpreter, _gvplGraph);
			basicBlockLoader.load(statement);
		} else if (statement instanceof ICPPASTSwitchStatement) {
			Switch.loadSwitch((ICPPASTSwitchStatement) statement, this);
		} else if (statement instanceof IASTWhileStatement) {
			logger.error("Node type not found!! Node: " + statement.toString());
		} else
			logger.error("Node type not found!! Node: " + statement.toString());

		DebugOptions.setCurrCodeLocation(previousCodeLocation);
	}
	
	void loadDeleteOp(ICPPASTDeleteExpression deleteExpr) {
		IASTExpression opExpr = deleteExpr.getOperand();
		IVar var;
		try {
			var = _parentBaseScope.getVarFromExpr(opExpr);
		} catch (NotFoundException e) {
			logger.error("Var not found on delete {}", e.getItemName());
			return;
		}
		MemAddressVar mav = (MemAddressVar) var;
		var = mav.getVarInMem();
		if(!(var instanceof ClassVar)) {
			logger.error("Var {} must be ClassVar", var);
			return;
		}
		
		((ClassVar) var).callDestructor(_parentBaseScope, _gvplGraph);
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
		logger.debug("Variable initialization: {}", lhsVar);
		IASTInitializer initializer = decl.getInitializer();	
		if(initializer == null) //variable is not initialized
		{
			if(!(lhsVar instanceof ClassVar))
				return;
			
			ClassVar classVar = (ClassVar) lhsVar;
			classVar.callConstructor(new ArrayList<FuncParameter>(), NodeType.E_VARIABLE, _gvplGraph, 
						_parentBaseScope, _astInterpreter);
			return;
		}
		
		if (initializer instanceof ICPPASTConstructorInitializer) { 
			// format: int a(5);
			ICPPASTConstructorInitializer constrInit = (ICPPASTConstructorInitializer) initializer;
			IASTExpression initExpr = constrInit.getExpression();
			loadConstructorInitializer(lhsVar, initExpr);														
			return;
		}
		
		IASTInitializerExpression initExp = (IASTInitializerExpression) initializer;
		IASTExpression rhsExpr = initExp.getExpression();

		if (lhsVar instanceof PointerVar) {
			loadRhsPointer((PointerVar) lhsVar, rhsExpr);
			return;
		}

		Value rhsValue = loadValue(rhsExpr);		
		if(rhsValue == null) {
			logger.error("Shoudn't happen");
			GraphNode problemNode = _gvplGraph.addGraphNode("PROBLEM", NodeType.E_INVALID_NODE_TYPE);
			lhsVar.receiveAssign(NodeType.E_INVALID_NODE_TYPE, new Value(problemNode), _gvplGraph);
			return;
		}

		lhsVar.receiveAssign(NodeType.E_VARIABLE, rhsValue, _gvplGraph);
	}
	
	public void loadConstructorInitializer(IVar lhsVar, IASTNode initExpr) {
		List<FuncParameter> parameterValues = null;
		if(lhsVar instanceof ClassVar) {
			IASTNode[] parameterNodes = getChildExpressions(initExpr);
			parameterValues = loadFunctionParameters(parameterNodes);
		} else {
			parameterValues = new ArrayList<FuncParameter>();
			Value value = loadValue(initExpr);
			FuncParameter funcParameter = new FuncParameter(value, IndirectionType.E_INDIFERENT);
			parameterValues.add(funcParameter);
		}
		lhsVar.callConstructor(parameterValues, NodeType.E_VARIABLE, _gvplGraph,
				_parentBaseScope, _astInterpreter);
	}

	/*
	 * @brief Alguma coisa que retorna um valor
	 */
	public Value loadValue(IASTNode node) {
		logger.debug("Load Value, Node type {}", node.getClass());
		ExecTreeLogger.log(node.getRawSignature());
		// Eh uma variavel

		try {
			if (node instanceof IASTIdExpression) {
				if (_parentBaseScope != null)
					return _parentBaseScope.getValueFromExpr((IASTIdExpression)node);
				else {
					IASTName name = ((IASTIdExpression) node).getName();
					IBinding binding = name.resolveBinding();
					if( binding instanceof IProblemBinding )
						return new Value(_gvplGraph.addGraphNode("PROBLEM_BINDING_" + binding, NodeType.E_INVALID_NODE_TYPE));
					CodeLocation codeLocation = CodeLocationCDT.NewFromFileLocation(name);
					return new Value(_astInterpreter.getGlobalVar(binding, codeLocation));
				}
			} else if (node instanceof IASTBinaryExpression) {// Eh uma
																// expressao
				return new Value(loadBinOp((IASTBinaryExpression) node));
			} else if (node instanceof IASTLiteralExpression) {// Eh um valor
																// direto
				return new Value(loadDirectValue((IASTLiteralExpression) node));
			} else if (node instanceof IASTFunctionCallExpression) {// Eh
																	// umachamada
																	// a funcao
				return loadFunctionCall((IASTFunctionCallExpression) node);
			} else if (node instanceof IASTFieldReference) {// reference to
															// field of
															// a struct
				IVar varDecl = _parentBaseScope.getVarFromFieldRef((IASTFieldReference) node);
				if (varDecl == null)
					return new Value(GraphNode.newGarbageNode(_gvplGraph, "INVALID_READ"));
				else
					return new Value(varDecl);
			} else if (node instanceof IASTUnaryExpression) {
				return new Value(loadUnaryExpr((IASTUnaryExpression) node));
			} else if (node instanceof IASTArraySubscriptExpression) {
				// It's an array
				IASTArraySubscriptExpression arraySubscrExpr = (IASTArraySubscriptExpression) node;
				IASTExpression arrayExpr = arraySubscrExpr.getArrayExpression();
				IVar varDecl = _parentBaseScope.getVarFromExpr(arrayExpr);
				IASTExpression index = arraySubscrExpr.getSubscriptExpression();
				Value indexValue = loadValue(index);
				GraphNode arrayResult = ArrayCDT.readFromArray(varDecl, indexValue, _gvplGraph);
				return new Value(arrayResult);
			} else if (node instanceof ICPPASTNewExpression) {
				throw new ClassNotImplementedException(node.getClass().toString(), node.getRawSignature());
			} else if (node instanceof ICPPASTSimpleTypeConstructorExpression) {
				ICPPASTSimpleTypeConstructorExpression stce = (ICPPASTSimpleTypeConstructorExpression) node;
				return loadValue(stce.getInitialValue());
			} else if (node instanceof ICPPASTTypeIdExpression) {
				ICPPASTTypeIdExpression tie = (ICPPASTTypeIdExpression) node;
				String raw = tie.getRawSignature();
				if(raw.length() >= 8 && raw.substring(0, 7).compareTo("sizeof(") == 0) {
					return new Value(_gvplGraph.addGraphNode(raw, NodeType.E_DIRECT_VALUE));
				} else {
					logger.warn("Not implemented CPPASTTypeIdExpression, {}", raw);
					return new Value(_gvplGraph.addGraphNode("PROBLEM_NODE_CPPASTTypeIdExpression",
							NodeType.E_INVALID_NODE_TYPE));
				}
			} else if (node instanceof IASTConditionalExpression) {
				IASTConditionalExpression ce = (IASTConditionalExpression) node;
				IASTExpression positiveExpr = ce.getPositiveResultExpression();
				IASTExpression negativeExpr = ce.getNegativeResultExpression();
				IASTExpression conditionExpr = ce.getLogicalConditionExpression();
				
				Value positiveValue = loadValue(positiveExpr);
				Value negativeValue = loadValue(negativeExpr);
				Value conditionValue = loadValue(conditionExpr);
				
				GraphNode ifNode = IfCondition.createIfNode(_gvplGraph, conditionValue.getNode(), 
						positiveValue.getNode(), negativeValue.getNode());

				return new Value(ifNode);
			} else if (node instanceof ICPPASTCastExpression) {
				return loadValue(((ICPPASTCastExpression) node).getOperand());
			} else if (node instanceof IASTExpressionList) {
				IASTExpressionList exprList = (IASTExpressionList) node;
				throw new ClassNotImplementedException(node.getClass().toString(), exprList.getRawSignature());
			} else if (node instanceof ClangSizeof) {
				GraphNode graphNode = _gvplGraph.addGraphNode(node.toString(), NodeType.E_DIRECT_VALUE);
				return new Value(graphNode);
			} else
				throw new ClassNotImplementedException(node.getClass().toString(), node.getRawSignature());
		} catch (ClassNotImplementedException e) {
			String nodeName = "INVALID_CLASS_" + e.getClassName();
			GraphNode problemGraphNode = _gvplGraph.addGraphNode(nodeName, NodeType.E_INVALID_NODE_TYPE);
			Value problemValue = new Value(problemGraphNode);
			return problemValue;
		} catch (NotFoundException e) {
			String nodeName = "INVALID_NODE_PROBLEM_" + e.getItemName() + "_" + e.getClass();
			return new Value(_gvplGraph.addGraphNode(nodeName, NodeType.E_INVALID_NODE_TYPE));
		} catch(Exception e) {
			logger.fatal("Critical error. Code location: {}, Stack trace: {}, Msg: {}", 
					DebugOptions.getCurrCodeLocation(), e.getStackTrace(), e.getMessage());
			String nodeName = "FATAL_ERROR_NODE_PROBLEM" + e.getClass();
			return new Value(_gvplGraph.addGraphNode(nodeName, NodeType.E_INVALID_NODE_TYPE));
		}
	}

	private void loadForStmt(IASTForStatement node) {
		ForLoop forLoop = new ForLoop(_parentBaseScope, _astInterpreter);
		forLoop.load(node, _gvplGraph, _parentBaseScope);
	}

	private void loadReturnStatement(IASTReturnStatement statement) {
		IASTReturnStatement returnStat = (IASTReturnStatement) statement;

		IASTExpression returnExpr = returnStat.getReturnValue();
		if( returnExpr == null )
			return;
			
		Value rvalue = loadValue(returnExpr);

		Function function = _parentBaseScope.getFunction();
		if(function == null) {
			logger.error("you're doing it wrong");
			return;
		}
		// TODO use the return type
		//TypeId returnType = function.getReturnTypeId();

		// TODO set the correct type of the return value
		//Value returnValue = function.addReturnStatement(rvalue, returnType,
		//		function.getName(), _gvplGraph);

		//If the function is returning a pointer, it should return the pointed var
		IVar var = rvalue.getVar();
		if(var instanceof PointerVar) {
			PointerVar pv = (PointerVar) var;
			rvalue.setVar(pv.getVarInMem());
		}	
		
		function.setReturnValue(rvalue);
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
		IVar lhsVar;
		try {
			lhsVar = _parentBaseScope.getVarFromExpr(lhsOp);
		} catch (NotFoundException e) {
			return _gvplGraph.addGraphNode("INVALID_NODE_PROBLEM " + e.getItemName(), NodeType.E_INVALID_NODE_TYPE);
		}
		
		if(lhsVar instanceof ClassVar) {
			IASTExpression op2 = binExpr.getOperand2();
			if( op2 instanceof IASTLiteralExpression) {
				//TODO op2 is probably NULL or zero
				logger.error("work here");
			} else
				return loadOperatorOverload(binExpr);
		}
		
		IASTExpression rhsExpr = binExpr.getOperand2();

		logger.debug("lhsOp class: {}", lhsOp.getClass());
		// check if we're trying to read a the instance of a pointer
		if (lhsOp instanceof IASTUnaryExpression) {
			IASTUnaryExpression unOp = (IASTUnaryExpression) lhsOp;
			logger.debug("Operator: {} in var {}", unOp.getOperator(), lhsVar.getName());
		} else if (lhsOp instanceof IASTArraySubscriptExpression) {
			Value rhsValue = loadValue(rhsExpr);
			IASTArraySubscriptExpression indexExpr = (IASTArraySubscriptExpression) lhsOp;
			IASTExpression subscriptExpr = indexExpr.getSubscriptExpression();
			Value indexValue = loadValue(subscriptExpr);
			return ArrayCDT.writeToArray( lhsVar, rhsValue, indexValue, _gvplGraph );
		} else if (lhsVar instanceof PointerVar) {
			loadRhsPointer((PointerVar) lhsVar, rhsExpr);
			return null;
		}

		logger.debug("loading rhs value");
		Value rhsValue = loadValue(rhsExpr);
		if(rhsValue == null) {
			logger.error("Shoudn't happen");
			return _gvplGraph.addGraphNode("PROBLEM", NodeType.E_INVALID_NODE_TYPE);
		}

		if (binExpr.getOperator() == IASTBinaryExpression.op_assign) {
			lhsVar.receiveAssign(NodeType.E_VARIABLE, rhsValue, _parentBaseScope.getGraph());
			return null;
		}

		Value lhsValue = loadValue(binExpr.getOperand1());
		String opStr = CppMaps.getAssignBinOpString(binExpr.getOperator());
		return _gvplGraph.addAssignBinOp(opStr, lhsVar, lhsValue.getNode(), rhsValue.getNode(), _parentBaseScope);
	}
	
	private GraphNode loadOperatorOverload(IASTBinaryExpression binExpr) {
		IASTExpression lhsOp = binExpr.getOperand1();
		ClassVar lhsVar;
		try {
			lhsVar = (ClassVar) _parentBaseScope.getVarFromExpr(lhsOp);
		} catch (NotFoundException e) {
			return _gvplGraph.addGraphNode("PROBLEM_NODE_" + e.getItemName(), NodeType.E_INVALID_NODE_TYPE);
		}
		
		IASTExpression rhsOp = binExpr.getOperand2();
		Value rhsValue = _parentBaseScope.getValueFromExpr(rhsOp);
		
		int operator = binExpr.getOperator();
		ClassDecl classDecl = lhsVar.getClassDecl();
		MemberFunc opFunc = classDecl.getOpFunc(operator);
		List<FuncParameter> parameterValues = new ArrayList<FuncParameter>();
		parameterValues.add(new FuncParameter(rhsValue, IndirectionType.E_REFERENCE));
		if(opFunc == null) {
			logger.error("opFunc must not be null: {}", lhsOp.getRawSignature());
			return _gvplGraph.addGraphNode("PROBLEM_NODE_" + lhsOp.getRawSignature(), NodeType.E_INVALID_NODE_TYPE);
		}
		Value result = opFunc.addFuncRef(parameterValues, _gvplGraph, lhsVar, _parentBaseScope);
		return result.getNode();
	}

	void loadRhsPointer(PointerVar lhsPointer, IASTNode rhsOp) {
		logger.debug("loading pointer of type: {}", rhsOp.getClass());
		if (rhsOp instanceof ICPPASTNewExpression) {
			ICPPASTNewExpression newExpr = (ICPPASTNewExpression) rhsOp;
			IASTDeclSpecifier namedSpec = newExpr.getTypeId().getDeclSpecifier();
			
			ClassDeclCDT classDecl = null;
			if(namedSpec instanceof IASTNamedTypeSpecifier) {
				IASTNamedTypeSpecifier typeSpec = (IASTNamedTypeSpecifier) namedSpec;
				IBinding funcBinding = typeSpec.getName().resolveBinding();
				classDecl = _astInterpreter.getClassFromFuncBinding(funcBinding);
			}
			
			if (classDecl == null) {
				lhsPointer.initializeVar(NodeType.E_VARIABLE, _gvplGraph, _astInterpreter);
				return;
			}

			List<FuncParameter> parameterValues = null;
			IASTExpression expr = ((ICPPASTNewExpression) rhsOp).getNewInitializer();
			IASTNode[] parameterNodes = getChildExpressions(expr);
			parameterValues = loadFunctionParameters(parameterNodes);
			lhsPointer.constructor(parameterValues, NodeType.E_VARIABLE, _gvplGraph,
					_parentBaseScope, _astInterpreter, classDecl.getTypeId());
		} else if (rhsOp instanceof IASTFunctionCallExpression) {
			Value result = loadFunctionCall((IASTFunctionCallExpression) rhsOp);
			lhsPointer.setPointedVar(result.getVar());
		} else if (rhsOp instanceof IASTLiteralExpression) {
			IVar var = BaseScopeCDT.addVarDecl(rhsOp.getRawSignature(), 
					AstInterpreter.getPrimitiveType(), IndirectionType.E_VARIABLE, _gvplGraph, _astInterpreter);
			lhsPointer.setPointedVar(var);
		} else {
			//TODO gambierre?
			if(rhsOp.getRawSignature().equals("NULL"))
			{			
				IVar var = BaseScopeCDT.addVarDecl("NULL", AstInterpreter.getPrimitiveType(), 
						IndirectionType.E_VARIABLE, _gvplGraph, _astInterpreter);
				lhsPointer.setPointedVar(var);
				return;
			}
			
			IVar rhsPointer;
			try {
				rhsPointer = loadPointedVar(rhsOp, _parentBaseScope);
			} catch (NotFoundException e) {
				_gvplGraph.addGraphNode("PROBLEM_" + e.getItemName(), NodeType.E_INVALID_NODE_TYPE);
				return;
			}
			lhsPointer.setPointedVar(rhsPointer);
		}
	}

	public Value loadFunctionCall(IASTFunctionCallExpression funcCall) {
		IASTExpression paramExpr = funcCall.getParameterExpression();
		IASTExpression nameExpr = funcCall.getFunctionNameExpression();
		
		if (nameExpr instanceof IASTIdExpression)
		{
			IASTIdExpression idExpr = (IASTIdExpression) nameExpr;
			IASTName name = idExpr.getName();
			IBinding idExprBinding = name.resolveBinding();
			
			if (idExprBinding instanceof ICPPMethod) {
				Function func = null;
				if (name instanceof ICPPASTQualifiedName) { // static function		
					IASTName[] names = ((ICPPASTQualifiedName)name).getNames();
					IASTName className = names[0];
					IASTName funcName = names[1];
					ClassDeclCDT classDecl = _astInterpreter.getClassDecl(className.resolveBinding());
					func = classDecl.getMemberFunc(funcName.resolveBinding());
				} else if (name instanceof IASTName){ // method from own class
					MemberFunc parentMF = getParentFunc();
					func = parentMF.getParentClass().getMemberFunc(idExprBinding);
				}
				else {
					logger.error("you're doing it wrong. {}, {}", name.getClass(), nameExpr);
					return new Value( _gvplGraph.addGraphNode("PROBLEM_NODE_" + name.getClass(), NodeType.E_INVALID_NODE_TYPE));
				}
				
				if (func == null) {
					logger.error("func must not be null, {}", nameExpr);
					return new Value(_gvplGraph.addGraphNode("PROBLEM_NODE", NodeType.E_INVALID_NODE_TYPE));
				}
				
				if (func.getIsStatic()) { // static function
					return loadStaticMethod(paramExpr, func);
				} else
					return loadOwnMethod(paramExpr, func);
				
			} else if (idExprBinding instanceof ICPPFunction) {
				return loadSimpleFunc(idExprBinding, paramExpr);
			} else if (idExprBinding instanceof IProblemBinding) {
				String problemName = ((IProblemBinding)idExprBinding).getMessage();
				logger.error("Problem binding: {}", problemName);
				return new Value(_gvplGraph.addGraphNode("PROBLEM_BINDING_" + problemName, NodeType.E_INVALID_NODE_TYPE));
			} else if (idExprBinding instanceof ITypedef) {
				String problemName = ((ITypedef)idExprBinding).getName();
				logger.warn("Class not working: CPPTypedef, {}", problemName);
				return new Value(_gvplGraph.addGraphNode("PROBLEM_CPPTypedef_" + problemName, NodeType.E_INVALID_NODE_TYPE));
			} else if (idExprBinding instanceof ICPPTemplateTypeParameter) {
				String problemName = ((ICPPTemplateTypeParameter)idExprBinding).getName();
				logger.warn("Class not working: CPPTemplateTypeParameter, {}", problemName);
				return new Value(_gvplGraph.addGraphNode("PROBLEM_CPPTemplateTypeParameter_" + problemName, NodeType.E_INVALID_NODE_TYPE));
			} else if (idExprBinding instanceof ICPPDeferredTemplateInstance) {
				ICPPDeferredTemplateInstance dfi = (ICPPDeferredTemplateInstance) idExprBinding;
				logger.warn("Class not working: CPPTemplateTypeParameter, {}", dfi.getName());
				return new Value(_gvplGraph.addGraphNode("PROBLEM_CPPTemplateTypeParameter_" + dfi.getName(), NodeType.E_INVALID_NODE_TYPE));
			} else
				logger.error("problem: instance: {}", idExprBinding.getClass());
		} else if (nameExpr instanceof IASTFieldReference) {
			return loadVarMethod(funcCall, paramExpr);
		} else
			logger.fatal("problem");

		return new Value(_gvplGraph.addGraphNode("PROBLEM_NODE", NodeType.E_INVALID_NODE_TYPE));
	}
	
	Value loadStaticMethod(IASTExpression paramExpr, Function func) {
		List<FuncParameter> parameterValues = loadFunctionParameters(func, paramExpr);
		return func.addFuncRef(parameterValues, _gvplGraph, _parentBaseScope);
	}
	
	/**
	 * Loads a simple function (i.e., a function that is not a instance method)
	 * @param idExprBinding
	 * @param paramExpr
	 * @param stLine
	 * @return
	 */
	private Value loadSimpleFunc(IBinding idExprBinding, IASTExpression paramExpr) {
		Function func = _astInterpreter.getFuncId(idExprBinding);
		
		if(func == null) {
			logger.warn("Func {} not found.", idExprBinding.getName());
			GraphNode problemNode = _gvplGraph.addGraphNode(idExprBinding.getName(), NodeType.E_INVALID_NODE_TYPE);
			return new Value(problemNode);
		}

		List<FuncParameter> parameterValues = loadFunctionParameters(func, paramExpr);
		return func.addFuncRef(parameterValues, _gvplGraph, _parentBaseScope);
	}
	
	/**
	 * Recursive function that returns the parent member function. Go deep in the parents
	 * @return Parent parent member func
	 */
	private MemberFunc getParentFunc() {
		BaseScope parent = (BaseScope) _parentBaseScope;
		while(!(parent instanceof MemberFunc)){
			BaseScope bb = (BaseScope) parent;
			parent = bb.getParent();
		}
		return (MemberFunc) parent;
	}
	
	/**
	 * Used when a method call a method of it's own instance
	 * @param paramExpr
	 * @return The graph node of the result of the function
	 */
	private Value loadOwnMethod(IASTExpression paramExpr, Function func) {
		MemberFunc parentMF = getParentFunc();
		
		List<FuncParameter> parameterValues = loadFunctionParameters(func, paramExpr);
		MemberFunc memberFunc = (MemberFunc) func;
		ClassVar var = parentMF.getThisReference();
		return memberFunc.addFuncRef(parameterValues, _gvplGraph, var, _parentBaseScope);
	}
	
	/**
	 * Used to call the method of a variable
	 * @param funcCall
	 * @param paramExpr
	 * @return The graph node of the result of the function
	 */
	private Value loadVarMethod(IASTFunctionCallExpression funcCall, IASTExpression paramExpr) {
		IASTFieldReference fieldRef = (IASTFieldReference) funcCall.getFunctionNameExpression();
		IASTExpression ownerExpr = fieldRef.getFieldOwner();
		IVar var;
		try {
			var = _parentBaseScope.getVarFromExpr(ownerExpr);
		} catch (NotFoundException e) {
			return new Value(_gvplGraph.addGraphNode("PROBLEM_NODE" + e.getItemName(), NodeType.E_INVALID_NODE_TYPE));
		} catch (ClassCastException e) {
			logger.error("Invalid cast with {}", ownerExpr.getRawSignature());
			return new Value(_gvplGraph.addGraphNode("PROBLEM_NODE" + ownerExpr.getRawSignature(), NodeType.E_INVALID_NODE_TYPE));
		}
		IBinding funcMemberBinding = fieldRef.getFieldName().resolveBinding();
		if(funcMemberBinding instanceof IProblemBinding) {
			logger.error("Problem binding. Member not found: {}", fieldRef.getFieldName());
			String nodeName = "PROBLEM_BINDING_FIELD_REF" + fieldRef.getFieldName();
			GraphNode problemGraphNode = _gvplGraph.addGraphNode(nodeName, NodeType.E_INVALID_NODE_TYPE);
			return new Value(problemGraphNode);
		} else if (funcMemberBinding instanceof  ICPPSpecialization) {
			funcMemberBinding = (( ICPPSpecialization)funcMemberBinding).getSpecializedBinding();
		}
		
		IVar varInMem = var.getVarInMem();
		if( varInMem == null ) {
			logger.error("varInMem is null. Var: {}", var.getName());
			return new Value(_gvplGraph.addGraphNode("PROBLEM_NODE_VMNULL", NodeType.E_INVALID_NODE_TYPE));
		}
		
		TypeId typeId = varInMem.getType();
		ClassDeclCDT classDecl =_astInterpreter.getClassDecl(typeId);
		if(classDecl == null) {
			logger.error("Problem on var {}", var.getName());
			return new Value(_gvplGraph.addGraphNode("PROBLEM_NODE_CDNULL", NodeType.E_INVALID_NODE_TYPE));
		}
		MemberFunc memberFunc = classDecl.getMemberFunc(funcMemberBinding);

		List<FuncParameter> parameterValues = loadFunctionParameters(memberFunc, paramExpr);
		
		if(var instanceof ClassVar) {
			return ((ClassVar)var).loadMemberFuncRef(memberFunc, parameterValues, _gvplGraph,
					_parentBaseScope);
		} else if(var instanceof MemAddressVar) {
			return ((MemAddressVar)var).loadMemberFuncRef(memberFunc, parameterValues, _gvplGraph,
					_parentBaseScope);
		} 
		return null;
	}

	private IASTNode[] getChildExpressions(IASTNode paramExpr) {
		if (paramExpr instanceof IASTExpressionList) {
			IASTExpressionList exprList = (IASTExpressionList) paramExpr;
			return exprList.getExpressions();
		} else if(paramExpr instanceof ICPPASTConstructorInitializer) {
			//CPPASTConstructorInitializer constrInit = (CPPASTConstructorInitializer) paramExpr;
			logger.fatal("Not implemented in this version of CDT");
			return null;
		} else {
			IASTNode[] parameters = new IASTNode[1];
			parameters[0] = paramExpr;
			return parameters;
		}
	}
	
	public List<FuncParameter> loadFunctionParameters(Function func, IASTNode paramExpr) {
		List<FuncParameter> parameterValues = new ArrayList<FuncParameter>();
		if (paramExpr == null)
			return parameterValues;

		IASTNode[] parameters = getChildExpressions(paramExpr);
		
		return loadFunctionParameters(func, parameters);
	}
	
	public List<FuncParameter> loadFunctionParameters(Function func, IASTNode[] parameters) {
		List<FuncParameter> parameterValues = new ArrayList<FuncParameter>();

		if(parameters == null) {
			logger.error("No parameters on func {}", func);
			return new ArrayList<FuncParameter>();
		}
		
		if(func == null) {
			logger.error("Func must not be null");
			return new ArrayList<FuncParameter>();
		}
		
		if(parameters.length != func.getNumParameters()) {
			logger.error("Number of parameters are different! {}", func);
			return new ArrayList<FuncParameter>();
		}

		for (int i = 0; i < parameters.length; i++) {
			IASTNode parameter = parameters[i];
			FuncParameter localParameter = null;
			FuncParameter insideFuncParameter = func.getOriginalParameter(i);

			if (insideFuncParameter.getType() == IndirectionType.E_POINTER) {
				IVar paramVar;
				try {
					paramVar = loadVarInAddress(parameter, _parentBaseScope);
				} catch (NotFoundException e) {
					continue;
				}
				localParameter = new FuncParameter(new Value(paramVar), IndirectionType.E_POINTER);
			} else if (insideFuncParameter.getType() == IndirectionType.E_REFERENCE) {
				Value value = _parentBaseScope.getValueFromExpr(parameter);
				localParameter = new FuncParameter(value, IndirectionType.E_REFERENCE);
			} else if (insideFuncParameter.getType() == IndirectionType.E_VARIABLE) {
				localParameter = new FuncParameter(loadValue(parameter), IndirectionType.E_VARIABLE);
			} else if (insideFuncParameter.getType() == IndirectionType.E_FUNCTION_POINTER) {
				IASTIdExpression idExpr = (IASTIdExpression) parameter;
				IBinding binding = idExpr.getName().resolveBinding();
				Function funcPointer = _astInterpreter.getFuncId(binding);
				localParameter = new FuncParameter(funcPointer);
			} else
				logger.fatal("Work here ");

			parameterValues.add(localParameter);
		}

		return parameterValues;
	}
	
	public List<FuncParameter> loadFunctionParameters(IASTNode[] parameters) {
		List<FuncParameter> parameterValues = new ArrayList<FuncParameter>();

		if(parameters == null) {
			logger.error("No parameters");
			return new ArrayList<FuncParameter>();
		}

		for (IASTNode parameter : parameters) {
			Value value = loadValue(parameter);
			IndirectionType indirType = IndirectionType.E_VARIABLE;
			{
				IVar var = value.getVar();
				if(var != null)
				{
					if(var instanceof PointerVar)
						indirType = IndirectionType.E_POINTER;
				}
			}
			FuncParameter localParameter = new FuncParameter(value, indirType);
			parameterValues.add(localParameter);

//			if (insideFuncParameter.getType() == IndirectionType.E_POINTER) {
//				IVar paramVar;
//				try {
//					paramVar = loadVarInAddress(parameter, _parentBaseScope);
//				} catch (NotFoundException e) {
//					continue;
//				}
//				localParameter = new FuncParameter(new Value(paramVar), IndirectionType.E_POINTER);
//			} else if (insideFuncParameter.getType() == IndirectionType.E_REFERENCE) {
//				Value value = _parentBaseScope.getValueFromExpr(parameter);
//				localParameter = new FuncParameter(value, IndirectionType.E_REFERENCE);
//			} else if (insideFuncParameter.getType() == IndirectionType.E_VARIABLE) {
//				localParameter = new FuncParameter(loadValue(parameter), IndirectionType.E_VARIABLE);
//			} else if (insideFuncParameter.getType() == IndirectionType.E_FUNCTION_POINTER) {
//				IASTIdExpression idExpr = (IASTIdExpression) parameter;
//				IBinding binding = idExpr.getName().resolveBinding();
//				Function funcPointer = _astInterpreter.getFuncId(binding);
//				localParameter = new FuncParameter(funcPointer);
//			} else
//				logger.fatal("Work here ");
//
//			parameterValues.add(localParameter);
		}

		return parameterValues;
	}

	GraphNode loadBinOp(IASTBinaryExpression binOp) {
		if(binOp == null) {
			logger.error("binOp must not be null");
			return _gvplGraph.addGraphNode("PROBLEM_NODE_binOpNULL", NodeType.E_INVALID_NODE_TYPE);
		}
		String opStr = CppMaps.getBinOpString(binOp.getOperator());
		Value lValue = loadValue(binOp.getOperand1());
		Value rValue = loadValue(binOp.getOperand2());
		if(lValue == null || rValue == null) {
			logger.error("lValue and rValue must be valid");
			return _gvplGraph.addGraphNode("PROBLEM_NODE_lrValueNULL", NodeType.E_INVALID_NODE_TYPE);
		}
		return _gvplGraph.addBinOp(opStr, lValue.getNode(), rValue.getNode(), _parentBaseScope);
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
	 * @throws NotFoundException 
	 */
	static IVar loadVarInAddress(IASTNode address, BaseScopeCDT astLoader) throws NotFoundException {
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
		if(unExpr instanceof IASTCastExpression) {
			return loadValue( ((IASTCastExpression)unExpr).getOperand() ).getNode();
		}
		
		// Check if the operator is a star
		int operator = unExpr.getOperator();
		if (operator == ICPPASTUnaryExpression.op_star){
			IASTExpression opExpr = unExpr.getOperand();
			IVar pointerVar;
			try {
				pointerVar = _parentBaseScope.getVarFromExpr(opExpr);
			} catch (NotFoundException e) {
				return _gvplGraph.addGraphNode("PROBLEM_" + e.getItemName(), NodeType.E_INVALID_NODE_TYPE);
			}
			if (!(pointerVar instanceof PointerVar))
				logger.fatal("not expected here");

			return pointerVar.getCurrentNode();
		} else if (operator == ICPPASTUnaryExpression.op_bracketedPrimary) {
			return loadValue(unExpr.getOperand()).getNode();
		} else if (operator == ICPPASTUnaryExpression.op_not) {
			IASTExpression opExpr = unExpr.getOperand();
			Value value = loadValue(opExpr);
			return _gvplGraph.addNotOp(value.getNode());
		} else if (operator == ICPPASTUnaryExpression.op_sizeof) {
			return _gvplGraph.addGraphNode("sizeof()" + unExpr.getRawSignature(), NodeType.E_DIRECT_VALUE);
		} else if (operator == ICPPASTUnaryExpression.op_minus) {
			return _gvplGraph.addGraphNode(unExpr.getRawSignature(), NodeType.E_DIRECT_VALUE);
		} else {
			logger.fatal("not implemented {}", unExpr.getRawSignature());
			String nodeName = "INVALID_CLASS_" + unExpr.getRawSignature();
			GraphNode problemGraphNode = _gvplGraph.addGraphNode(nodeName, NodeType.E_INVALID_NODE_TYPE);
			return problemGraphNode;
		}
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
	 * @throws NotFoundException 
	 */
	public static IVar loadPointedVar(IASTNode pointerExpr, BaseScopeCDT astLoader) throws NotFoundException {
		IVar pointerVar = astLoader.getVarFromExpr(pointerExpr);
		if (pointerVar instanceof PointerVar)
			return ((PointerVar) pointerVar).getVarInMem();
		else
			return loadVarInAddress(pointerExpr, astLoader);
	}

	public BaseScopeCDT getParentBasicBlock() {
		return _parentBaseScope;
	}
	
	public AstInterpreterCDT getAstInterpreter() {
		return _astInterpreter;
	}
	
	public Graph getGraph() {
		return _gvplGraph;
	}
}
