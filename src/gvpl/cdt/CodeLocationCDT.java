package gvpl.cdt;

import gvpl.common.CodeLocation;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;

public class CodeLocationCDT {

	static public CodeLocation NewFromFileLocation(IASTFileLocation fileLocation) {
		String fileName = fileLocation.getFileName();
		int startingLine = fileLocation.getStartingLineNumber();
		return new CodeLocation(fileName, startingLine);
	}
	
}
