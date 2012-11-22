package tests;

import static org.junit.Assert.assertTrue;
import gvpl.cdt.AstInterpreterCDT;
import gvpl.common.File;
import gvpl.graph.GraphNode;
import gvpl.graphviz.FileDriver;
import gvpl.graphviz.Visualizer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
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

public class TestsUtil {

	public static void baseTest(String testName) {
		String fixturesPath = System.getProperty("user.dir") + "/fixtures/";
		String examplePath = fixturesPath + testName + "/";
		GraphNode.resetCounter();
		FileDriver.resetCounter();

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
		Map definedSymbols = new LinkedHashMap();
		String[] includePaths = new String[1];
		includePaths[0] = examplePath;
		IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
		ICodeReaderFactory readerFactory = FileCodeReaderFactory.getInstance();

		IASTTranslationUnit translationUnit = null;
		try {
			translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(reader, info,
					readerFactory, null, log);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		AstInterpreterCDT astInterpreter = new AstInterpreterCDT(new gvpl.graph.Graph());
		astInterpreter.execute(translationUnit);

		FileDriver fileDriver = new gvpl.graphviz.FileDriver();
		Visualizer visualizer = new Visualizer(fileDriver);

		FileWriter outFile = null;
		try {
			outFile = new FileWriter(examplePath + "generated.dot");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		fileDriver.print(astInterpreter.getGraph(), outFile, visualizer);

		Graph gvGraph = DotTree.getGraphFromDot(examplePath + testName + ".dot");
		assertTrue(GraphCompare.isEqual(astInterpreter.getGraph(), gvGraph));
	}
}
