package tests;

import gvpl.cdt.IfCondition;
import gvpl.cdt.PrevTrueFalseMemVar;
import gvpl.common.IVar;
import gvpl.common.PossiblePointedVar;
import gvpl.common.MemAddressVar;
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
		
		// the graph in the calling block
		Graph extGraph = new Graph("graph", -1);
		// the graph of the true block
		Graph trueGraph = new Graph("trueGraph", -1);
		// the graph of the false block
		Graph falseGraph = new Graph("falseGraph", -1);
		TypeId type = null;
		// the original variable in the parent/external block
		MemAddressVar prev = new MemAddressVar(extGraph, "prev", type);
		PrevTrueFalseMemVar ptfm = new PrevTrueFalseMemVar();
		ptfm._prev = prev;
		
		// the pointed var in the true block
		IVar truePointedVar = new Var(extGraph, "truePointedVar", type);
		//truePointedVar.initializeGraphNode(nodeType, graph, astLoader, astInterpreter, -1);
		// the address var in the true block
		ptfm._true = new MemAddressVar(trueGraph, "true", type);
		ptfm._true.setPointedVar(truePointedVar);

		// the pointed var in the false block
		IVar falsePointedVar = new Var(extGraph, "falsePointedVar", type);
		// the address var in the false block
		ptfm._false = new MemAddressVar(falseGraph, "true", type);
		ptfm._false.setPointedVar(falsePointedVar);
		
		mapPrevTrueFalseMV.put(prev, ptfm);
		
		inToExtVarTrue.put(ptfm._true, prev);
		inToExtVarFalse.put(ptfm._false, prev);
		
		IfCondition.mergeIfMAV(mapPrevTrueFalseMV, conditionNode, inToExtVarTrue, inToExtVarFalse);
		PossiblePointedVar ppv = (PossiblePointedVar) prev.getPointedVar();
		
		assertEquals(conditionNode, ppv._conditionNode);
		assertEquals(truePointedVar, ppv._varTrue._finalVar);
		assertEquals(falsePointedVar, ppv._varFalse._finalVar);
		
		assertEquals(extGraph, ppv._varTrue._finalVar.getGraph());
		assertEquals(extGraph, ppv._varFalse._finalVar.getGraph());
		
		//GraphNode nodeFromIfVar = prev.getCurrentNode(-1);
	}
	
}
