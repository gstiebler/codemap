package gvpl.cdt;

import gvpl.common.VarDecl;
import gvpl.graph.GraphNode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class BasicBlock extends AstLoader {
	
	private class VarNodePair {
		public VarDecl _varDecl;
		public GraphNode _graphNode;
		public VarNodePair(VarDecl varDecl, GraphNode graphNode) {
			_varDecl = varDecl;
			_graphNode = graphNode;
		}
	}
	
	private GraphNode _conditionNode;
	private LinkedList<VarNodePair> _writtenVar = new LinkedList<VarNodePair>();
	private Set<VarDecl> _writtenVarSet = new HashSet<VarDecl>();
	
	public BasicBlock(AstLoader parent, AstInterpreter astInterpreter, GraphNode conditionNode) {
		super(parent._graphBuilder, parent, parent._cppMaps, astInterpreter);
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
			InstructionLine instructionLine = new InstructionLine(_graphBuilder, this, _cppMaps, _astInterpreter);
			instructionLine.load(statement);
		}
		
		int startingLine = baseStatement.getFileLocation().getStartingLineNumber();
		
		if(_conditionNode != null) {
			for (VarNodePair varNodePair : _writtenVar) {
				VarDecl var = varNodePair._varDecl;
				_graphBuilder.addIf(var, var.getCurrentNode(startingLine), varNodePair._graphNode, _conditionNode, null, startingLine);
			}
		}
	}
	
	@Override
	public void varWrite(VarDecl var, int startingLine) {
		if (_parent != null) 
			_parent.varWrite(var, startingLine);
		
		if(!_writtenVarSet.contains(var)) {
			_writtenVarSet.add(var);
			_writtenVar.add(new VarNodePair(var, var.getCurrentNode(startingLine)));
		}
	}

}
