package gvpl.cdt;

import gvpl.common.BaseScope;
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
		
		IfScope ifScope = new IfScope(instructionLine.getParentBasicBlock(), conditionNode);
		
		ifScope.setKind(eIfScopeKind.E_THEN);
		ScopeManager.addScope(ifScope);
		BasicBlockCDT trueBasicBlock = loadBasicBlock(thenClause, instructionLine, ifScope);
		ScopeManager.removeScope(ifScope);
		
		ifScope.setKind(eIfScopeKind.E_ELSE);
		ScopeManager.addScope(ifScope);
		BasicBlockCDT falseBasicBlock = loadBasicBlock(elseClause, instructionLine, ifScope);
		ScopeManager.removeScope(ifScope);

		Graph graph = instructionLine.getGraph();
		IfCondition.createIfNodes(trueBasicBlock, falseBasicBlock, conditionNode, graph);
	
	}

	static BasicBlockCDT loadBasicBlock(IASTStatement clause, InstructionLine instructionLine, BaseScope parentScope) {
		if (clause == null)
			return null;

		ExecTreeLogger.log(clause.getRawSignature());

		BasicBlockCDT basicBlock = new BasicBlockCDT(parentScope,
				instructionLine.getAstInterpreter(), instructionLine.getGraph());
		basicBlock.load(clause);
		return basicBlock;
	}

}
