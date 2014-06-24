package tests;

import static org.junit.Assert.assertTrue;
import gvpl.cdt.Codemap;
import gvpl.common.FileFuncs;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestsUtil {
	
	static Logger logger = LogManager.getLogger(TestsUtil.class.getName());
	
	public static void baseTest(String testName) {
		String fixturesPath = System.getProperty("user.dir") + "/fixtures/";
		String examplePath = fixturesPath + testName + "/";
		String mainFile = examplePath + testName + ".cpp";
		
		Codemap.execute(examplePath, mainFile);

		try {
			List<String> original = FileFuncs.readLines(new java.io.File(examplePath + testName + ".dot"));
			List<String> generated = FileFuncs.readLines(new java.io.File(examplePath + "generated.dot"));
			assertTrue(original.equals(generated));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Terminou de executar.");
	}
}
