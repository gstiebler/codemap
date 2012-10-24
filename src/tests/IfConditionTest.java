package tests;

import gvpl.cdt.AstInterpreter;
import gvpl.cdt.BasicBlock;
import gvpl.cdt.IfCondition;
import gvpl.cdt.PrevTrueFalseMemVar;
import gvpl.common.IVar;
import gvpl.common.MemAddressVar;
import gvpl.common.PossiblePointedVar;
import gvpl.common.TypeId;
import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

public class IfConditionTest {
	
	@Test
	public void basic() {
		GraphNode conditionNode = new GraphNode("true", NodeType.E_DIRECT_VALUE, -1);
		Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV = new LinkedHashMap<IVar, PrevTrueFalseMemVar>();
		Map<IVar, IVar> inToExtVarTrue = new LinkedHashMap<IVar, IVar>();
		Map<IVar, IVar> inToExtVarFalse = new LinkedHashMap<IVar, IVar>();
		
		AstInterpreter astInterpreter = new AstInterpreter(new Graph("", -1));
		
		// the graph in the calling block
		Graph extGraph = new Graph("graph", -1);
		TypeId type = null;
		// the original variable in the parent/external block
		MemAddressVar prev = new MemAddressVar(extGraph, "prev", type);
		PrevTrueFalseMemVar ptfm = new PrevTrueFalseMemVar();
		ptfm._prev = prev;
		
		// true block
		BasicBlock trueBasicBlock = new BasicBlock(null, astInterpreter);
		// the graph of the true block
		Graph trueGraph = trueBasicBlock.getGraph();
		// the pointed var in the true block
		IVar truePointedVar = new Var(extGraph, "truePointedVar", type);
		truePointedVar.initializeGraphNode(NodeType.E_VARIABLE, trueGraph, trueBasicBlock, astInterpreter, -1);
		GraphNode currTruePointedVar = truePointedVar.getCurrentNode(-1);
		// the address var in the true block
		ptfm._true = new MemAddressVar(trueGraph, "true", type);
		ptfm._true.setPointedVar(truePointedVar);
		
		// true block
		BasicBlock falseBasicBlock = new BasicBlock(null, astInterpreter);
		// the graph of the true block
		Graph falseGraph = falseBasicBlock.getGraph();
		// the pointed var in the false block
		IVar falsePointedVar = new Var(extGraph, "falsePointedVar", type);
		falsePointedVar.initializeGraphNode(NodeType.E_VARIABLE, falseGraph, falseBasicBlock, astInterpreter, -1);
		GraphNode currFalsePointedVar = falsePointedVar.getCurrentNode(-1);
		// the address var in the false block
		ptfm._false = new MemAddressVar(falseGraph, "true", type);
		ptfm._false.setPointedVar(falsePointedVar);
		
		mapPrevTrueFalseMV.put(prev, ptfm);
		
		inToExtVarTrue.put(ptfm._true, prev);
		inToExtVarFalse.put(ptfm._false, prev);
		
		// the function to be tested
		IfCondition.mergeIfMAV(mapPrevTrueFalseMV, conditionNode, inToExtVarTrue, inToExtVarFalse);
		PossiblePointedVar ppv = (PossiblePointedVar) prev.getPointedVar();
		
		assertEquals(conditionNode, ppv._conditionNode);
		assertEquals(truePointedVar, ppv._varTrue._finalVar);
		assertEquals(falsePointedVar, ppv._varFalse._finalVar);
		
		assertEquals(extGraph, ppv._varTrue._finalVar.getGraph());
		assertEquals(extGraph, ppv._varFalse._finalVar.getGraph());
		
		// test this node
		GraphNode nodeFromIfVar = prev.getCurrentNode(-1);
		assertEquals(currTruePointedVar, nodeFromIfVar.getSourceNodes().get(0));
		assertEquals(currFalsePointedVar, nodeFromIfVar.getSourceNodes().get(1));
		assertEquals(conditionNode, nodeFromIfVar.getSourceNodes().get(2));
		
		assertTrue(currTruePointedVar.isDependentNode(nodeFromIfVar));
		assertTrue(currFalsePointedVar.isDependentNode(nodeFromIfVar));
		assertTrue(conditionNode.isDependentNode(nodeFromIfVar));
	}
	
}
