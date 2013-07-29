package gvpl.common.ifclasses;

import gvpl.common.BaseScope;
import gvpl.graph.GraphNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IfScope extends BaseScope {

	public enum eIfScopeKind {
		E_THEN, E_ELSE
	};
	
	static Logger logger = LogManager.getLogger(BaseScope.class.getName());
	eIfScopeKind _kind;
	GraphNode _conditionNode;
	
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

}
