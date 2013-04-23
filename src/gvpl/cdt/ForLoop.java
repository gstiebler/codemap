package gvpl.cdt;

import gvpl.common.IVar;
import gvpl.graph.Graph;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;

import debug.ExecTreeLogger;

public class ForLoop extends BasicBlockCDT {

	BaseScopeCDT _typeSource;
	
	public ForLoop(BaseScopeCDT parent, AstInterpreterCDT astInterpreter) {
		super(parent, astInterpreter);
		_typeSource = parent;
		_gvplGraph.setLabel("ForLoop");
	}

	public void load(IASTForStatement node, Graph gvplGraph, BaseScopeCDT astLoader) {
		IASTStatement body = node.getBody();

		loadHeader(node);

		load(body);
		
		gvplGraph.addSubGraph(_gvplGraph);
	}

	@Override
	protected IVar getVarInsideSandboxFromBinding(IBinding binding) {
		ExecTreeLogger.log(binding.getName());	
		return _parent.getVarInsideSandboxFromBinding(binding);
	}

	private void loadHeader(IASTForStatement node) {
		IASTStatement initializer = node.getInitializerStatement();
		IASTExpression condition = node.getConditionExpression();
		
		Graph headerGraph = new Graph();
		headerGraph.setLabel("ForLoopHeader");
		
		InstructionLine instructionLine = new InstructionLine(headerGraph, this, _astInterpreter);
		instructionLine.load(initializer);
		instructionLine.loadValue(condition);
		
		_gvplGraph.addSubGraph(headerGraph);
	}

}
