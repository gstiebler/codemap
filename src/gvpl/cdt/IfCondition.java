package gvpl.cdt;

import java.util.ArrayList;
import java.util.List;

import gvpl.cdt.AstLoader.InExtVarPair;
import gvpl.common.Var;
import gvpl.graph.GraphNode;
import gvpl.graph.Graph.NodeType;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class IfCondition {
	
	static void loadIfCondition(IASTIfStatement ifStatement, InstructionLine instructionLine) {

		IASTExpression condition = ifStatement.getConditionExpression();
		GraphNode conditionNode = instructionLine.loadValue(condition);

		BasicBlock ifTrueBB = new BasicBlock(instructionLine.getParentBasicBlock(), instructionLine.getAstInterpreter());
		BasicBlock ifFalseBB = null;

		List<InExtVarPair> ifTrueWrittenVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> ifFalseWrittenVars = null;
		{
			IASTStatement thenClause = ifStatement.getThenClause();
			int startingLine = thenClause.getFileLocation().getStartingLineNumber();
			
			ifTrueBB.load(thenClause);
			
			ifTrueBB.getAccessedVars(new ArrayList<InExtVarPair>(), ifTrueWrittenVars, new ArrayList<InExtVarPair>(), startingLine);
			//ifTrueBB.addToExtGraph(startingLine);
		}

		IASTStatement elseClause = ifStatement.getElseClause();
		if (elseClause != null) {
			int startingLine = elseClause.getFileLocation().getStartingLineNumber();
			
			GraphNode notCondition = instructionLine.getGraph().addNotOp(conditionNode, instructionLine.getParentBasicBlock(),
					ifStatement.getFileLocation().getStartingLineNumber());

			ifFalseBB = new BasicBlock(instructionLine.getParentBasicBlock(), instructionLine.getAstInterpreter());
			ifFalseBB.load(elseClause);

			ifFalseWrittenVars = new ArrayList<InExtVarPair>();
			ifFalseBB.getAccessedVars(new ArrayList<InExtVarPair>(), ifFalseWrittenVars, new ArrayList<InExtVarPair>(), startingLine);
			//ifFalseBB.addToExtGraph(startingLine);
		}
		
		

		
		
		
		
		
		
		
/*
		Var var = varNodePair._varDecl;
				
		GraphNode ifTrue = var.getCurrentNode(startingLine);
		GraphNode ifFalse = varNodePair._graphNode;
				
		GraphNode ifOpNode = _gvplGraph.addGraphNode("If", NodeType.E_OPERATION, startingLine);

		ifTrue.addDependentNode(ifOpNode, this, startingLine);
		ifFalse.addDependentNode(ifOpNode, this, startingLine);
		conditionNode.addDependentNode(ifOpNode, this, startingLine);

		var.receiveAssign(NodeType.E_VARIABLE, ifOpNode, null, startingLine);*/
	}

}
