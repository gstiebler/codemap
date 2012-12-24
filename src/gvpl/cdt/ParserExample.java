package gvpl.cdt;

import gvpl.common.FileFuncs;
import gvpl.graph.Graph;
import gvpl.graphviz.FileDriver;
import gvpl.graphviz.Visualizer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
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

import java.io.Reader;

import org.mozilla.javascript.*;

public class ParserExample {

	public static void main(String[] args) throws Exception {
		{
			Context cx = Context.enter();
		    // Initialize the standard objects (Object, Function, etc.)
		    // This must be done before scripts can be executed. Returns
		    // a scope object that we use in later calls.
		    Scriptable scope = cx.initStandardObjects();
		    Object jsOut = Context.javaToJS(System.out, scope);
		    ScriptableObject.putProperty(scope, "out", jsOut);
		    ScriptableObject.putProperty(scope, "name", "JavaScript");
		
			String javaScriptExpression = "sayHello(name);";
			Reader javaScriptFile = new StringReader("function sayHello(name) {\n"
					+ "    out.println('Hello, '+name+'!');\n return sayHello;" + "}");
		    // Now evaluate the string we've collected.
			cx.evaluateReader(scope, javaScriptFile, "nada", 1, null);
		    Object result = cx.evaluateString(scope, javaScriptExpression, "nada", 1, null);
		    org.mozilla.javascript.Function func = (org.mozilla.javascript.Function)result;
		    Object[] funcArgs = {"teste string"};
			func.call(cx, scope, null, funcArgs);
		}
		
		IParserLogService log = new DefaultLogService();
		
		String code = FileFuncs.readFileToString(FileFuncs.examplesPath() + "main.cpp");

		CodeReader reader = new CodeReader(code.toCharArray());
		@SuppressWarnings("rawtypes")
		Map definedSymbols = new LinkedHashMap();
		String[] includePaths = new String[0];
		IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
		ICodeReaderFactory readerFactory = FileCodeReaderFactory.getInstance();

		IASTTranslationUnit translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(
				reader, info, readerFactory, null, log);

		Graph gvplGraph = new Graph();
		AstInterpreterCDT astInterpreter = new AstInterpreterCDT(gvplGraph);
		astInterpreter.loadDeclarations(translationUnit);

		FileDriver fileDriver = new gvpl.graphviz.FileDriver();
		Visualizer visualizer = new Visualizer(fileDriver);
		
		FileWriter outFile = null;
		try {
			outFile = new FileWriter(FileFuncs.examplesPath() + "first.dot");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		fileDriver.print(gvplGraph, outFile, visualizer);
	}
}
