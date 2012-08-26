package gvpl.cdt;

import gvpl.common.ErrorOutputter;
import gvpl.common.Var;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class ForLoopHeader extends AstLoader {
	
	private Set<Var> _writtenExtVars = new HashSet<Var>();
	private Set<Var> _readExtVars = new HashSet<Var>();
	/** Maps the external variables (from external graph) to internal generated variables */
	private Map<Var, Var> _externalVars = new HashMap<Var, Var>();	
	
	public ForLoopHeader(GraphBuilder graphBuilder, AstLoader parent, CppMaps cppMaps, AstInterpreter astInterpreter) {
		super(graphBuilder, parent, cppMaps, astInterpreter);
	}
	
	public void load(IASTStatement initializer, IASTExpression condition) {
		InstructionLine instructionLineInit = new InstructionLine(_graphBuilder, _parent, _cppMaps, _astInterpreter);
		instructionLineInit.load(initializer);
		
		InstructionLine instructionLineCond = new InstructionLine(_graphBuilder, this, _cppMaps, _astInterpreter);
		instructionLineCond.loadValue(condition);
	}
	
	/**
	 * Returns the DirectVarDecl of the reference to a variable
	 * 
	 * @return The DirectVarDecl of the reference to a variable
	 */
	@Override
	public Var getVarOfReference(IASTExpression expr) {
		int startingLine = expr.getFileLocation().getStartingLineNumber();
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
		intVarDecl.initializeGraphNode(NodeType.E_VARIABLE, _graphBuilder._gvplGraph, this, _astInterpreter, startingLine);
		_externalVars.put(extVarDecl, intVarDecl);
		return intVarDecl;
	}
	
	@Override
	public void varWrite(Var var, int startingLine) {
		if (_parent != null) 
			_parent.varWrite(var, startingLine);
		
		_writtenExtVars.add(var);
	}
	
	@Override
	public void varRead(Var var) {
		if (_parent != null) 
			_parent.varRead(var);
		
		_readExtVars.add(var);
	}
	
	public Iterable<Var> getWrittenVars() {
		return _writtenExtVars;
	}
	
	public Iterable<Var> getReadVars() {
		return _readExtVars;
	}
	
	
}
