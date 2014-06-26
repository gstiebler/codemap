package gvpl.cdt;

import gvpl.clang.CPPASTCompoundStatement;
import gvpl.common.Value;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;

class SwitchCase {
	IASTCaseStatement caseStmt;
	List<IASTExpressionStatement> exprStatements = new ArrayList<IASTExpressionStatement>();
	
	public static CPPASTCompoundStatement getCompoundStatement(List<IASTExpressionStatement> exprStatements) {
		List<IASTStatement> statements = new ArrayList<IASTStatement>();
		for(IASTExpressionStatement exprStatement : exprStatements)
			statements.add(exprStatement);
		return new CPPASTCompoundStatement(statements);
	}
	
	CPPASTCompoundStatement getCompoundStatement() {
		return getCompoundStatement(exprStatements);
	}
}

public class Switch {
	
	static Logger logger = LogManager.getLogger(Switch.class.getName());
	
	List<SwitchCase> _cases = new ArrayList<SwitchCase>();
	List<SwitchCase> _activeCases = new ArrayList<SwitchCase>();
	List<IASTExpressionStatement> _defaultStatements = new ArrayList<IASTExpressionStatement>();
	boolean _defaultFound = false;
	
	void breakFound() {
		_activeCases.clear();
	}
	
	void defaultFound() {
		_defaultFound = true;
	}
	
	void addStatement(IASTExpressionStatement statement) {
		for(SwitchCase sc : _activeCases) {
			sc.exprStatements.add(statement);
		}
		if(_defaultFound)
			_defaultStatements.add(statement);
	}
	
	void addCase(IASTCaseStatement caseStmt) {
		SwitchCase sc = new SwitchCase();
		sc.caseStmt = caseStmt;
		_cases.add(sc);
		_activeCases.add(sc);
	}
	
	CPPASTCompoundStatement getDefaultStatements() {
		return SwitchCase.getCompoundStatement(_defaultStatements);
	}
	
	public static void loadSwitch(ICPPASTSwitchStatement switchStatement, InstructionLine il) {
		IASTExpression controlExpr = switchStatement.getControllerExpression();
		IASTCompoundStatement body = (IASTCompoundStatement) switchStatement.getBody();
		GraphNode controlNode = il.loadValue(controlExpr).getNode();
		IASTStatement[] stmts = body.getStatements();
		int numStatements = stmts.length;
		
		Switch sInstance = new Switch();
		for (int i = 0; i < numStatements; i++) {
			IASTStatement statement = null;
			statement = body.getStatements()[i];
			if(statement instanceof IASTCaseStatement)
				sInstance.addCase((IASTCaseStatement) statement);
			else if(statement instanceof IASTDefaultStatement)
				sInstance.defaultFound();
			else if (statement instanceof IASTExpressionStatement)
				sInstance.addStatement((IASTExpressionStatement) statement);
			else if (statement instanceof  IASTBreakStatement)
				sInstance.breakFound();
			else
				logger.error("Not expected");
		}
		
		boolean first = true;
		for(SwitchCase sc : sInstance._cases) {
			Value condValue = il.loadValue(sc.caseStmt.getExpression());
			GraphNode condExpr = condValue.getNode();			
			GraphNode compareOpNode = il.getGraph().addGraphNode("==", NodeType.E_OPERATION);
			condExpr.addDependentNode(compareOpNode);
			controlNode.addDependentNode(compareOpNode);
			
			IASTStatement elseStatement = null;
			if(first == true)
				elseStatement = sInstance.getDefaultStatements();
			
			IfConditionCDT.loadIfCondition(compareOpNode, sc.getCompoundStatement(), elseStatement, il);	
			first = false;
		}
	}

}
