package gvpl.cdt;

import gvpl.cdt.function.Function;
import gvpl.common.AstLoader;
import gvpl.common.IVar;
import gvpl.common.InExtVarPair;
import gvpl.common.InToExtVar;
import gvpl.common.MemAddressVar;
import gvpl.common.VarInfo;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;

import debug.ExecTreeLogger;

public class BasicBlockCDT extends AstLoaderCDT {

	protected AstLoaderCDT _parent;
	
	public BasicBlockCDT(AstLoaderCDT parent, AstInterpreterCDT astInterpreter) {
		super(astInterpreter);
		_gvplGraph = new Graph();
		_parent = parent;
		_gvplGraph.setLabel("BasicBlockGraph");
	}

	public void load(IASTStatement baseStatement) {
		ExecTreeLogger.log("");
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
		ExecTreeLogger.log("");
		return addToExtGraph(_parent.getGraph(), this);
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
	public IVar getVarFromBinding(IBinding binding) {
		ExecTreeLogger.log(binding.getName());
		return getVarInsideSandboxFromBinding(binding);
	}
	
	@Override
	public IVar getVarFromBindingUnbounded(IBinding binding) {
		ExecTreeLogger.log(binding.getName());
		return null;
	}
	
	@Override
	public VarInfo getTypeFromVarBinding(IBinding binding) {
		ExecTreeLogger.log(binding.getName());
		VarInfo vi = _parent.getTypeFromVarBinding(binding);
		
		if(vi != null)
			return vi;
		else
			return super.getTypeFromVarBinding(binding);
	}
	
	public Graph getGraph() {
		return _gvplGraph;
	}
	
	public AstLoader getParent() {
		return _parent;
	}

	/**
	 * Add the nodes of the internal graph to the external graph
	 * @param startingLine
	 * @return Maps the nodes that were merged with others. The nodes in the key
	 *         of the map no longer exists.
	 */
	public Map<GraphNode, GraphNode> addToExtGraph(Graph extGraph, AstLoader astLoader) {
		ExecTreeLogger.log("Graph: " + extGraph.getName());
		Map<GraphNode, GraphNode> mergedNodes = new LinkedHashMap<GraphNode, GraphNode>();
		
		List<InExtVarPair> readVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> writtenVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> ignoredVars = new ArrayList<InExtVarPair>();
		getAccessedVars(readVars, writtenVars, ignoredVars, new InToExtVar(extGraph), _parent);
		extGraph.merge(astLoader.getGraph());

		// bind the vars from calling block to the internal read vars
		for(InExtVarPair readPair : readVars) {
			GraphNode intVarFirstNode = readPair._in.getFirstNode();
			// if someone read from internal var
			GraphNode extVarCurrNode = readPair._ext.getCurrentNode();
			extGraph.mergeNodes(extVarCurrNode, intVarFirstNode);
			// connect the var from the calling block to the correspodent var in this block
			mergedNodes.put(intVarFirstNode, extVarCurrNode);
		}
		
		// bind the vars from calling block to the internal written vars
		for (InExtVarPair writtenPair : writtenVars) {
			GraphNode intVarCurrNode = writtenPair._in.getCurrentNode();
			// if someone has written in the internal var

			writtenPair._ext.initializeVar(NodeType.E_VARIABLE, extGraph,
					astLoader, astLoader.getAstInterpreter());
			GraphNode extVarCurrNode = writtenPair._ext
					.getCurrentNode();
			// connect the var from the calling block to the correspodent var in this block
			extGraph.mergeNodes(extVarCurrNode, intVarCurrNode);
			mergedNodes.put(intVarCurrNode, extVarCurrNode);
		}
		
		for(InExtVarPair ignoredPair : ignoredVars) {
			extGraph.removeNode(ignoredPair._in.getFirstNode());
		}
		
		return mergedNodes;
	}
	
	@Override
	public Function getFunction() {
		return _parent.getFunction();
	}
	
	//TODO prepare to read member vars of each var. It's only working
	// for primitive types
	public List<InExtMAVarPair> getAccessedMemAddressVar() {
		ExecTreeLogger.log("");
		List<InExtMAVarPair> vars = new ArrayList<InExtMAVarPair>();
		
		for (Map.Entry<IBinding, IVar> entry : _extToInVars.entrySet()) {
			IVar intVar = entry.getValue();
			if(intVar instanceof MemAddressVar) {
				MemAddressVar extVar = (MemAddressVar) _parent.getVarFromBinding(entry.getKey());
				
				vars.add(new InExtMAVarPair((MemAddressVar)intVar, extVar));
			}
		}
		
		return vars;
	}
	
}
