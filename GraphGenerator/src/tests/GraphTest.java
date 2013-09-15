package tests;

import static org.junit.Assert.assertEquals;
import gvpl.cdt.AstInterpreterCDT;
import gvpl.cdt.Codemap;
import gvpl.graph.Graph;

import org.junit.Test;

public class GraphTest {

	@Test
	public void loadAndSaveFile() {
		String testName = "constructor";
		String userDir = System.getProperty("user.dir");
		String fixturesPath = userDir + "/fixtures/";
		String examplePath = fixturesPath + testName + "/";
		String mainFile = examplePath + testName + ".cpp";
		
		AstInterpreterCDT astInterpreter = Codemap.execute(examplePath, mainFile);
		Graph originalGraph = astInterpreter.getGraph();
		String fileOutputName = userDir + "/bin/testGraphSave.out";
		originalGraph.saveToFile(fileOutputName);
		Graph loadedGraph = Graph.loadFromFile(fileOutputName);

		assertEquals(loadedGraph._subgraphs.get(0).getName(), "main");
	}
	
}
