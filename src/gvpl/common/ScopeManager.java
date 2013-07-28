package gvpl.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ScopeManager {

	static Stack<BaseScope> _scopeStack = new Stack<BaseScope>();
	
	public static void addScope(BaseScope scope) {
		_scopeStack.push(scope);
	}
	
	public static void removeScope() {
		_scopeStack.pop();
	}
	
	public static BaseScope getCurrentScope() {
		return _scopeStack.lastElement();
	}
	
	public static List<BaseScope> getScopeList() {
		return new ArrayList<BaseScope>(_scopeStack);
	}
}
