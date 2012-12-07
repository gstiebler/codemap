package gvpl.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gvpl.cdt.function.Function;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;
import gvpl.graph.Graph.NodeType;

public abstract class AstLoader {
	
	static Logger logger = LogManager.getLogger(AstLoader.class.getName());

	protected List<ClassVar> _varsCreatedInThisScope = new ArrayList<ClassVar>();
	protected Graph _gvplGraph = null;
	
	public IVar instanceVar(IndirectionType indirectionType, String name, TypeId typeId,
			Graph graph, AstLoader astLoader, AstInterpreter astInterpreter) {
		switch (indirectionType) {
		case E_VARIABLE:
			IVar result = null;
			if (astInterpreter.isPrimitiveType(typeId)){
				result = new Var(graph, name, typeId);
			} else {
				ClassDecl classDecl = astInterpreter.getClassDecl(typeId);
				result = new ClassVar(graph, name, classDecl, astLoader);
			}
			
			return result;
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
	
	public IVar addVarDecl(String name, TypeId type, Graph graph) {
		return instanceVar(IndirectionType.E_VARIABLE, name, type, graph, this, getAstInterpreter());
	}
	
	/**
	 * Connects a external graph to the internal graph
	 * @param graph External graph
	 * @param startingLine
	 * @return A map from the internal graph nodes to the external graph nodes
	 */
	protected Map<GraphNode, GraphNode> addSubGraph(Graph graph) {
		return graph.addSubGraph(_gvplGraph, this);
	}

	public GraphNode addReturnStatement(GraphNode rvalue, TypeId type, String functionName, Graph graph) {
		IVar var_decl = addVarDecl(functionName, type, graph);
		return var_decl.receiveAssign(NodeType.E_RETURN_VALUE, rvalue);
	}

	public Function getFunction() {
		logger.fatal("ERROR");
		return null;
	}
	
	protected void lostScope() {
		logger.debug("Lost scope of graph {}", _gvplGraph.getName());
		for(ClassVar classVar : _varsCreatedInThisScope) {
			classVar.callDestructor(this, _gvplGraph);
		}
	}
	
	public Graph getGraph() {
		return _gvplGraph;
	}
	
	public abstract AstInterpreter getAstInterpreter();

}
