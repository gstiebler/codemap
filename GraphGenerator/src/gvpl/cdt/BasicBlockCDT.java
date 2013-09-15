package gvpl.cdt;

import gvpl.cdt.function.Function;
import gvpl.common.BaseScope;
import gvpl.common.ScopeManager;
import gvpl.graph.Graph;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;

import debug.ExecTreeLogger;

public class BasicBlockCDT extends BaseScopeCDT {
	
	public BasicBlockCDT(BaseScope parent, AstInterpreterCDT astInterpreter, Graph gvplGraph) {
		super(astInterpreter, parent);
		_gvplGraph = gvplGraph;
		_parent = parent;
		//_gvplGraph.setLabel("BasicBlockGraph");
	}

	public void load(IASTStatement baseStatement) {
		ScopeManager.addScope(this);
		ExecTreeLogger.log("");
		IASTStatement[] statements = null;
		if (baseStatement instanceof IASTCompoundStatement)
			statements = ((IASTCompoundStatement) baseStatement).getStatements();
		else if (baseStatement instanceof IASTExpressionStatement) {
			statements = new IASTStatement[1];
			statements[0] = baseStatement;
		} else if (baseStatement instanceof CPPASTReturnStatement) { 
			statements = new IASTStatement[1];
			statements[0] = baseStatement;
		} else {
			logger.error("something wrong here: {}", baseStatement.getClass());
		}

		for (IASTStatement statement : statements) {
			InstructionLine instructionLine = new InstructionLine(_gvplGraph, this, _astInterpreter);
			instructionLine.load(statement);
		}
		
		lostScope();
	}
	
	public Graph getGraph() {
		return _gvplGraph;
	}
	
	@Override
	public Function getFunction() {
		return _parent.getFunction();
	}
	
}
