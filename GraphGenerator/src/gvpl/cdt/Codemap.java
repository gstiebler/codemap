package gvpl.cdt;

import gvpl.clang.CPPASTTranslationUnit;
import gvpl.common.CodeLocation;
import gvpl.common.FileFuncs;
import gvpl.common.OutputManager;
import gvpl.common.ScopeManager;
import gvpl.common.ScriptManager;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;
import gvpl.graphviz.FileDriver;
import gvpl.graphviz.Visualizer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import debug.DebugOptions;
import debug.ExecTreeLogger;

public class Codemap {

	static Logger logger = LogManager.getLogger(Codemap.class.getName());
	
	public static void main(String[] args) {
		String basePath = args[0] + "/";
		String mainFile = basePath + args[1];
		long first = System.currentTimeMillis();
		execute(basePath, mainFile);
		long last = System.currentTimeMillis();
		long dif = last - first;
		System.out.println("Terminou de executar. " + Long.toString(dif));
	}
	
	public static AstInterpreterCDT execute(String basePath, String mainFile) {
		ScopeManager.reset();
		DebugOptions.resetLines();
		GraphNode.resetCounter();
		FileDriver.resetCounter();

		List<String> readIncludePaths = null;
		Set<String> allIncludePaths = new LinkedHashSet<String>();
		String includesFilePath = basePath + "includes.txt";
		try {
			readIncludePaths = FileFuncs.readLines(new java.io.File(includesFilePath));
		} catch (Exception e1) {
			logger.error("Cannot read {}", includesFilePath);
			readIncludePaths = null;
		}

		String[] includePaths = null;
		if (readIncludePaths == null) {
			includePaths = new String[0];
			includePaths[0] = basePath;
		} else {
			for (int i = 0; i < readIncludePaths.size(); i++) {
				String completePath = basePath + readIncludePaths.get(i);
				allIncludePaths.add(completePath);
				List<String> subFolders = addSubFolders(completePath);
				allIncludePaths.addAll(subFolders);
			}
			allIncludePaths.add(basePath);
			includePaths = new String[allIncludePaths.size()];
			allIncludePaths.toArray(includePaths);
		}

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
		
		OutputManager.setInstance();
		OutputManager.getInstance().setSrcFiles(fileNames);
		
		List<IASTTranslationUnit> translationUnits = new ArrayList<IASTTranslationUnit>();
		
		for(String fileName : fileNames)
		{
			logger.debug(" -- ** Processing cpp: {}", fileName);
			CPPASTTranslationUnit clangTranslationUnit = new CPPASTTranslationUnit(basePath, fileName);
			translationUnits.add(clangTranslationUnit);
		}
		
		for(int i = 0; i < translationUnits.size(); ++i) {
			DebugOptions.setCurrCpp(fileNames.get(i));
			CodeLocation.setCurrentFileName(fileNames.get(i));
			logger.debug(" -*- Loading declarations {}", fileNames.get(i));
			System.out.println("Processing TU from " + fileNames.get(i));
			astInterpreter.loadDeclarations(translationUnits.get(i));
		}
		
		astInterpreter.loadMain();
		
		if(ScopeManager.getScopeList().size() > 0)
			logger.error("Scope list is not empty");	

		FileDriver fileDriver = new gvpl.graphviz.FileDriver();
		Visualizer visualizer = new Visualizer(fileDriver);

		FileWriter outFile = null;
		try {
			outFile = new FileWriter(basePath + "generated.dot");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Graph mainGraph = astInterpreter.getGraph();
		mainGraph.saveToFile(basePath + "graph.ser");	
		
		OutputManager.getInstance().saveToFile(basePath + "output.ser");

		fileDriver.print(mainGraph, outFile, visualizer);
		
		ExecTreeLogger.finish();
		
		return astInterpreter;
	}
	
	private static List<String> addSubFolders(String folderPath) {
		List<String> subFolders = new ArrayList<String>();
		
		File dir = new File(folderPath);
		String[] children = dir.list();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				if(children[i].compareTo(".svn") == 0)
					continue;
				String completePath = folderPath + "/" + children[i];
				File subDir = new File(completePath);
				if(!subDir.isDirectory() || subDir.isHidden())
					continue;
				subFolders.add(completePath + "/");
				subFolders.addAll(addSubFolders(completePath));
			}
		}
		
		return subFolders;
	}
}
