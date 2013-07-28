package gvpl.cdt;

import gvpl.cdt.function.Function;
import gvpl.common.BaseScope;
import gvpl.graph.Graph;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;

import debug.ExecTreeLogger;

public class BasicBlockCDT extends BaseScopeCDT {

	protected BaseScopeCDT _parent;
	
	public BasicBlockCDT(BaseScopeCDT parent, AstInterpreterCDT astInterpreter, Graph gvplGraph) {
		super(astInterpreter);
		_gvplGraph = gvplGraph;
		_parent = parent;
		_gvplGraph.setLabel("BasicBlockGraph");
	}

	public void load(IASTStatement baseStatement) {
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
	
	public BaseScope getParent() {
		return _parent;
	}
	
	@Override
	public Function getFunction() {
		return _parent.getFunction();
	}
	
	@Override
	public boolean hasVarInScope(IBinding binding) {
		if(super.hasVarInScope(binding))
			return true;
		
		return _parent.hasVarInScope(binding);
	}
	
}
