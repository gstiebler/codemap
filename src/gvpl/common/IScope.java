package gvpl.common;

import org.eclipse.cdt.core.dom.ast.IBinding;

public interface IScope {
	
	abstract IVar getVarFromBinding(IBinding binding);
	abstract IVar getVarFromBindingUnbounded(IBinding binding);
	
}
