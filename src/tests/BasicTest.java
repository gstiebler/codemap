package tests;

import static org.junit.Assert.assertTrue;
import gvpl.cdt.AstInterpreter;
import gvpl.common.File;
import gvpl.graph.GraphBuilder;
import gvpl.graphviz.FileDriver;
import gvpl.graphviz.Visualizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.cesta.parsers.dot.DotTree;
import org.cesta.parsers.dot.DotTree.Graph;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.parser.scanner2.FileCodeReaderFactory;
import org.eclipse.core.runtime.CoreException;
import org.junit.Test;

public class BasicTest {
	
	@Test
	public void outroTeste() {
		baseTest("K:/Projetos/GVPL/fixtures/", "func_call");
	}
	
	private void baseTest(String fixturesPath, String testName) {
		String examplePath = fixturesPath + testName + "/";
        
		IParserLogService log = new DefaultLogService();
		String code = "";
		try {
			code = File.readFileToString(examplePath + testName + ".cpp");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		CodeReader reader = new CodeReader(code.toCharArray());
		@SuppressWarnings("rawtypes")
		Map definedSymbols = new HashMap();
		String[] includePaths = new String[0];
		IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
		ICodeReaderFactory readerFactory = FileCodeReaderFactory.getInstance();

		IASTTranslationUnit translationUnit = null;
		try {
			translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(
					reader, info, readerFactory, null, log);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		GraphBuilder graph_builder = new GraphBuilder();
		new AstInterpreter(graph_builder, translationUnit);
		
		FileDriver fileDriver = new gvpl.graphviz.FileDriver();
		Visualizer visualizer = new Visualizer(fileDriver);
		fileDriver.print(graph_builder._gvplGraph, examplePath + "generated.dot", visualizer);

        Graph gvGraph = DotTree.getGraphFromDot(examplePath + testName + ".dot");
		assertTrue(GraphCompare.isEqual(graph_builder._gvplGraph, gvGraph));
	}
	
}
