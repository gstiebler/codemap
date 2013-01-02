package gvpl.common;

import gvpl.cdt.AstLoaderCDT;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;

import java.util.List;

public class PointerVar extends MemAddressVar {

	public PointerVar(Graph gvplGraph, String name, TypeId type) {
		super(gvplGraph, name, type);
	}

	@Override
	public String getName() {
		return "*" + super.getName();
	}

	@Override
	public void callConstructor(List<FuncParameter> parameter_values, NodeType nodeType,
			Graph graph, AstLoader astLoader, AstInterpreter astInterpreter) {
		internalConstructor(parameter_values, nodeType, graph, astLoader, astInterpreter, _type);
	}

	public void constructor(List<FuncParameter> parameter_values, NodeType nodeType, Graph graph,
			AstLoaderCDT astLoader, AstInterpreter astInterpreter, TypeId type) {
		internalConstructor(parameter_values, nodeType, graph, astLoader, astInterpreter, type);
	}

	private void internalConstructor(List<FuncParameter> parameter_values, NodeType nodeType,
			Graph graph, AstLoader astLoader, AstInterpreter astInterpreter, TypeId type) {
		// creates the variable allocated with the new op
		IVar var = AstLoader.instanceVar(IndirectionType.E_VARIABLE, _name + "_pointed", type,
				graph, astInterpreter);
		// assigns the variable created with the new op
		initializePointedVar(var);
		var.callConstructor(parameter_values, nodeType, graph, astLoader, astInterpreter);
	}

	@Override
	public VarInfo getVarInfo() {
		return new VarInfo(_type, IndirectionType.E_POINTER);
	}

}
