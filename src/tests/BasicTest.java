package tests;

import static org.junit.Assert.assertTrue;
import gvpl.cdt.AstInterpreter;
import gvpl.common.File;
import gvpl.graph.GraphBuilder;

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
		String examplePath = "K:/Projetos/GVPL/src/tests/fixtures/basic/";
        Graph gvGraph = DotTree.getGraphFromDot(examplePath + "basic.dot");
        
		IParserLogService log = new DefaultLogService();
		String code = "";
		try {
			code = File.readFileToString(examplePath + "basic.cpp");
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
		
		assertTrue(GraphCompare.isEqual(graph_builder._gvplGraph, gvGraph));
	}
	
}
