package gvpl.cdt;

import gvpl.cdt.function.Function;
import gvpl.common.AstLoader;
import gvpl.common.ClassVar;
import gvpl.common.IVar;
import gvpl.common.InExtVarPair;
import gvpl.common.InToExtVar;
import gvpl.common.MemAddressVar;
import gvpl.common.MemberId;
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
	protected IVar getVarFromBinding(IBinding binding) {
		return getVarInsideSandboxFromBinding(binding);
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
	
	protected AstLoader getParent() {
		return _parent;
	}
	
	public void getAccessedVars(List<InExtVarPair> read, List<InExtVarPair> written,
			List<InExtVarPair> ignored, InToExtVar inToExtMap) {
		ExecTreeLogger.log("");
		for (Map.Entry<IBinding, IVar> entry : _extToInVars.entrySet()) {
			IVar extVar = _parent.getVarFromBinding(entry.getKey());
			if (extVar == null)
				logger.fatal("extVar cannot be null");

			getAccessedVarsRecursive(entry.getValue(), extVar, read, written, ignored, inToExtMap);
		}
	}
	
	/**
	 * Gets the vars accessed/created in the block. It's recursive because it deals with
	 * members of class vars
	 * @param intVar The var created inside the block
	 * @param extVar the correspondant var in the parent of the block
	 * @param read The resulting list of the vars that were read
	 * @param written The resulting list of the vars that were written
	 * @param ignored The list of the vars that was not read nor written
	 * @param inToExtMap The map of the internal variables to the external variables 
	 */
	protected static void getAccessedVarsRecursive(IVar intVar, IVar extVar, List<InExtVarPair> read,
			List<InExtVarPair> written, List<InExtVarPair> ignored, InToExtVar inToExtMap) {
		
		if(extVar == null)
			return;
		
		IVar extVarInMem = extVar.getVarInMem();
		IVar intVarInMem = intVar.getVarInMem();
		
		if(extVarInMem == null)
			return;
		
		inToExtMap.put(intVar, extVar);
		
		if (intVarInMem instanceof ClassVar && extVarInMem instanceof ClassVar && 
				intVarInMem instanceof ClassVar) {
			ClassVar extClassVar = (ClassVar) extVarInMem;
			ClassVar intClassVar = (ClassVar) intVarInMem;
			for (MemberId memberId : intClassVar.getClassDecl().getMemberIds()) {
				IVar memberExtVar = extClassVar.getMember(memberId);
				IVar memberIntVar = intClassVar.getMember(memberId);
				getAccessedVarsRecursive(memberIntVar, memberExtVar, read, written, ignored, inToExtMap);
			}

			return;
		} else {
			logger.error("Some variables are ClassVar, some are not");
		}

		InExtVarPair varPair = new InExtVarPair(intVar, extVar);
		boolean accessed = false;
		if (intVar.onceRead()) {
			read.add(varPair);
			accessed = true;
		}

		if (intVar.onceWritten()) {
			written.add(varPair);
			accessed = true;
		}

		if (!accessed)
			ignored.add(varPair);
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
		getAccessedVars(readVars, writtenVars, ignoredVars, new InToExtVar(extGraph));
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
