package gvpl.common;

import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;

public class ReferenceVar extends MemAddressVar {

	public ReferenceVar(Graph gvplGraph, String name, TypeId type) {
		super(gvplGraph, name, type);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public VarInfo getVarInfo() {
		return new VarInfo(_type, IndirectionType.E_REFERENCE);
	}

}
