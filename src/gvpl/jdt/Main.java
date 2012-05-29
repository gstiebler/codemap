package gvpl.jdt;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.jdt.core.dom.AST;
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
		cu.accept(new Visitor());
 
	}
 
	//read file content into a string
	public static String readFileToString(String filePath) throws IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
 
		char[] buf = new char[10];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			System.out.println(numRead);
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
 
		reader.close();
 
		return  fileData.toString();	
	}
 
	//loop directory to get file list
	public void ParseFilesInDir() throws IOException{
		path = "K:\\Projetos\\GVPL\\exemplos\\";
		file = "Main.java";
		parse(readFileToString(path + file));
	}
 
	public static void main(String[] args) throws IOException {
		(new Main()).ParseFilesInDir();
	}
	
	public static void inutil(String[] args) {
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
	}
}