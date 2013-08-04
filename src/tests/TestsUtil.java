package tests;

import static org.junit.Assert.assertTrue;
import gvpl.cdt.AstInterpreterCDT;
import gvpl.cdt.Codemap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cesta.parsers.dot.DotTree;
import org.cesta.parsers.dot.DotTree.Graph;

public class TestsUtil {
	
	static Logger logger = LogManager.getLogger(TestsUtil.class.getName());
	
	public static void baseTest(String testName) {
		String fixturesPath = System.getProperty("user.dir") + "/fixtures/";
		String examplePath = fixturesPath + testName + "/";
		String mainFile = examplePath + testName + ".cpp";
		
		AstInterpreterCDT astInterpreter = Codemap.execute(examplePath, mainFile);
		
		Graph gvGraph = DotTree.getGraphFromDot(examplePath + testName + ".dot");
		assertTrue(GraphCompare.isEqual(astInterpreter.getGraph(), gvGraph));
		
		System.out.println("Terminou de executar.");
	}
}
