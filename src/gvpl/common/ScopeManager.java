package gvpl.common;

import gvpl.common.ifclasses.IfScope;
import gvpl.graph.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScopeManager {

	static Logger logger = LogManager.getLogger(Graph.class.getName());
	static Stack<BaseScope> _scopeStack = new Stack<BaseScope>();
	
	public static void addScope(BaseScope scope) {
		_scopeStack.push(scope);
	}
	
	public static void removeScope(BaseScope scope) {
		if(scope != _scopeStack.lastElement())
			logger.error("Error removing scope");
		_scopeStack.pop();
	}
	
	public static BaseScope getCurrentScope() {
		return _scopeStack.lastElement();
	}
	
	public static List<BaseScope> getScopeList() {
		return new ArrayList<BaseScope>(_scopeStack);
	}
	
	public static void reset() {
		_scopeStack = new Stack<BaseScope>();
	}
	
	public static IfScope getLastIfScope() {
		for(int i = _scopeStack.size() - 1; i >= 0; i--) {
			if(_scopeStack.get(i) instanceof IfScope)
				return (IfScope) _scopeStack.get(i);
		}
		return null;
	}
}
