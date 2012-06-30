package gvpl.cdt;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class LoadBasicBlock extends AstLoader {
	
	public LoadBasicBlock(AstLoader parent, AstInterpreter astInterpreter) {
		super(parent._graph_builder, parent, parent._cppMaps, astInterpreter);
	}

	public void load(IASTCompoundStatement cs) {
		IASTStatement[] statements = cs.getStatements();

		for (IASTStatement statement : statements) {
			LoadInstructionLine instructionLine = new LoadInstructionLine(_graph_builder, this, _cppMaps, _astInterpreter);
			instructionLine.load(statement);
		}
	}

}
