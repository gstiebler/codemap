package gvpl.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gvpl.cdt.Function;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;
import gvpl.graph.Graph.NodeType;

public abstract class AstLoader {
	
	static Logger logger = LogManager.getLogger(Graph.class.getName());

	protected Graph _gvplGraph;
	
	public static IVar instanceVar(IndirectionType indirectionType, String name, TypeId typeId,
			Graph graph, AstLoader astLoader, AstInterpreter astInterpreter) {
		switch (indirectionType) {
		case E_VARIABLE:
			if (astInterpreter.isPrimitiveType(typeId))
				return new Var(graph, name, typeId);

			ClassDecl classDecl = astInterpreter.getClassDecl(typeId);
			return new ClassVar(graph, name, classDecl, astLoader);
		case E_POINTER:
			return new PointerVar(graph, name, typeId);
		case E_REFERENCE:
			return new ReferenceVar(graph, name, typeId);
		case E_INDIFERENT:
			{
				logger.fatal("Not expected");
				return null;
			}
		}
		return null;
	}
	
	public IVar addVarDecl(String name, TypeId type) {
		return instanceVar(IndirectionType.E_VARIABLE, name, type, _gvplGraph, this, getAstInterpreter());
	}
	
	public Graph getGraph() {
		return _gvplGraph;
	}
	
	protected static void getAccessedVarsRecursive(IVar intVar, IVar extVar, List<InExtVarPair> read,
			List<InExtVarPair> written, List<InExtVarPair> ignored, InToExtVar inToExtMap,
			int startingLine) {
		
		if(extVar == null)
			return;
		
		IVar extVarInMem = extVar.getVarInMem();
		IVar intVarInMem = intVar.getVarInMem();
		
		if(extVarInMem == null)
			return;
		
		inToExtMap.put(intVar, extVar);
		
		if (intVarInMem instanceof ClassVar) {
			ClassVar extClassVar = (ClassVar) extVarInMem;
			ClassVar intClassVar = (ClassVar) intVarInMem;
			for (MemberId memberId : intClassVar.getClassDecl().getMemberIds()) {
				IVar memberExtVar = extClassVar.getMember(memberId);
				IVar memberIntVar = intClassVar.getMember(memberId);
				getAccessedVarsRecursive(memberIntVar, memberExtVar, read, written, ignored, inToExtMap,
						startingLine);
			}

			return;
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
	 * Connects a external graph to the internal graph
	 * @param graph External graph
	 * @param startingLine
	 * @return A map from the internal graph nodes to the external graph nodes
	 */
	protected Map<GraphNode, GraphNode> addSubGraph(Graph graph, int startingLine) {
		
		Map<GraphNode, GraphNode> map = graph.addSubGraph(_gvplGraph, this, startingLine);

		List<InExtVarPair> readVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> writtenVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> ignoredVars = new ArrayList<InExtVarPair>();
		getAccessedVars(readVars, writtenVars, ignoredVars, new InToExtVar(graph), startingLine);
		
		for(InExtVarPair readPair : readVars) {
			GraphNode firstNodeInNewGraph = map.get(readPair._in.getFirstNode());
			GraphNode currNode = readPair._ext.getCurrentNode(startingLine);
			currNode.addDependentNode(firstNodeInNewGraph, startingLine);
		}

		for(InExtVarPair writtenPair : writtenVars) {
			GraphNode currNodeInNewGraph = map.get(writtenPair._in.getCurrentNode(startingLine));
			writtenPair._ext.receiveAssign(NodeType.E_VARIABLE, currNodeInNewGraph, startingLine);
		}
		
		return map;
	}

	public GraphNode addReturnStatement(GraphNode rvalue, TypeId type, String functionName,
			int startLine) {
		IVar var_decl = addVarDecl(functionName, type);
		return var_decl.receiveAssign(NodeType.E_RETURN_VALUE, rvalue, startLine);
	}

	public Function getFunction() {
		return getParent().getFunction();
	}
	
	protected abstract AstInterpreter getAstInterpreter();
	protected abstract AstLoader getParent();
	public abstract void getAccessedVars(List<InExtVarPair> read, List<InExtVarPair> written,
			List<InExtVarPair> ignored, InToExtVar inToExtMap, int startingLine);

}
