package gvpl.cdt;

import gvpl.common.ErrorOutputter;
import gvpl.common.Var;
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
	private Map<Var, Var> _externalVars = new HashMap<Var, Var>();	
	
	private Set<Var> _writtenExtVars = new HashSet<Var>();
	private Set<Var> _readExtVars = new HashSet<Var>();

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

		int startingLine = node.getFileLocation().getStartingLineNumber();
		Map<GraphNode, GraphNode> map = graphBuilder._gvplGraph.addSubGraph(_graphBuilder._gvplGraph, this, startingLine);
		
		for (Map.Entry<Var, Var> entry : _externalVars.entrySet()) {
			Var extVarDecl = entry.getKey();
			Var intVarDecl = entry.getValue();
			
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
		for(Var readVar : header.getReadVars()) {
			readVar.getCurrentNode(startingLine).addDependentNode(headerNode, null, startingLine);
		}
	}

	/**
	 * Returns the DirectVarDecl of the reference to a variable
	 * 
	 * @return The DirectVarDecl of the reference to a variable
	 */
	@Override
	public Var getVarOfReference(IASTExpression expr) {
		IASTIdExpression id_expr = null;
		if (expr instanceof IASTIdExpression)
			id_expr = (IASTIdExpression) expr;
		else
			ErrorOutputter.fatalError("problem here");

		Var extVarDecl = _parent.getVarOfReference(expr);

		Var intVarDecl = _externalVars.get(extVarDecl);
		if (intVarDecl != null)
			return intVarDecl;

		String varName = id_expr.getName().toString();
		intVarDecl = new Var(_graphBuilder._gvplGraph, varName , null);
		intVarDecl.initializeGraphNode(NodeType.E_VARIABLE, expr.getFileLocation().getStartingLineNumber());
		_externalVars.put(extVarDecl, intVarDecl);
		return intVarDecl;
	}
	
	@Override
	public void varWrite(Var var, int startingLIne) {
		if (_parent != null) 
			_parent.varWrite(var, startingLIne);
		
		_writtenExtVars.add(var);
	}
	
	@Override
	public void varRead(Var var) {
		if (_parent != null) 
			_parent.varRead(var);
		
		_readExtVars.add(var);
	}

}
