package tests;

import static org.junit.Assert.assertTrue;
import gvpl.cdt.Codemap;
import gvpl.common.FileFuncs;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestsUtil {
	
	static Logger logger = LogManager.getLogger(TestsUtil.class.getName());
	
	public static void baseTest(String testName) {
		String fixturesPath = System.getProperty("user.dir") + "/fixtures/";
		String examplePath = fixturesPath + testName + "/";
		String mainFile = examplePath + testName + ".cpp";
	
		String generatedFileName = examplePath + "generated.dot";
		File oldGeneratedFile = new java.io.File(generatedFileName);
		oldGeneratedFile.delete();
		
		Codemap.execute(examplePath, mainFile);

		try {
			List<String> original = FileFuncs.readLines(new java.io.File(examplePath + testName + ".dot"));
			List<String> generated = FileFuncs.readLines(new java.io.File(generatedFileName));
			assertTrue(original.equals(generated));
		} catch (Exception e) {
			
			e.printStackTrace();
			assertTrue(false);
		}
		
		System.out.println("Terminou de executar.");
	}
}
