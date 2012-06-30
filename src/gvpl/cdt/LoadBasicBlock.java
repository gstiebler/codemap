package gvpl.cdt;

import gvpl.graph.GraphBuilder.DirectVarDecl;
import gvpl.graph.GraphBuilder.TypeId;
import gvpl.graph.GraphBuilder.VarId;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
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
	
	public DirectVarDecl load_var_decl(IASTDeclarator decl, TypeId type) {
		IASTName name = decl.getName();

		VarId id = _graph_builder.new VarId();
		DirectVarDecl var_decl = _graph_builder.new DirectVarDecl(id, name.toString(), type);
		addVarDecl(name.resolveBinding(), id);
		_graph_builder.add_var_decl(var_decl);

		return var_decl;
	}

}
