package gvpl.cdt;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class BasicBlock extends AstLoader {
	
	public BasicBlock(AstLoader parent, AstInterpreter astInterpreter) {
		super(parent._graph_builder, parent, parent._cppMaps, astInterpreter);
	}

	public void load(IASTCompoundStatement cs) {
		IASTStatement[] statements = cs.getStatements();

		for (IASTStatement statement : statements) {
			InstructionLine instructionLine = new InstructionLine(_graph_builder, this, _cppMaps, _astInterpreter);
			instructionLine.load(statement);
		}
	}

}
