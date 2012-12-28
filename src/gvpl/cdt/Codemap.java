package gvpl.cdt;

import gvpl.common.CodeLocation;
import gvpl.common.FileFuncs;
import gvpl.common.ScriptManager;
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

public class Codemap {

	static Logger logger = LogManager.getLogger(Codemap.class.getName());
	
	public static void main(String[] args) {
		String basePath = args[0];
		String mainFile = basePath + "/" + args[1];
		execute(basePath, mainFile);
	}
	
	public static AstInterpreterCDT execute(String basePath, String mainFile) {
		DebugOptions.resetLines();
		GraphNode.resetCounter();
		FileDriver.resetCounter();

		IParserLogService log = new DefaultLogService();
		List<String> readIncludePaths = null;
		String includesFilePath = basePath + "/includes.txt";
		try {
			readIncludePaths = FileFuncs.readLines(new java.io.File(includesFilePath));
		} catch (Exception e1) {
			readIncludePaths = null;
		}

		String[] includePaths = null;
		if (readIncludePaths == null) {
			includePaths = new String[0];
			includePaths[0] = basePath;
		} else {
			for (int i = 0; i < readIncludePaths.size(); i++)
				readIncludePaths.set(i, basePath + readIncludePaths.get(i));
			includePaths = new String[readIncludePaths.size() + 1];
			readIncludePaths.add(basePath);
			readIncludePaths.toArray(includePaths);
		}

		Map<String, String> definedSymbols = new LinkedHashMap<String, String>();
		IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
		ICodeReaderFactory readerFactory = FileCodeReaderFactory.getInstance();

		AstInterpreterCDT astInterpreter = new AstInterpreterCDT(new gvpl.graph.Graph());
		ScriptManager sm = new ScriptManager(basePath, astInterpreter);
		astInterpreter.setScriptManager(sm);
		sm.execMainScript();
		
		List<String> fileNames = new ArrayList<String>();
		
		List<String> readBuildFiles = null;
		try {
			readBuildFiles = FileFuncs.readLines(new java.io.File(basePath + "files.txt"));
		} catch (Exception e1) {
			readBuildFiles = null;
		}
		
		if(readBuildFiles != null){
			for (String buildFile : readBuildFiles)
				fileNames.add(basePath + buildFile);
		}
		
		fileNames.add(mainFile);
		
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
			outFile = new FileWriter(basePath + "generated.dot");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		fileDriver.print(astInterpreter.getGraph(), outFile, visualizer);
		return astInterpreter;
	}
}
