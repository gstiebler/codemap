package gvpl.common;

import gvpl.cdt.AstInterpreter;
import gvpl.cdt.AstLoader;
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
	public void constructor(List<FuncParameter> parameter_values, NodeType nodeType, Graph graph,
			AstLoader astLoader, AstInterpreter astInterpreter, int startingLine) {
		internalConstructor(parameter_values, nodeType, graph, astLoader, astInterpreter, _type, startingLine);
	}

	public void constructor(List<FuncParameter> parameter_values, NodeType nodeType, Graph graph,
			AstLoader astLoader, AstInterpreter astInterpreter, TypeId type, int startingLine) {
		internalConstructor(parameter_values, nodeType, graph, astLoader, astInterpreter, type, startingLine);
	}
	
	private void internalConstructor(List<FuncParameter> parameter_values, NodeType nodeType, Graph graph,
			AstLoader astLoader, AstInterpreter astInterpreter, TypeId type, int startingLine) {
		// creates the variable allocated with the new op
		Var var = AstLoader.instanceVar(IndirectionType.E_VARIABLE, _name + "_pointed", type,
				graph, astLoader, astInterpreter);
		// assigns the variable created with the new op
		initializePointedVar(var);
		var.constructor(parameter_values, nodeType, graph, astLoader, astInterpreter,
				startingLine);
	}
	
	@Override
	public VarInfo getVarInfo() {
		return new VarInfo(_type, IndirectionType.E_POINTER);
	}

}
