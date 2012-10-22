package gvpl.common;

import gvpl.cdt.AstInterpreter;
import gvpl.cdt.AstLoader;
import gvpl.cdt.MemberFunc;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.List;

public interface IVar {

	
	public TypeId getType();

	public void updateNode(GraphNode node);
	
	public void updateNodes(GraphNode oldNode, GraphNode newNode);

	public GraphNode getFirstNode();

	public GraphNode getCurrentNode(int startingLine);

	public void initializeGraphNode(NodeType nodeType, Graph graph, AstLoader astLoader,
			AstInterpreter astInterpreter, int startingLine);

	public void constructor(List<FuncParameter> parameter_values, NodeType nodeType, Graph graph,
			AstLoader astLoader, AstInterpreter astInterpreter, int startingLine);

	/**
	 * Creates an assignment to this variable
	 * 
	 * @return New node from assignment, the left from assignment
	 */
	public GraphNode receiveAssign(NodeType lhsType, GraphNode rhsNode, int startingLine);

	public String getName();

	public IVar getVarInMem();
	
	public void setOwner(IVar owner);
	
	public List<IVar> getInternalVars();
	
	public VarInfo getVarInfo();
	
	public boolean onceRead();
	
	public boolean onceWritten();
	
	public void setGraph(Graph graph);
	
	public GraphNode loadMemberFuncRef(MemberFunc memberFunc, List<FuncParameter> parameterValues,
			Graph graph, AstLoader astLoader, int startingLine);
	
	
}
