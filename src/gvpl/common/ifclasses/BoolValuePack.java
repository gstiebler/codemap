package gvpl.common.ifclasses;

import gvpl.cdt.AstLoaderCDT;
import gvpl.cdt.BasicBlock;
import gvpl.cdt.InExtMAVarPair;
import gvpl.cdt.InToExtVar;
import gvpl.cdt.InstructionLine;
import gvpl.common.IVar;
import gvpl.common.InExtVarPair;
import gvpl.common.MemAddressVar;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTStatement;

public abstract class BoolValuePack {
	BasicBlock _ifBasicBlock = null;
	public Map<GraphNode, GraphNode> _ifMergedNodes = null;
	/** includes all member vars */
	public InToExtVar _inToExtVar = null;

	BoolValuePack(InstructionLine instructionLine, IASTStatement clause,
			Map<IVar, PrevTrueFalseNode> mapPrevTrueFalse,
			Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV) {
		if (clause == null)
			return;

		int startingLine = clause.getFileLocation().getStartingLineNumber();
		
		_inToExtVar = new InToExtVar(instructionLine.getGraph());

		AstLoaderCDT parentBasicBlock = instructionLine.getParentBasicBlock();
		_ifBasicBlock = new BasicBlock(parentBasicBlock, instructionLine.getAstInterpreter());
		_ifBasicBlock.load(clause);

		List<InExtVarPair> ifWrittenVars = new ArrayList<InExtVarPair>();
		// Get the accessed vars inside the block. This functions returns the variables created
		// inside the block, and the equivalent var from the calling block (external vars)
		_ifBasicBlock.getAccessedVars(new ArrayList<InExtVarPair>(), ifWrittenVars,
				new ArrayList<InExtVarPair>(), _inToExtVar, startingLine);
		for (InExtVarPair falseWrittenVarPair : ifWrittenVars) {
			IVar extVar = falseWrittenVarPair._ext;
			GraphNode currExtNode = falseWrittenVarPair._ext.getCurrentNode(startingLine);
			GraphNode currIntNode = falseWrittenVarPair._in.getCurrentNode(startingLine);

			PrevTrueFalseNode prevTrueFalse = mapPrevTrueFalse.get(extVar);
			if (prevTrueFalse == null)
				prevTrueFalse = new PrevTrueFalseNode();
			// the previous value is always the value that the variable was holding 
			// before the if and else blocks
			prevTrueFalse._prev = currExtNode;
			// the new value depends if it's an true or false (else) block
			insertBoolNode(prevTrueFalse, currIntNode);
			mapPrevTrueFalse.put(extVar, prevTrueFalse);
		}

		// the list of all pointers and reference variables
		List<InExtMAVarPair> addressVars = _ifBasicBlock.getAccessedMemAddressVar();
		for (InExtMAVarPair pair : addressVars) {
			PrevTrueFalseMemVar prevTrueFalse = mapPrevTrueFalseMV.get(pair._ext);
			if (prevTrueFalse == null)
				prevTrueFalse = new PrevTrueFalseMemVar();
			prevTrueFalse._prev = pair._ext;
			insertBoolVar(prevTrueFalse, pair._in);
			mapPrevTrueFalseMV.put(prevTrueFalse._prev, prevTrueFalse);
		}

		_ifMergedNodes = _ifBasicBlock.addToExtGraph(startingLine);
	}

	abstract void insertBoolNode(PrevTrueFalseNode prevTrueFalse, GraphNode node);
	abstract void insertBoolVar(PrevTrueFalseMemVar prevTrueFalse, MemAddressVar var);
}