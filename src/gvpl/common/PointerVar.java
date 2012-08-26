package gvpl.common;

import gvpl.graph.Graph;
import gvpl.graph.GraphBuilder.TypeId;

public class PointerVar extends MemAddressVar {
	
	public PointerVar(Graph gvplGraph, String name, TypeId type) {
		super(gvplGraph, name, type);
	}

	@Override
	public String getName() {
		return "*" + super.getName();
	}
	
}
