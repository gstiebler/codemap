package gvpl.common.ifclasses;

import gvpl.common.BaseScope;
import gvpl.common.ScopeManager;
import gvpl.common.Var;
import gvpl.graph.GraphNode;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IfScope extends BaseScope {

	public enum eIfScopeKind {
		E_THEN, E_ELSE
	};
	
	static Logger logger = LogManager.getLogger(BaseScope.class.getName());
	eIfScopeKind _kind;
	GraphNode _conditionNode;
	Set<Var> _createdVars = new LinkedHashSet<Var>();
	
	public IfScope(BaseScope parent, GraphNode conditionNode) {
		super(parent);
		_conditionNode = conditionNode;
	}
	
	public void setKind(eIfScopeKind kind) {
		_kind = kind;
	}
	
	public eIfScopeKind getKind() {
		return _kind;
	}
	
	public GraphNode getConditionNode() {
		return _conditionNode;
	}

	@Override
	protected void lostScope() {
		logger.debug("Lost scope of graph {}", _gvplGraph.getName());
		ScopeManager.removeScope(this);
	}
	
	public void varCreated(Var var) {
		_createdVars.add(var);
	}
	
	public Set<Var> getCreatedVars() {
		return new LinkedHashSet<Var>(_createdVars);
	}

	public static IfScope getLastIfScope() {
		List<BaseScope> scopeStack = ScopeManager.getScopeList();
		for(int i = scopeStack.size() - 1; i >= 0; i--) {
			if(scopeStack.get(i) instanceof IfScope)
				return (IfScope) scopeStack.get(i);
		}
		return null;
	}
	
	/**
	 * Searches de condition node in the scope stack. If there is some IfScope in the scope stack
	 * with the received conditionNode, the function returns the ScopeKind of the IfScope
	 * @param conditionNode
	 * @return IScopeKind of the IfScope that have the received conditionNode
	 */
	public static eIfScopeKind getScopeKind(GraphNode conditionNode) {
		List<BaseScope> scopeStack = ScopeManager.getScopeList();
		for(BaseScope scope : scopeStack) {
			if(scope instanceof IfScope) {
				IfScope ifScope = (IfScope)scope;
				if(ifScope._conditionNode == conditionNode)
					return ifScope._kind;
			}
		}
		return null;
	}
	
}
