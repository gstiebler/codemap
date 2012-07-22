package gvpl.cdt;

import gvpl.graph.GraphNode;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class BasicBlock extends AstLoader {
	
	private GraphNode _conditionNode = null;
	
	public BasicBlock(AstLoader parent, AstInterpreter astInterpreter, GraphNode conditionNode) {
		super(parent._graph_builder, parent, parent._cppMaps, astInterpreter);
		_conditionNode = conditionNode;
	}

	public void load(IASTCompoundStatement cs) {
		IASTStatement[] statements = cs.getStatements();

		for (IASTStatement statement : statements) {
			InstructionLine instructionLine = new InstructionLine(_graph_builder, this, _cppMaps, _astInterpreter);
			instructionLine.load(statement);
		}
	}
	
	public GraphNode getCondition() {
		return _conditionNode;
	}

}
