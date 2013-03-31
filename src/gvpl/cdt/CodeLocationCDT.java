package gvpl.cdt;

import gvpl.common.CodeLocation;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;

import debug.DebugOptions;

public class CodeLocationCDT {

	static public CodeLocation NewFromFileLocation(IASTFileLocation fileLocation) {
		String fileName = fileLocation.getFileName();
		int startingLine = fileLocation.getStartingLineNumber();
		int offset = fileLocation.getNodeOffset();
		CodeLocation codeLoc = new CodeLocation(fileName, startingLine, offset);
		DebugOptions.setCurrCodeLocation(codeLoc);
		return codeLoc;
	}
	
}
