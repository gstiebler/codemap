package gvpl.common.ifclasses;

import gvpl.cdt.BasicBlockCDT;
import gvpl.cdt.InstructionLine;
import gvpl.common.IVar;
import gvpl.common.MemAddressVar;
import gvpl.graph.GraphNode;

import java.util.Map;

public class trueClass extends BoolValuePack {

	public trueClass(InstructionLine instructionLine, BasicBlockCDT basicBlock,
			Map<IVar, PrevTrueFalseNode> mapPrevTrueFalse,
			Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV, int startingLine) {
		super(instructionLine, basicBlock, mapPrevTrueFalse, mapPrevTrueFalseMV, startingLine);
	}

	void insertBoolNode(PrevTrueFalseNode prevTrueFalse, GraphNode node) {
		prevTrueFalse._true = node;
	}

	void insertBoolVar(PrevTrueFalseMemVar prevTrueFalse, MemAddressVar var) {
		prevTrueFalse._true = var;
	}
}