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
	public void structure() {
		TestsUtil.baseTest("structure");
	}
	
	@Test
	public void memberFunc() {
		TestsUtil.baseTest("member_func");
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
	
}
