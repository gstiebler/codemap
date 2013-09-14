package gvpl.cdt;

import gvpl.common.CodeLocation;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import debug.DebugOptions;

public class CodeLocationCDT {

	static public CodeLocation NewFromFileLocation(IASTNode node) {
		IASTFileLocation fileLocation = node.getFileLocation();
		String fileName = fileLocation.getFileName();
		int startingLine = fileLocation.getStartingLineNumber();
		int offset = fileLocation.getNodeOffset();
		CodeLocation codeLoc = new CodeLocation(fileName, startingLine, offset);
		DebugOptions.setCurrCodeLocation(codeLoc);
		return codeLoc;
	}
	
}
