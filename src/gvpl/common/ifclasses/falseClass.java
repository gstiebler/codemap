package gvpl.common.ifclasses;

import gvpl.cdt.BasicBlock;
import gvpl.cdt.InstructionLine;
import gvpl.common.IVar;
import gvpl.common.MemAddressVar;
import gvpl.graph.GraphNode;

import java.util.Map;

public class falseClass extends BoolValuePack {

	public falseClass(InstructionLine instructionLine, BasicBlock basicBlock,
			Map<IVar, PrevTrueFalseNode> mapPrevTrueFalse,
			Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV, int startingLine) {
		super(instructionLine, basicBlock, mapPrevTrueFalse, mapPrevTrueFalseMV, startingLine);
	}

	void insertBoolNode(PrevTrueFalseNode prevTrueFalse, GraphNode node) {
		prevTrueFalse._false = node;
	}

	void insertBoolVar(PrevTrueFalseMemVar prevTrueFalse, MemAddressVar var) {
		prevTrueFalse._false = var;
	}
}