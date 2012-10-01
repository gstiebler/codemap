package gvpl.common;

import gvpl.cdt.AstInterpreter;
import gvpl.cdt.AstLoader;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.List;

class PossiblePointedVar {
	Var _var;
	GraphNode _conditionNode;
	
	PossiblePointedVar(Var var, GraphNode conditionNode) {
		_var = var;
		_conditionNode = conditionNode;
	}
}

public class MemAddressVar extends Var {

	private List<PossiblePointedVar> _pointedVars = new ArrayList<PossiblePointedVar>();
	GraphNode _lastPointedVarNode = null;

	public MemAddressVar(Graph gvplGraph, String name, TypeId type) {
		super(gvplGraph, name, type);
		// TODO Auto-generated constructor stub
	}

	public void setPointedVar(Var pointedVarDecl) {
		_pointedVars.clear();
		_pointedVars.add(new PossiblePointedVar(pointedVarDecl, null));
	}
	
	public Var getPointedVar() {
		return _pointedVars.get(0)._var;
	}

	@Override
	public void updateNode(GraphNode node) {
		getPointedVar().updateNode(node);
	}

	@Override
	public GraphNode getFirstNode() {
		return getPointedVar().getFirstNode();
	}

	@Override
	public GraphNode getCurrentNode(int startingLine) {
		GraphNode currentPointedVarNode = getPointedVar().getCurrentNode(startingLine);
		if (currentPointedVarNode != _lastPointedVarNode) {
			_currGraphNode = _gvplGraph.addGraphNode(this, NodeType.E_VARIABLE, startingLine);
			currentPointedVarNode.addDependentNode(_currGraphNode, startingLine);
		}

		return _currGraphNode;
	}

	/**
	 * Cria-se um novo n� para a 
	 */
	@Override
	public GraphNode receiveAssign(NodeType lhs_type, GraphNode rhs_node, int startLocation) {
		//Cria-se um novo n� para a vari�vel "ponteiro"
		GraphNode newNode = super.receiveAssign(lhs_type, rhs_node, startLocation);
		//A vari�vel apontada recebe o n� rec�m-criado
		return getPointedVar().receiveAssign(lhs_type, newNode, startLocation);
	}

	@Override
	public void initializeGraphNode(NodeType nodeType, Graph graph, AstLoader astLoader,
			AstInterpreter astInterpreter, int startingLine) {
		Var var = AstLoader.instanceVar(IndirectionType.E_VARIABLE, _name + "_pointed", _type,
				graph, astLoader, astInterpreter);
		setPointedVar(var);
		var.initializeGraphNode(nodeType, graph, astLoader, astInterpreter, startingLine);
	}

	@Override
	public Var getVarInMem() {
		return getPointedVar();
	}

}
