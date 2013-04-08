package gvpl.common.ifclasses;

import gvpl.cdt.BasicBlockCDT;
import gvpl.cdt.InstructionLine;
import gvpl.common.IVar;
import gvpl.common.MemAddressVar;
import gvpl.graph.GraphNode;

import java.util.Map;

import debug.ExecTreeLogger;

public class trueClass extends BoolValuePack {

	public trueClass(InstructionLine instructionLine, BasicBlockCDT basicBlock,
			Map<IVar, PrevTrueFalseNode> mapPrevTrueFalse,
			Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV) {
		super(instructionLine, basicBlock, mapPrevTrueFalse, mapPrevTrueFalseMV);

		ExecTreeLogger.log("construtor trueClass");
	}

	void insertBoolNode(PrevTrueFalseNode prevTrueFalse, GraphNode node) {
		ExecTreeLogger.log(node.getName());
		prevTrueFalse._true = node;
	}

	void insertBoolVar(PrevTrueFalseMemVar prevTrueFalse, MemAddressVar var) {
		ExecTreeLogger.log(var.getName());
		prevTrueFalse._true = var;
	}
}