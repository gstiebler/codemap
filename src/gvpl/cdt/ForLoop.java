package gvpl.cdt;

import gvpl.common.ErrorOutputter;
import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class ForLoop extends AstLoader {

	/**
	 * Maps the external variables (from external graph) to internal generated
	 * variables
	 */
	private Map<Var, Var> _externalVars = new HashMap<Var, Var>();

	private Set<Var> _writtenExtVars = new HashSet<Var>();
	private Set<Var> _readExtVars = new HashSet<Var>();

	public ForLoop(Graph gvplGraph, AstLoader parent, AstInterpreter astInterpreter) {
		super(new Graph(-1), parent, astInterpreter);
		_gvplGraph.setLabel("ForLoop");
	}

	public void load(IASTForStatement node, Graph gvplGraph) {
		IASTStatement body = node.getBody();

		// loadHeader(node);

		BasicBlock basicBlockLoader = new BasicBlock(this, _astInterpreter);
		basicBlockLoader.load(body);

		int startingLine = node.getFileLocation().getStartingLineNumber();
		Map<GraphNode, GraphNode> map = gvplGraph.addSubGraph(_gvplGraph, this, startingLine);

		for (Map.Entry<Var, Var> entry : _externalVars.entrySet()) {
			Var extVarDecl = entry.getKey();
			Var intVarDecl = entry.getValue();

			GraphNode firstNode = map.get(intVarDecl.getFirstNode());
			GraphNode currentNode = map.get(intVarDecl.getCurrentNode(startingLine));

			if (_readExtVars.contains(intVarDecl))
				extVarDecl.getCurrentNode(startingLine).addDependentNode(firstNode, null,
						startingLine);

			if (_writtenExtVars.contains(intVarDecl))
				extVarDecl.receiveAssign(NodeType.E_VARIABLE, currentNode, null, startingLine);
		}
	}

	private void loadHeader(IASTForStatement node) {
		IASTStatement initializer = node.getInitializerStatement();
		IASTExpression condition = node.getConditionExpression();

		ForLoopHeader header = new ForLoopHeader(_gvplGraph, this, _astInterpreter);
		header.load(initializer, condition);

		int startingLine = node.getFileLocation().getStartingLineNumber();
		GraphNode headerNode = _gvplGraph.add_graph_node("ForHeader", NodeType.E_LOOP_HEADER,
				startingLine);
		for (Var readVar : header.getReadVars()) {
			readVar.getCurrentNode(startingLine).addDependentNode(headerNode, null, startingLine);
		}
	}

	/**
	 * Returns the DirectVarDecl of the reference to a variable
	 * 
	 * @return The DirectVarDecl of the reference to a variable
	 */
	@Override
	public Var getVarFromBinding(IASTExpression expr) {
		int startingLine = expr.getFileLocation().getStartingLineNumber();
		IASTIdExpression id_expr = null;
		if (expr instanceof IASTIdExpression)
			id_expr = (IASTIdExpression) expr;
		else
			ErrorOutputter.fatalError("problem here");

		Var extVarDecl = _parent.getVarFromBinding(expr);

		Var intVarDecl = _externalVars.get(extVarDecl);
		if (intVarDecl != null)
			return intVarDecl;

		String varName = id_expr.getName().toString();
		intVarDecl = new Var(_gvplGraph, varName, null);
		intVarDecl.initializeGraphNode(NodeType.E_VARIABLE, _gvplGraph, this, _astInterpreter,
				startingLine);
		_externalVars.put(extVarDecl, intVarDecl);
		return intVarDecl;
	}

}
