package tests;

import static org.junit.Assert.assertTrue;
import gvpl.cdt.AstInterpreterCDT;
import gvpl.common.CodeLocation;
import gvpl.common.FileFuncs;
import gvpl.graph.GraphNode;
import gvpl.graphviz.FileDriver;
import gvpl.graphviz.Visualizer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import debug.DebugOptions;

public class TestsUtil {
	
	static Logger logger = LogManager.getLogger(TestsUtil.class.getName());
	
	public static void baseTest(String testName) {
		DebugOptions.resetLines();
		String fixturesPath = System.getProperty("user.dir") + "/fixtures/";
		String examplePath = fixturesPath + testName + "/";
		GraphNode.resetCounter();
		FileDriver.resetCounter();

		IParserLogService log = new DefaultLogService();

		Map<String, String> definedSymbols = new LinkedHashMap<String, String>();

		List<String> readIncludePaths = null;
		try {
			readIncludePaths = FileFuncs.readLines(new java.io.File(examplePath + "includes.txt"));
		} catch (Exception e1) {
			readIncludePaths = null;
		}

		String[] includePaths = null;
		if (readIncludePaths == null) {
			includePaths = new String[0];
			includePaths[0] = examplePath;
		} else {
			for (int i = 0; i < readIncludePaths.size(); i++)
				readIncludePaths.set(i, examplePath + readIncludePaths.get(i));
			includePaths = new String[readIncludePaths.size() + 1];
			readIncludePaths.add(examplePath);
			readIncludePaths.toArray(includePaths);
		}

		IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
		ICodeReaderFactory readerFactory = FileCodeReaderFactory.getInstance();

		AstInterpreterCDT astInterpreter = new AstInterpreterCDT(new gvpl.graph.Graph());
		
		List<String> fileNames = new ArrayList<String>();
		String mainTestFileName = examplePath + testName + ".cpp";
		
		List<String> readBuildFiles = null;
		try {
			readBuildFiles = FileFuncs.readLines(new java.io.File(examplePath + "files.txt"));
		} catch (Exception e1) {
			readBuildFiles = null;
		}
		
		if(readBuildFiles != null){
			for (String buildFile : readBuildFiles)
				fileNames.add(examplePath + buildFile);
		}
		
		fileNames.add(mainTestFileName);
		
		List<IASTTranslationUnit> translationUnits = new ArrayList<IASTTranslationUnit>();
		
		for(String fileName : fileNames)
		{
			logger.debug(" -- ** Processing cpp: {}", fileName);
			CodeLocation.setCurrentFileName(fileName);
			String code = "";
			try {
				code = FileFuncs.readFileToString(fileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			CodeReader reader = new CodeReader(code.toCharArray());
			IASTTranslationUnit translationUnit = null;
			try {
				translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(reader, info,
						readerFactory, null, log);
				translationUnits.add(translationUnit);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for(int i = 0; i < translationUnits.size(); ++i) {
			DebugOptions.setCurrCpp(fileNames.get(i));
			logger.debug(" -*- Loading declarations {}", fileNames.get(i));
			astInterpreter.loadDeclarations(translationUnits.get(i));
		}
		
		astInterpreter.loadDefinitions();

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
