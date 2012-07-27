package gvpl.cdt;

import gvpl.common.VarDecl;
import gvpl.graph.GraphNode;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class BasicBlock extends AstLoader {
	
	private GraphNode _conditionNode;
	private Map<VarDecl, GraphNode> _writtenVar = new HashMap<VarDecl, GraphNode>();
	
	public BasicBlock(AstLoader parent, AstInterpreter astInterpreter, GraphNode conditionNode) {
		super(parent._graph_builder, parent, parent._cppMaps, astInterpreter);
		_conditionNode = conditionNode;
	}

	public void load(IASTStatement baseStatement) {
		IASTStatement[] statements = null;
		if(baseStatement instanceof IASTCompoundStatement)
			statements = ((IASTCompoundStatement)baseStatement).getStatements();
		else if (baseStatement instanceof IASTExpressionStatement)
		{
			statements = new IASTStatement [1];
			statements[0] = baseStatement;
		}

		for (IASTStatement statement : statements) {
			InstructionLine instructionLine = new InstructionLine(_graph_builder, this, _cppMaps, _astInterpreter);
			instructionLine.load(statement);
		}
		
		if(_conditionNode != null) {
			for (Map.Entry<VarDecl, GraphNode> entry : _writtenVar.entrySet()) {
				VarDecl var = entry.getKey();
				_graph_builder.addIf(var, var.getCurrentNode(), entry.getValue(), _conditionNode, null);
			}
		}
	}
	
	@Override
	public void varWrite(VarDecl var) {
		if (_parent != null) 
			_parent.varWrite(var);
		
		if(!_writtenVar.containsKey(var))
			_writtenVar.put(var, var.getCurrentNode());
	}

}
