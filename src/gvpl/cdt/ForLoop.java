package gvpl.cdt;

import gvpl.common.ErrorOutputter;
import gvpl.common.VarDecl;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphNode;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class ForLoop extends AstLoader {

	/** Maps the external variables (from external graph) to internal generated variables */
	private Map<VarDecl, VarDecl> _externalVars = new HashMap<VarDecl, VarDecl>();

	public ForLoop(GraphBuilder graph_builder, AstLoader parent, CppMaps cppMaps,
			AstInterpreter astInterpreter) {
		super(new GraphBuilder(), parent, cppMaps, astInterpreter);
		_graph_builder._gvpl_graph.setName("ForLoop");
	}

	public void load(IASTForStatement node, GraphBuilder graphBuilder) {
		IASTStatement body = node.getBody();

		BasicBlock basicBlockLoader = new BasicBlock(this, _astInterpreter, null);
		basicBlockLoader.load(body);

		Map<GraphNode, GraphNode> map = graphBuilder._gvpl_graph.addSubGraph(_graph_builder._gvpl_graph, this);

		for (Map.Entry<VarDecl, VarDecl> entry : _externalVars.entrySet()) {
			VarDecl extVarDecl = entry.getKey();
			VarDecl intVarDecl = entry.getValue();
			
			GraphNode firstNode = map.get(intVarDecl.getFirstNode());
			GraphNode currentNode = map.get(intVarDecl.getCurrentNode());
			
			extVarDecl.getCurrentNode().addDependentNode(firstNode, null);
			graphBuilder.add_assign(extVarDecl, NodeType.E_VARIABLE, currentNode, null);
		}
	}

	/**
	 * Returns the VarDecl of the reference to a variable
	 * 
	 * @return The VarDecl of the reference to a variable
	 */
	@Override
	public VarDecl getVarDeclOfReference(IASTExpression expr) {
		IASTIdExpression id_expr = null;
		if (expr instanceof IASTIdExpression)
			id_expr = (IASTIdExpression) expr;
		else
			ErrorOutputter.fatalError("problem here");

		VarDecl extVarDecl = _parent.getVarDeclOfReference(expr);

		VarDecl intVarDecl = _externalVars.get(extVarDecl);
		if (intVarDecl != null)
			return intVarDecl;

		String varName = id_expr.getName().toString();
		intVarDecl = _graph_builder.new DirectVarDecl(varName , null);
		intVarDecl.initializeGraphNode(NodeType.E_VARIABLE);
		_externalVars.put(extVarDecl, intVarDecl);
		return intVarDecl;
	}

}
