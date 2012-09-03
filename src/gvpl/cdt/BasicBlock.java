package gvpl.cdt;

import gvpl.common.Var;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class BasicBlock extends AstLoader {
	
	private class VarNodePair {
		public Var _varDecl;
		public GraphNode _graphNode;
		public VarNodePair(Var varDecl, GraphNode graphNode) {
			_varDecl = varDecl;
			_graphNode = graphNode;
		}
	}
	
	private GraphNode _conditionNode;
	private List<VarNodePair> _writtenVar = new ArrayList<VarNodePair>();
	private Set<Var> _writtenVarSet = new HashSet<Var>();
	
	public BasicBlock(AstLoader parent, AstInterpreter astInterpreter, GraphNode conditionNode) {
		super(parent._graphBuilder, parent, parent._cppMaps, astInterpreter);
		_conditionNode = conditionNode;
	}

	public void load(IASTStatement baseStatement) {
		int startingLine = baseStatement.getFileLocation().getStartingLineNumber();
		
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
		
		if(_conditionNode != null) {
			for (VarNodePair varNodePair : _writtenVar) {
				Var var = varNodePair._varDecl;
				_graphBuilder.addIf(var, var.getCurrentNode(startingLine), varNodePair._graphNode, _conditionNode, null, startingLine);
			}
		}
	}
	
	@Override
	public void varWrite(Var var, int startingLine) {
		if (_parent != null) 
			_parent.varWrite(var, startingLine);
		
		if(!_writtenVarSet.contains(var)) {
			_writtenVarSet.add(var);
			_writtenVar.add(new VarNodePair(var, var.getCurrentNode(startingLine)));
		}
	}

}
