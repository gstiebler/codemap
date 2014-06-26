package gvpl.cdt;

import gvpl.graph.GraphNode;
import gvpl.graph.Graph.NodeType;

import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;

public class Switch {
	
	public static void loadSwitch(ICPPASTSwitchStatement switchStatement, InstructionLine il) {
		IASTExpression controlExpr = switchStatement.getControllerExpression();
		IASTCompoundStatement body = (IASTCompoundStatement) switchStatement.getBody();
		GraphNode controlNode = il.loadValue(controlExpr).getNode();
		IASTStatement[] stmts = body.getStatements();
		int numStatements = stmts.length;
		
		IASTStatement defaultStatement = null;
		for (int i = 0; i < numStatements; i++) {
			IASTStatement statement = null;
			statement = body.getStatements()[i];
			if (!(statement instanceof IASTDefaultStatement)) continue;
			
			// the expression following the IASTDefaultStatement. It's the last
			defaultStatement = stmts[i + 1]; 
			break;
		}
		
		boolean first = true;
		for(int i = 0; i < numStatements; ) {
			IASTStatement statement = null;
			GraphNode condExpr = null;
			for(;i < numStatements; i++){
				statement = body.getStatements()[i];
				if(statement instanceof IASTCaseStatement) {
					IASTExpression caseExpr = ((IASTCaseStatement) statement).getExpression();
					condExpr = il.loadValue(caseExpr).getNode();
				} else if (statement instanceof IASTExpressionStatement) {
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
			
			GraphNode compareOpNode = il.getGraph().addGraphNode("==", NodeType.E_OPERATION);
			condExpr.addDependentNode(compareOpNode);
			controlNode.addDependentNode(compareOpNode);

			IfConditionCDT.loadIfCondition(compareOpNode, statement, elseStatement, il);
			first = false;
		}
	}

}
