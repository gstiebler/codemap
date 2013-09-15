package gvpl.common;

import gvpl.cdt.BaseScopeCDT;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;
import gvpl.graph.Graph.NodeType;

import java.util.List;

import debug.ExecTreeLogger;

public class PointerVar extends MemAddressVar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9163073112917903535L;

	public PointerVar(Graph gvplGraph, String name, TypeId type) {
		super(gvplGraph, name, type);
	}

	@Override
	public String getName() {
		return "*" + super.getName();
	}

	@Override
	public void callConstructor(List<FuncParameter> parameter_values, NodeType nodeType,
			Graph graph, BaseScope astLoader, AstInterpreter astInterpreter) {
		internalConstructor(parameter_values, nodeType, graph, astLoader, astInterpreter, _type);
	}

	public void constructor(List<FuncParameter> parameter_values, NodeType nodeType, Graph graph,
			BaseScopeCDT astLoader, AstInterpreter astInterpreter, TypeId type) {
		internalConstructor(parameter_values, nodeType, graph, astLoader, astInterpreter, type);
	}

	private void internalConstructor(List<FuncParameter> parameter_values, NodeType nodeType,
			Graph graph, BaseScope astLoader, AstInterpreter astInterpreter, TypeId type) {
		// creates the variable allocated with the new op
		IVar var = BaseScope.instanceVar(IndirectionType.E_VARIABLE, _name + "_pointed", type,
				graph, astInterpreter);
		// assigns the variable created with the new op
		initializePointedVar(var);
		var.callConstructor(parameter_values, nodeType, graph, astLoader, astInterpreter);
	}

	@Override
	public GraphNode receiveAssign(NodeType lhsType, Value rhsValue, Graph graph) {
		ExecTreeLogger.log("Var: " + getName());
		// Create a new node to the "pointer" variable
		GraphNode newNode = super.receiveAssign(lhsType, rhsValue, graph);

		return newNode;
	}

	@Override
	public VarInfo getVarInfo() {
		return new VarInfo(_type, IndirectionType.E_POINTER);
	}

}
