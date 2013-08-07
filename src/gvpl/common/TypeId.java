package gvpl.common;

import org.eclipse.cdt.core.dom.ast.IBinding;

/** typedef */
public class TypeId {
	
	private IBinding _binding;
	
	public TypeId(IBinding binding) {
		_binding = binding;
	}
}