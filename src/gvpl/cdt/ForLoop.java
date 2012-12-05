package gvpl.cdt;

import gvpl.common.IVar;
import gvpl.common.VarInfo;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class ForLoop extends BasicBlockCDT {

	AstLoaderCDT _typeSource;
	
	public ForLoop(AstLoaderCDT parent, AstInterpreterCDT astInterpreter) {
		super(null, astInterpreter);
		_typeSource = parent;
		_gvplGraph.setLabel("ForLoop");
	}

	public void load(IASTForStatement node, Graph gvplGraph, AstLoaderCDT astLoader) {
		IASTStatement body = node.getBody();

		// loadHeader(node);

		BasicBlockCDT basicBlockLoader = new BasicBlockCDT(this, _astInterpreter);
		basicBlockLoader.load(body);
		basicBlockLoader.addToExtGraph();
		basicBlockLoader.bindSettedPointers();

		_parent = _typeSource;
		addSubGraph(gvplGraph);
	}
	
	@Override
	public VarInfo getTypeFromVarBinding(IBinding binding) {
		return _typeSource.getTypeFromVarBinding(binding);
	}
	

	@Override
	protected IVar getPreLoadedVarFromBinding(IBinding binding) {
		if(_parent == null)
			return createVarFromBinding(binding);
		return _parent.getPreLoadedVarFromBinding(binding);
	}

	private void loadHeader(IASTForStatement node) {
		IASTStatement initializer = node.getInitializerStatement();
		IASTExpression condition = node.getConditionExpression();

		ForLoopHeader header = new ForLoopHeader(_gvplGraph, this, _astInterpreter);
		header.load(initializer, condition);

		GraphNode headerNode = _gvplGraph.addGraphNode("ForHeader", NodeType.E_LOOP_HEADER);
		for (IVar readVar : header.getReadVars()) {
			readVar.getCurrentNode().addDependentNode(headerNode);
		}
	}

}
