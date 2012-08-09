package gvpl.cdt;

import gvpl.common.DirectVarDecl;
import gvpl.common.ErrorOutputter;
import gvpl.common.VarDecl;
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
	
	private Set<VarDecl> _writtenExtVars = new HashSet<VarDecl>();
	private Set<VarDecl> _readExtVars = new HashSet<VarDecl>();
	/** Maps the external variables (from external graph) to internal generated variables */
	private Map<VarDecl, VarDecl> _externalVars = new HashMap<VarDecl, VarDecl>();	
	
	public ForLoopHeader(GraphBuilder graphBuilder, AstLoader parent, CppMaps cppMaps, AstInterpreter astInterpreter) {
		super(graphBuilder, parent, cppMaps, astInterpreter);
	}
	
	public void load(IASTStatement initializer, IASTExpression condition) {
		InstructionLine instructionLineInit = new InstructionLine(_graphBuilder, _parent, _cppMaps, _astInterpreter);
		instructionLineInit.load(initializer);
		
		InstructionLine instructionLineCond = new InstructionLine(_graphBuilder, this, _cppMaps, _astInterpreter);
		instructionLineCond.load_value(condition);
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
		intVarDecl = new DirectVarDecl(_graphBuilder, varName , null);
		intVarDecl.initializeGraphNode(NodeType.E_VARIABLE);
		_externalVars.put(extVarDecl, intVarDecl);
		return intVarDecl;
	}
	
	@Override
	public void varWrite(VarDecl var) {
		if (_parent != null) 
			_parent.varWrite(var);
		
		_writtenExtVars.add(var);
	}
	
	@Override
	public void varRead(VarDecl var) {
		if (_parent != null) 
			_parent.varRead(var);
		
		_readExtVars.add(var);
	}
	
	public Iterable<VarDecl> getWrittenVars() {
		return _writtenExtVars;
	}
	
	public Iterable<VarDecl> getReadVars() {
		return _readExtVars;
	}
	
	
}
