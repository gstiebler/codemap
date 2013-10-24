package gvpl.cdt;

import gvpl.common.CodeLocation;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class CodeLocationCDT {

	static public CodeLocation NewFromFileLocation(IASTNode node) {
		IASTFileLocation fileLocation = node.getFileLocation();
		if( fileLocation == null ) 
			return new CodeLocation("default", -1, -1);

		String fileName = fileLocation.getFileName();
		int startingLine = fileLocation.getStartingLineNumber();
		int offset = fileLocation.getNodeOffset();
		CodeLocation codeLoc = new CodeLocation(fileName, startingLine, offset);
		return codeLoc;
	}
	
}
