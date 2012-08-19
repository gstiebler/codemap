package gvpl.cdt;

import gvpl.common.DirectVarDecl;
import gvpl.common.ErrorOutputter;
import gvpl.common.VarDecl;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder;
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

	/** Maps the external variables (from external graph) to internal generated variables */
	private Map<VarDecl, VarDecl> _externalVars = new HashMap<VarDecl, VarDecl>();	
	
	private Set<VarDecl> _writtenExtVars = new HashSet<VarDecl>();
	private Set<VarDecl> _readExtVars = new HashSet<VarDecl>();

	public ForLoop(GraphBuilder graph_builder, AstLoader parent, CppMaps cppMaps,
			AstInterpreter astInterpreter) {
		super(new GraphBuilder(cppMaps), parent, cppMaps, astInterpreter);
		_graphBuilder._gvplGraph.setLabel("ForLoop");
	}

	public void load(IASTForStatement node, GraphBuilder graphBuilder) {
		IASTStatement body = node.getBody();
		
		//loadHeader(node);
		
		BasicBlock basicBlockLoader = new BasicBlock(this, _astInterpreter, null);
		basicBlockLoader.load(body);

		Map<GraphNode, GraphNode> map = graphBuilder._gvplGraph.addSubGraph(_graphBuilder._gvplGraph, this);

		int startingLine = node.getFileLocation().getStartingLineNumber();
		for (Map.Entry<VarDecl, VarDecl> entry : _externalVars.entrySet()) {
			VarDecl extVarDecl = entry.getKey();
			VarDecl intVarDecl = entry.getValue();
			
			GraphNode firstNode = map.get(intVarDecl.getFirstNode());
			GraphNode currentNode = map.get(intVarDecl.getCurrentNode(startingLine));
			
			if(_readExtVars.contains(intVarDecl))
				extVarDecl.getCurrentNode(startingLine).addDependentNode(firstNode, null, startingLine);
			
			if(_writtenExtVars.contains(intVarDecl))
				extVarDecl.receiveAssign(NodeType.E_VARIABLE, currentNode, null, startingLine);
		}
	}
	
	private void loadHeader(IASTForStatement node) {
		IASTStatement initializer = node.getInitializerStatement();
		IASTExpression condition = node.getConditionExpression();
		
		ForLoopHeader header = new ForLoopHeader(_graphBuilder, this, _cppMaps, _astInterpreter);
		header.load(initializer, condition);
		
		int startingLine = node.getFileLocation().getStartingLineNumber();
		GraphNode headerNode = _graphBuilder._gvplGraph.add_graph_node("ForHeader", NodeType.E_LOOP_HEADER, startingLine);
		for(VarDecl readVar : header.getReadVars()) {
			readVar.getCurrentNode(startingLine).addDependentNode(headerNode, null, startingLine);
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
		intVarDecl = new DirectVarDecl(_graphBuilder._gvplGraph, varName , null);
		intVarDecl.initializeGraphNode(NodeType.E_VARIABLE, expr.getFileLocation().getStartingLineNumber());
		_externalVars.put(extVarDecl, intVarDecl);
		return intVarDecl;
	}
	
	@Override
	public void varWrite(VarDecl var, int startingLIne) {
		if (_parent != null) 
			_parent.varWrite(var, startingLIne);
		
		_writtenExtVars.add(var);
	}
	
	@Override
	public void varRead(VarDecl var) {
		if (_parent != null) 
			_parent.varRead(var);
		
		_readExtVars.add(var);
	}

}
