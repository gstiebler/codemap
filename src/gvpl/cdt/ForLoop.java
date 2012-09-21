package gvpl.cdt;

import gvpl.common.Var;
import gvpl.common.VarInfo;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		
		
		Map<GraphNode, GraphNode> map = gvplGraph.addSubGraph(_gvplGraph, this, startingLine);

		List<InExtVarPair> readVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> writtenVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> ignoredVars = new ArrayList<InExtVarPair>();
		
		_parent = _typeSource;
		for (Map.Entry<IBinding, Var> entry : _extToInVars.entrySet()) {
			getAccessedVars(entry.getValue(), entry.getKey(), readVars, writtenVars, ignoredVars, startingLine);
		}
		
		for(InExtVarPair readPair : readVars) {
			GraphNode firstNodeInNewGraph = map.get(readPair._in.getFirstNode());
			readPair._ext.getCurrentNode(startingLine).addDependentNode(firstNodeInNewGraph,
					astLoader, startingLine);
		}

		for(InExtVarPair writtenPair : writtenVars) {
			GraphNode currNodeInNewGraph = map.get(writtenPair._in.getCurrentNode(startingLine));
			writtenPair._ext.receiveAssign(NodeType.E_VARIABLE, currNodeInNewGraph, astLoader,
					startingLine);
		}
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
			readVar.getCurrentNode(startingLine).addDependentNode(headerNode, null, startingLine);
		}
	}

}
