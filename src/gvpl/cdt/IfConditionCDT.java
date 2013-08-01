package gvpl.cdt;

import gvpl.common.ScopeManager;
import gvpl.common.ifclasses.IfCondition;
import gvpl.common.ifclasses.IfScope;
import gvpl.common.ifclasses.IfScope.eIfScopeKind;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

import debug.ExecTreeLogger;

public class IfConditionCDT {

	static void loadIfCondition(IASTIfStatement ifStatement, InstructionLine instructionLine) {
		IASTExpression condition = ifStatement.getConditionExpression();
		GraphNode conditionNode = instructionLine.loadValue(condition).getNode();
		loadIfCondition(conditionNode, ifStatement.getThenClause(), ifStatement.getElseClause(),
				instructionLine);
	}
	
	static void loadIfCondition(GraphNode conditionNode, IASTStatement thenClause,
			IASTStatement elseClause, InstructionLine instructionLine) {
		ExecTreeLogger.log(conditionNode.getName());
		
		IfScope trueIfScope = new IfScope(instructionLine.getParentBasicBlock(), conditionNode);
		trueIfScope.setKind(eIfScopeKind.E_THEN);
		BasicBlockCDT trueBasicBlock = loadBasicBlock(thenClause, instructionLine, trueIfScope);

		IfScope falseIfScope = new IfScope(instructionLine.getParentBasicBlock(), conditionNode);
		falseIfScope.setKind(eIfScopeKind.E_ELSE);
		BasicBlockCDT falseBasicBlock = loadBasicBlock(elseClause, instructionLine, falseIfScope);

		Graph graph = instructionLine.getGraph();
		IfCondition.createIfNodes(trueBasicBlock, falseBasicBlock, conditionNode, graph);	
	}

	static BasicBlockCDT loadBasicBlock(IASTStatement clause, InstructionLine instructionLine, IfScope parentScope) {
		if (clause == null)
			return null;

		ScopeManager.addScope(parentScope);
		ExecTreeLogger.log(clause.getRawSignature());

		BasicBlockCDT basicBlock = new BasicBlockCDT(parentScope,
				instructionLine.getAstInterpreter(), instructionLine.getGraph());
		basicBlock.load(clause);
		ScopeManager.removeScope(parentScope);
		return basicBlock;
	}

}
