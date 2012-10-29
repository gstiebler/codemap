package gvpl.cdt;

import gvpl.common.File;
import gvpl.graph.Graph;
import gvpl.graphviz.FileDriver;
import gvpl.graphviz.Visualizer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.parser.scanner2.FileCodeReaderFactory;

public class ParserExample {

	public static void main(String[] args) throws Exception {
		IParserLogService log = new DefaultLogService();
		
		String code = File.readFileToString(File.examplesPath() + "main.cpp");

		CodeReader reader = new CodeReader(code.toCharArray());
		@SuppressWarnings("rawtypes")
		Map definedSymbols = new LinkedHashMap();
		String[] includePaths = new String[0];
		IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
		ICodeReaderFactory readerFactory = FileCodeReaderFactory.getInstance();

		IASTTranslationUnit translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(
				reader, info, readerFactory, null, log);

		Graph gvplGraph = new Graph(-1);
		AstInterpreterCDT astInterpreter = new AstInterpreterCDT(gvplGraph);
		astInterpreter.execute(translationUnit);

		FileDriver fileDriver = new gvpl.graphviz.FileDriver();
		Visualizer visualizer = new Visualizer(fileDriver);
		
		FileWriter outFile = null;
		try {
			outFile = new FileWriter(File.examplesPath() + "first.dot");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		fileDriver.print(gvplGraph, outFile, visualizer);
	}
}
