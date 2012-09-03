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
		_pointedVar = AstLoader.instanceVar(IndirectionType.E_VARIABLE, _name + "_pointed", _type,
				graph, astLoader, astInterpreter);
		_pointedVar.constructor(parameter_values, nodeType, graph, astLoader, astInterpreter,
				startingLine);
	}

}
