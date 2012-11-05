package gvpl.cdt;

import gvpl.common.IVar;
import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class ForLoopHeader extends AstLoaderCDT {
	
	Logger logger = LogManager.getLogger(Graph.class.getName());
	
	private Set<IVar> _writtenExtVars = new HashSet<IVar>();
	private Set<IVar> _readExtVars = new HashSet<IVar>();
	/**
	 * Maps the external variables (from external graph) to internal generated
	 * variables
	 */
	private Map<IVar, IVar> _externalVars = new LinkedHashMap<IVar, IVar>();

	public ForLoopHeader(Graph gvplGraph, AstLoaderCDT parent, AstInterpreterCDT astInterpreter) {
		super(gvplGraph, parent, astInterpreter);
	}

	public void load(IASTStatement initializer, IASTExpression condition) {
		InstructionLine instructionLineInit = new InstructionLine(_gvplGraph, _parent,
				_astInterpreter);
		instructionLineInit.load(initializer);

		InstructionLine instructionLineCond = new InstructionLine(_gvplGraph, this, _astInterpreter);
		instructionLineCond.loadValue(condition);
	}

	/**
	 * Returns the DirectVarDecl of the reference to a variable
	 * 
	 * @return The DirectVarDecl of the reference to a variable
	 */
	@Override
	public IVar getVarFromExpr(IASTExpression expr) {
		IASTIdExpression id_expr = null;
		if (expr instanceof IASTIdExpression)
			id_expr = (IASTIdExpression) expr;
		else
			logger.fatal("problem here");

		IVar extVarDecl = _parent.getVarFromExpr(expr);

		IVar intVarDecl = _externalVars.get(extVarDecl);
		if (intVarDecl != null)
			return intVarDecl;

		String varName = id_expr.getName().toString();
		intVarDecl = new Var(_gvplGraph, varName, null);
		intVarDecl.initializeVar(NodeType.E_VARIABLE, _gvplGraph, this, _astInterpreter);
		_externalVars.put(extVarDecl, intVarDecl);
		return intVarDecl;
	}

	public Iterable<IVar> getWrittenVars() {
		return _writtenExtVars;
	}

	public Iterable<IVar> getReadVars() {
		return _readExtVars;
	}

}
