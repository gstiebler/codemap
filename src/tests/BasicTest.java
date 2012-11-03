package tests;

import org.junit.Test;

public class BasicTest {
	
	@Test
	public void basic() {
		TestsUtil.baseTest("basic");
	}
	
	@Test
	public void funcCall() {
		TestsUtil.baseTest("func_call");
	}
	
	@Test
	public void memberFunc() {
		TestsUtil.baseTest("member_func");
	}
	
	@Test
	public void memberFunc2() {
		TestsUtil.baseTest("member_func2");
	}
	
	@Test
	public void memberFuncInsideMemberFunc() {
		TestsUtil.baseTest("member_func_inside_member_func");
	}
	
	@Test
	public void funcInsideFunc() {
		TestsUtil.baseTest("func_inside_func");
	}
	
	@Test
	public void ifCall() {
		TestsUtil.baseTest("if_call");
	}
	
	@Test
	public void forLoop() {
		TestsUtil.baseTest("for_loop");
	}
	
	@Test
	public void operators() {
		TestsUtil.baseTest("operators");
	}
	
	@Test
	public void pointer() {
		TestsUtil.baseTest("pointer");
	}
	
	@Test
	public void pointerFunc() {
		TestsUtil.baseTest("pointer_func");
	}
	
	@Test
	public void reference() {
		TestsUtil.baseTest("reference");
	}
	
	@Test
	public void newOp() {
		TestsUtil.baseTest("new_op");
	}
	
	@Test
	public void classStructure() {
		TestsUtil.baseTest("class_structure");
	}
	
	@Test
	public void classNew() {
		TestsUtil.baseTest("class_new");
	}
	
	@Test
	public void constructor() {
		TestsUtil.baseTest("constructor");
	}
	
	@Test
	public void constructorChain() {
		TestsUtil.baseTest("constructor_chain");
	}

	@Test
	public void funcPointerReference() {
		TestsUtil.baseTest("func_pointer_reference");
	}

	@Test
	public void ifPointer() {
		TestsUtil.baseTest("if_pointer");
	}

	@Test
	public void inheritance() {
		TestsUtil.baseTest("inheritance");
	}

	@Test
	public void polymorphism() {
		TestsUtil.baseTest("polymorphism");
	}

	@Test
	public void polymorphism2() {
		TestsUtil.baseTest("polymorphism2");
	}

	@Test
	public void array() {
		TestsUtil.baseTest("array");
	}

	@Test
	public void destructor() {
		TestsUtil.baseTest("destructor");
	}
	
}
