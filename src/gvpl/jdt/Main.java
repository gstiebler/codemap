package gvpl.jdt;

import gvpl.Graph;
import gvpl.GraphBuilder;
import gvpl.common.*;

import java.io.IOException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;


 
public class Main {
	
	private String path;
	private String file;
	
	//use ASTParse to parse string
	public void parse(String str) {		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(str.toCharArray()); // set source
		parser.setResolveBindings(true); // we need bindings later on
		parser.setEnvironment(null, new String[]{  path }, null, false);
		parser.setUnitName(file);
			
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		
		ASTNode root = cu.getRoot();
		Visitor visitor = new Visitor(root);
		cu.accept(visitor);
 
		Graph gvpl_graph = new Graph();
		GraphBuilder graph_builder = new GraphBuilder(gvpl_graph);
		new AstInterpreter(graph_builder, visitor._root);
		
		new gvpl.graphviz.FileDriver(graph_builder._gvpl_graph, File.examplesPath() + "first.dot");
	}
 
	//loop directory to get file list
	public void ParseFilesInDir() throws IOException{
		parse(File.readFileToString(File.examplesPath() + "Main.java"));
	}
 
	public static void main(String[] args) throws IOException {
		(new Main()).ParseFilesInDir();
	}
	
	/*public static void inutil(String[] args) {
		int x = 0;
		int y = 3;
		x += y;
		int d;
		d = x * 5 + y * 3;
		boolean value = false;
		for (int i = 0; i < 4; i++)
		{
			int x2 = 5;
		}
	}*/
}