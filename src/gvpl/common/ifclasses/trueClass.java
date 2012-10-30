package gvpl.common.ifclasses;

import gvpl.cdt.InstructionLine;
import gvpl.common.IVar;
import gvpl.common.MemAddressVar;
import gvpl.graph.GraphNode;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class trueClass extends BoolValuePack {

	public trueClass(InstructionLine instructionLine, IASTStatement clause,
			Map<IVar, PrevTrueFalseNode> mapPrevTrueFalse,
			Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV) {
		super(instructionLine, clause, mapPrevTrueFalse, mapPrevTrueFalseMV);
	}

	void insertBoolNode(PrevTrueFalseNode prevTrueFalse, GraphNode node) {
		prevTrueFalse._true = node;
	}

	void insertBoolVar(PrevTrueFalseMemVar prevTrueFalse, MemAddressVar var) {
		prevTrueFalse._true = var;
	}
}