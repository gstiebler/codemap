package gvpl.common;

import gvpl.cdt.AstInterpreterCDT;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.List;

public interface IVar {

	
	public TypeId getType();

	public void updateNode(GraphNode node);
	
	public void updateNodes(GraphNode oldNode, GraphNode newNode);

	public GraphNode getCurrentNode();

	public void initializeVar(NodeType nodeType, Graph graph, AstInterpreterCDT astInterpreter);

	public void callConstructor(List<FuncParameter> parameter_values, NodeType nodeType, Graph graph,
			BaseScope astLoader, AstInterpreterCDT astInterpreter);

	/**
	 * Creates an assignment to this variable
	 * 
	 * @return New node from assignment, the left from assignment
	 */
	public GraphNode receiveAssign(NodeType lhsType, Value rhsNode, Graph graph);

	public String getName();

	public IVar getVarInMem();
	
	public List<IVar> getInternalVars();
	
	public VarInfo getVarInfo();
	
	public void setGraph(Graph graph);
	
	public Graph getGraph();
	
	public int getId();
}
