import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;


 
public class Main  extends ASTVisitor{
 
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		for (Iterator iter = node.fragments().iterator(); iter.hasNext();) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter
					.next();

			// VariableDeclarationFragment: is the plain variable declaration
			// part. Example:
			// "int x=0, y=0;" contains two VariableDeclarationFragments, "x=0"
			// and "y=0"

			IVariableBinding binding = fragment.resolveBinding();
			if(binding != null)
				System.out.println("sucesso");
			// first assignment is the initalizer
		}
		return false; // prevent that SimpleName is interpreted as
		// reference
	}
	
	public boolean visit(Assignment node) {
		if (node.getLeftHandSide() instanceof SimpleName) {
			IBinding binding = ((SimpleName) node.getLeftHandSide())
					.resolveBinding();

			if(binding != null)
				System.out.println("sucesso");
		}
		// prevent that simplename is interpreted as reference
		return false;
	}
	
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		SimpleName name = node.getName();
		
		if(node.resolveBinding() != null)
			System.out.println("funcionou");
		
		IBinding binding = name.resolveBinding();
		if(binding != null)
		{
			String temp = binding.getKey();
			System.out.println(temp);
		}
		
		
		/*this.names.add(name.getIdentifier());
		System.out.println("Declaration of '" + name + "' at line"
				+ cu.getLineNumber(name.getStartPosition()));*/
		return false; // do not continue 
	}
	
	@Override
	public boolean visit(SimpleName node) {
		
		IBinding binding = node.resolveBinding();
		if(binding != null)
		{
			String temp = binding.getKey();
			System.out.println(temp);
		}
		
		
		/*if (this.names.contains(node.getIdentifier())) {
			System.out.println("Usage of '" + node + "' at line "
					+ cu.getLineNumber(node.getStartPosition()));
		}*/
		return true;
	}
	
	//use ASTParse to parse string
	public static void parse(String str) {		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(str.toCharArray()); // set source
		parser.setResolveBindings(true); // we need bindings later on
		parser.setEnvironment(null, new String[]{  "K:\\Projetos\\GVPL\\JDT_test\\src" }, null, false);
		parser.setUnitName("Main.java");
		
	
		
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		 
		cu.accept(new Main());
 
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
	public static void ParseFilesInDir() throws IOException{
		File dirs = new File(".");
		String dirPath = dirs.getCanonicalPath() + File.separator+"src"+File.separator;
 
		File root = new File(dirPath);
		//System.out.println(rootDir.listFiles());
		File[] files = root.listFiles ( );
		String filePath = null;
 
		 for (File f : files ) {
			 filePath = f.getAbsolutePath();
			 if(f.isFile()){
				 parse(readFileToString(filePath));
			 }
		 }
	}
 
	public static void main(String[] args) throws IOException {
		ParseFilesInDir();
	}
}