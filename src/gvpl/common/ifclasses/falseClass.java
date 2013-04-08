package gvpl.common.ifclasses;

import gvpl.cdt.BasicBlockCDT;
import gvpl.cdt.InstructionLine;
import gvpl.common.IVar;
import gvpl.common.MemAddressVar;
import gvpl.graph.GraphNode;

import java.util.Map;

import debug.ExecTreeLogger;

public class falseClass extends BoolValuePack {

	public falseClass(InstructionLine instructionLine, BasicBlockCDT basicBlock,
			Map<IVar, PrevTrueFalseNode> mapPrevTrueFalse,
			Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV) {
		super(instructionLine, basicBlock, mapPrevTrueFalse, mapPrevTrueFalseMV);
	}

	void insertBoolNode(PrevTrueFalseNode prevTrueFalse, GraphNode node) {
		ExecTreeLogger.log(node.getName());
		prevTrueFalse._false = node;
	}

	void insertBoolVar(PrevTrueFalseMemVar prevTrueFalse, MemAddressVar var) {
		ExecTreeLogger.log(var.getName());
		prevTrueFalse._false = var;
	}
}