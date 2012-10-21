package gvpl.cdt;

import gvpl.common.Var;
import gvpl.common.VarInfo;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class ForLoop extends AstLoader {

	AstLoader _typeSource;
	
	public ForLoop(AstLoader parent, AstInterpreter astInterpreter, int startingLine) {
		super(new Graph(-1), null, astInterpreter);
		_typeSource = parent;
		_gvplGraph.setLabel("ForLoop");
	}

	public void load(IASTForStatement node, Graph gvplGraph, AstLoader astLoader) {
		int startingLine = node.getFileLocation().getStartingLineNumber();
		IASTStatement body = node.getBody();

		// loadHeader(node);

		BasicBlock basicBlockLoader = new BasicBlock(this, _astInterpreter);
		basicBlockLoader.load(body);
		basicBlockLoader.addToExtGraph(startingLine);
		basicBlockLoader.bindSettedPointers();

		_parent = _typeSource;
		addSubGraph(gvplGraph, startingLine);
	}
	
	@Override
	protected VarInfo getTypeFromVarBinding(IBinding binding) {
		return _typeSource.getTypeFromVarBinding(binding);
	}
	

	@Override
	protected Var getVarFromBinding(IBinding binding) {
		if(_parent == null)
			return createVarFromBinding(binding, -2);
		return _parent.getVarFromBinding(binding);
	}

	private void loadHeader(IASTForStatement node) {
		IASTStatement initializer = node.getInitializerStatement();
		IASTExpression condition = node.getConditionExpression();

		ForLoopHeader header = new ForLoopHeader(_gvplGraph, this, _astInterpreter);
		header.load(initializer, condition);

		int startingLine = node.getFileLocation().getStartingLineNumber();
		GraphNode headerNode = _gvplGraph.addGraphNode("ForHeader", NodeType.E_LOOP_HEADER,
				startingLine);
		for (Var readVar : header.getReadVars()) {
			readVar.getCurrentNode(startingLine).addDependentNode(headerNode, startingLine);
		}
	}

}
