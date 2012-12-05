package gvpl.cdt;

import gvpl.common.AstLoader;
import gvpl.common.BasicBlock;
import gvpl.common.IVar;
import gvpl.common.InExtVarPair;
import gvpl.common.InToExtVar;
import gvpl.common.VarInfo;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;
import gvpl.graph.Graph.NodeType;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class BasicBlockCDT extends AstLoaderCDT {

	protected AstLoaderCDT _parent;
	protected Graph _gvplGraph = new Graph();
	
	public BasicBlockCDT(AstLoaderCDT parent, AstInterpreterCDT astInterpreter) {
		super(astInterpreter);
		_parent = parent;
		_gvplGraph.setLabel("BasicBlockGraph");
	}

	public void load(IASTStatement baseStatement) {
		IASTStatement[] statements = null;
		if (baseStatement instanceof IASTCompoundStatement)
			statements = ((IASTCompoundStatement) baseStatement).getStatements();
		else if (baseStatement instanceof IASTExpressionStatement) {
			statements = new IASTStatement[1];
			statements[0] = baseStatement;
		}

		for (IASTStatement statement : statements) {
			InstructionLine instructionLine = new InstructionLine(_gvplGraph, this, _astInterpreter);
			instructionLine.load(statement);
		}
		
		lostScope();
	}
	
	/**
	 * Add the nodes of the internal graph to the external graph
	 * @param startingLine
	 * @return Maps the nodes that were merged with others. The nodes in the key
	 *         of the map no longer exists.
	 */
	public Map<GraphNode, GraphNode> addToExtGraph() {
		return BasicBlock.addToExtGraph(_parent.getGraph(), this);
	}
	
	public void bindSettedPointers() {
		Graph extGraph = _parent.getGraph();
		List<InExtMAVarPair> addressVars = getAccessedMemAddressVar();
		for (InExtMAVarPair pair : addressVars) {
			IVar pointedVar = pair._in.getPointedVar();
			pointedVar.setGraph(extGraph);
			pair._ext.setPointedVar(pointedVar);
		}
	}
	
	@Override
	protected IVar getVarFromBinding(IBinding binding) {
		IVar var = getPreLoadedVarFromBinding(binding);
		if (var != null) 
			return var; 
		
		return createVarFromBinding(binding);	
	}
	
	protected IVar createVarFromBinding(IBinding binding) {
		VarInfo varInfo = getTypeFromVarBinding(binding);
		String name = binding.getName();
		
		IVar var =  instanceVar(varInfo._indirectionType, name, varInfo._type, _gvplGraph, this, _astInterpreter);
		//TODO only initialize a variable that will be read. Otherwise, the nodes generated
		// in the line below will never be used
		var.initializeVar(NodeType.E_VARIABLE, _gvplGraph, this, _astInterpreter);
		_extToInVars.put(binding, var);
		return var;
	}
	
	public Graph getGraph() {
		return _gvplGraph;
	}
	
	protected AstLoader getParent() {
		return _parent;
	}
	
	@Override
	public void getAccessedVars(List<InExtVarPair> read, List<InExtVarPair> written,
			List<InExtVarPair> ignored, InToExtVar inToExtMap) {
		for (Map.Entry<IBinding, IVar> entry : _extToInVars.entrySet()) {
			IVar extVar = _parent.getVarFromBinding(entry.getKey());
			if (extVar == null)
				logger.fatal("extVar cannot be null");

			getAccessedVarsRecursive(entry.getValue(), extVar, read, written, ignored, inToExtMap);
		}
	}
	
}
