package gvpl.common;

import gvpl.cdt.function.Function;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The parent of the BasicBlock
 * @author gstiebler
 *
 */
public abstract class AstLoader implements IContext {
	
	static Logger logger = LogManager.getLogger(AstLoader.class.getName());

	protected List<ClassVar> _varsCreatedInThisScope = new ArrayList<ClassVar>();
	protected Graph _gvplGraph = null;
	
	public static IVar instanceVar(IndirectionType indirectionType, String name, TypeId typeId,
			Graph graph, AstInterpreter astInterpreter) {
		switch (indirectionType) {
		case E_VARIABLE:
			IVar result = null;
			if (astInterpreter.isPrimitiveType(typeId)) {
				result = new Var(graph, name, typeId);
			} else {
				ClassDecl classDecl = astInterpreter.getClassDecl(typeId);
				result = new ClassVar(graph, name, classDecl, astInterpreter);
			}

			return result;
		case E_POINTER:
			return new PointerVar(graph, name, typeId);
		case E_REFERENCE:
			return new ReferenceVar(graph, name, typeId);
		case E_INDIFERENT: {
			logger.fatal("Not expected");
			return null;
		}
		case E_FUNCTION_POINTER: {
			logger.fatal("Not expected");
			return null;
		}
		}
		return null;
	}
	
	public static IVar addVarDecl(String name, TypeId type, Graph graph, AstInterpreter astInterpreter) {
		return instanceVar(IndirectionType.E_VARIABLE, name, type, graph, astInterpreter);
	}
	
	/**
	 * Connects a external graph to the internal graph
	 * @param graph External graph
	 * @param startingLine
	 * @return A map from the internal graph nodes to the external graph nodes
	 */
	protected Map<GraphNode, GraphNode> addSubGraph(Graph graph) {
		return graph.addSubGraphCopy(_gvplGraph);
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
