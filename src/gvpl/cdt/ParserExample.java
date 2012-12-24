package gvpl.cdt;

import gvpl.common.FileFuncs;
import gvpl.common.ScriptManager;
import gvpl.graph.Graph;
import gvpl.graphviz.FileDriver;
import gvpl.graphviz.Visualizer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
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
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

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
		    
//		    ScriptableObject.putProperty(scope, "name", "JavaScript");
//		
//			String javaScriptExpression = "sayHello(name);";
//			Reader javaScriptFile = new StringReader("function sayHello(name) {\n"
//					+ "    out.println('Hello, '+name+'!');\n return sayHello;" + "}");
//		    // Now evaluate the string we've collected.
//			cx.evaluateReader(scope, javaScriptFile, "nada", 1, null);
//		    Object result = cx.evaluateString(scope, javaScriptExpression, "nada", 1, null);
//		    org.mozilla.javascript.Function func = (org.mozilla.javascript.Function)result;
//		    Object[] funcArgs = {"teste string"};
//			func.call(cx, scope, null, funcArgs);
			
			
			String basePath = System.getProperty("user.dir") + "\\fixtures\\events\\";
			String fileName = "main.js";
			Reader javaScriptFile = new FileReader(basePath + fileName); 
			cx.evaluateReader(scope, javaScriptFile, fileName, 1, null);
			Function fct = (Function)scope.get("main", scope);
			ScriptManager sm = new ScriptManager();
			Object[] funcArgs = {sm};
			fct.call(cx, scope, null, funcArgs);
			int x = 5;
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
