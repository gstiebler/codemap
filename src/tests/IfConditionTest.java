package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gvpl.cdt.AstInterpreterCDT;
import gvpl.cdt.BasicBlock;
import gvpl.cdt.ClassDecl;
import gvpl.cdt.IfCondition;
import gvpl.cdt.InToExtVar;
import gvpl.cdt.PrevTrueFalseMemVar;
import gvpl.common.ClassMember;
import gvpl.common.ClassVar;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.common.IVar;
import gvpl.common.MemAddressVar;
import gvpl.common.MemberId;
import gvpl.common.PossiblePointedVar;
import gvpl.common.TypeId;
import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class IfConditionTest {
	
	@Test
	public void TestMergeIfMAV_PrimitiveType() {
		GraphNode conditionNode = new GraphNode("true", NodeType.E_DIRECT_VALUE, -1);
		Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV = new LinkedHashMap<IVar, PrevTrueFalseMemVar>();
		
		AstInterpreterCDT astInterpreter = new AstInterpreterCDT(new Graph("", -1));
		
		// the graph in the calling block
		Graph extGraph = new Graph("graph", -1);
		InToExtVar inToExtVarTrue = new InToExtVar(extGraph);
		InToExtVar inToExtVarFalse = new InToExtVar(extGraph);
		TypeId type = null;
		// the original variable in the parent/external block
		MemAddressVar prev = new MemAddressVar(extGraph, "prev", type);
		PrevTrueFalseMemVar ptfm = new PrevTrueFalseMemVar();
		ptfm._prev = prev;
		
		IVar truePointedVar = null;
		GraphNode currTruePointedVar = null;
		IVar truePointedVarInTrueBlock = null;
		{
			// true block
			BasicBlock trueBasicBlock = new BasicBlock(null, astInterpreter);
			// the graph of the true block
			Graph trueGraph = trueBasicBlock.getGraph();
			// the pointed var of true
			truePointedVar = new Var(extGraph, "truePointedVar", type);
			truePointedVar.initializeVar(NodeType.E_VARIABLE, trueGraph, trueBasicBlock,
					astInterpreter, -1);
			currTruePointedVar = truePointedVar.getCurrentNode(-1);
			// the address var in the true block
			ptfm._true = new MemAddressVar(trueGraph, "true", type);

			// the pointed var in the true block
			truePointedVarInTrueBlock = new Var(trueGraph, "truePointedVarInTrueBlock", type);
			ptfm._true.setPointedVar(truePointedVarInTrueBlock);
		}
		
		IVar falsePointedVar = null;
		GraphNode currFalsePointedVar = null;
		IVar falsePointedVarInFalseBlock = null;
		{
			// false block
			BasicBlock falseBasicBlock = new BasicBlock(null, astInterpreter);
			// the graph of the true block
			Graph falseGraph = falseBasicBlock.getGraph();
			// the pointed var of false
			falsePointedVar = new Var(extGraph, "falsePointedVar", type);
			falsePointedVar.initializeVar(NodeType.E_VARIABLE, falseGraph, falseBasicBlock,
					astInterpreter, -1);
			currFalsePointedVar = falsePointedVar.getCurrentNode(-1);
			// the address var in the false block
			ptfm._false = new MemAddressVar(falseGraph, "true", type);

			// the pointed var in the false block
			falsePointedVarInFalseBlock = new Var(falseGraph, "falsePointedVarInFalseBlock", type);
			ptfm._false.setPointedVar(falsePointedVarInFalseBlock);
		}
		
		mapPrevTrueFalseMV.put(prev, ptfm);
		
		//inToExtVarTrue.put(ptfm._true, prev);
		inToExtVarTrue.put(truePointedVarInTrueBlock, truePointedVar);
		
		//inToExtVarFalse.put(ptfm._false, prev);
		inToExtVarFalse.put(falsePointedVarInFalseBlock, falsePointedVar);
		
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
	
	@Test
	public void TestMergeIfMAV_ClassVar() {
		GraphNode conditionNode = new GraphNode("true", NodeType.E_DIRECT_VALUE, -1);
		Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV = new LinkedHashMap<IVar, PrevTrueFalseMemVar>();

		AstInterpreterCDT astInterpreter = new AstInterpreterCDT(new Graph("", -1));

		ClassDecl classDecl = new ClassDecl(astInterpreter);
		MemberId memberId = new MemberId();
		ClassMember member = new ClassMember(memberId, "member",
				astInterpreter.getPrimitiveType(), IndirectionType.E_VARIABLE);
		classDecl.addMember(member);
		astInterpreter.addClassDeclInMaps(classDecl);

		BasicBlock mainBasicBlock = new BasicBlock(null, astInterpreter);
		// the graph in the calling block
		Graph extGraph = mainBasicBlock.getGraph();
		InToExtVar inToExtVarTrue = new InToExtVar(extGraph);
		InToExtVar inToExtVarFalse = new InToExtVar(extGraph);
		TypeId type = classDecl.getTypeId();
		// the original variable in the parent/external block
		MemAddressVar prev = new MemAddressVar(extGraph, "prev", type);
		PrevTrueFalseMemVar ptfm = new PrevTrueFalseMemVar();
		ptfm._prev = prev;

		ClassVar truePointedVar = null;
		IVar trueVarMember = null;
		IVar trueVarMemberInBlock = null;
		ClassVar truePointedVarInTrueBlock = null;
		{
			// true block
			BasicBlock trueBasicBlock = new BasicBlock(mainBasicBlock, astInterpreter);
			// the graph of the true block
			Graph trueGraph = trueBasicBlock.getGraph();
			// the pointed var in the true block
			truePointedVar = new ClassVar(extGraph, "truePointedVar", classDecl, mainBasicBlock);
			truePointedVar.initializeVar(NodeType.E_VARIABLE, extGraph, mainBasicBlock,
					astInterpreter, -1);
			trueVarMember = truePointedVar.getMember(memberId);
			// the address var in the true block
			ptfm._true = new MemAddressVar(trueGraph, "true", type);

			truePointedVarInTrueBlock = new ClassVar(trueGraph, "truePointedVarInTrueBlock", classDecl, trueBasicBlock);
			truePointedVarInTrueBlock.initializeVar(NodeType.E_VARIABLE, trueGraph,
					trueBasicBlock, astInterpreter, -1);
			trueVarMemberInBlock = truePointedVarInTrueBlock.getMember(memberId);
			ptfm._true.setPointedVar(truePointedVarInTrueBlock);
		}

		ClassVar falsePointedVar = null;
		IVar falseVarMember = null;
		IVar falseVarMemberInBlock = null;
		ClassVar falsePointedVarInFalseBlock = null; 
		{
			// false block
			BasicBlock falseBasicBlock = new BasicBlock(mainBasicBlock, astInterpreter);
			// the graph of the true block
			Graph falseGraph = falseBasicBlock.getGraph();
			// the pointed var in the false block
			falsePointedVar = new ClassVar(extGraph, "falsePointedVar", classDecl, mainBasicBlock);
			falsePointedVar.initializeVar(NodeType.E_VARIABLE, extGraph, mainBasicBlock,
					astInterpreter, -1);
			falseVarMember = falsePointedVar.getMember(memberId);
			// the address var in the false block
			ptfm._false = new MemAddressVar(falseGraph, "true", type);

			falsePointedVarInFalseBlock = new ClassVar(falseGraph, "falsePointedVarInFalseBlock", classDecl, falseBasicBlock);
			falsePointedVarInFalseBlock.initializeVar(NodeType.E_VARIABLE, falseGraph,
					falseBasicBlock, astInterpreter, -1);
			falseVarMemberInBlock = falsePointedVarInFalseBlock.getMember(memberId);
			ptfm._false.setPointedVar(falsePointedVar);
		}

		mapPrevTrueFalseMV.put(prev, ptfm);

		//inToExtVarTrue.put(ptfm._true, prev);
		inToExtVarTrue.put(truePointedVarInTrueBlock, truePointedVar);
		inToExtVarTrue.put(trueVarMemberInBlock, trueVarMember);
		
		//inToExtVarFalse.put(ptfm._false, prev);
		inToExtVarFalse.put(falsePointedVarInFalseBlock, falsePointedVar);
		inToExtVarFalse.put(falseVarMemberInBlock, falseVarMember);

		// the function to be tested
		IfCondition.mergeIfMAV(mapPrevTrueFalseMV, conditionNode, inToExtVarTrue, inToExtVarFalse);
		PossiblePointedVar ppv = (PossiblePointedVar) prev.getPointedVar();

		assertEquals(conditionNode, ppv._conditionNode);
		assertEquals(truePointedVar, ppv._varTrue._finalVar);
		assertEquals(falsePointedVar, ppv._varFalse._finalVar);

		assertEquals(extGraph, ppv._varTrue._finalVar.getGraph());
		assertEquals(extGraph, ppv._varFalse._finalVar.getGraph());
		
		
		IVar newTrueVarMember = ((ClassVar)ppv._varTrue._finalVar).getMember(memberId);
		assertEquals(trueVarMember, newTrueVarMember);
		
		IVar newFalseVarMember = ((ClassVar)ppv._varFalse._finalVar).getMember(memberId);
		assertEquals(falseVarMember, newFalseVarMember);

		assertEquals(extGraph, newTrueVarMember.getGraph());
		assertEquals(extGraph, newFalseVarMember.getGraph());

		// test this node
		/*GraphNode nodeFromIfVar = prev.getCurrentNode(-1);
		assertEquals(currTruePointedVar, nodeFromIfVar.getSourceNodes().get(0));
		assertEquals(currFalsePointedVar, nodeFromIfVar.getSourceNodes().get(1));
		assertEquals(conditionNode, nodeFromIfVar.getSourceNodes().get(2));

		assertTrue(currTruePointedVar.isDependentNode(nodeFromIfVar));
		assertTrue(currFalsePointedVar.isDependentNode(nodeFromIfVar));
		assertTrue(conditionNode.isDependentNode(nodeFromIfVar));*/
	}
	
}
