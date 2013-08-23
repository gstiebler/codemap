package gvpl.common;

import gvpl.cdt.function.Function;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IBinding;

import debug.ExecTreeLogger;

/**
 * The parent of the BasicBlock
 * @author gstiebler
 *
 */
public abstract class BaseScope implements IScope {
	
	static Logger logger = LogManager.getLogger(BaseScope.class.getName());

	protected Graph _gvplGraph = null;
	private Map<Var, GraphNode> _lastWrittenNode = new LinkedHashMap<Var, GraphNode>();
	protected BaseScope _parent;
	protected Map<IBinding, IVar> _localVariables = new LinkedHashMap<IBinding, IVar>();
	
	protected BaseScope(BaseScope parent) {
		_parent = parent;
	}
	
	protected void resetBaseScope() {
		_lastWrittenNode = new LinkedHashMap<Var, GraphNode>();
	}
	
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
		return _parent.getFunction();
	}
	
	protected void lostScope() {
		logger.debug("Lost scope of graph {}", _gvplGraph.getName());
		for(IVar classVar : _localVariables.values()) {
			if(classVar instanceof ClassVar)
				((ClassVar)classVar).callDestructor(this, _gvplGraph);
		}
		
		if(_parent != null)
			_parent._lastWrittenNode.putAll(_lastWrittenNode);
		ScopeManager.removeScope(this);
	}
	
	public Graph getGraph() {
		return _gvplGraph;
	}
	
	public void varWritten(Var var) {
		_lastWrittenNode.put(var, var.getCurrentNode());
	}
	
	public Set<Var> getWrittenVars() {
		return new LinkedHashSet<Var>(_lastWrittenNode.keySet());
	}
	
	public GraphNode getLastNode(Var var) {
		GraphNode lastNode = _lastWrittenNode.get(var);
		if(lastNode != null)
			return lastNode;
		
		if(_parent != null)
			return _parent.getLastNode(var);
		else
			return null;
	}
	
	protected IVar getLocalVar(IBinding binding) {
		ExecTreeLogger.log(binding.getName());
		return _localVariables.get(binding);
	}
	
	public boolean hasVarInScope(IVar var) {
		return _localVariables.containsValue(var);
	}
	
	@Override
	public IVar getVarFromBinding(IBinding binding) {
		return _parent.getVarFromBinding(binding);
	}
	
	public BaseScope getParent() {
		return _parent;
	}

}
