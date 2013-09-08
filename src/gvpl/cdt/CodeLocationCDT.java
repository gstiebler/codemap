package gvpl.cdt;

import gvpl.common.CodeLocation;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;

import debug.DebugOptions;

public class CodeLocationCDT {

	static public CodeLocation NewFromFileLocation(IASTNode node) {
		if(node instanceof CPPASTTranslationUnit)
			return null;
		IASTFileLocation fileLocation = node.getFileLocation();
		String fileName = fileLocation.getFileName();
		if(fileName.equals("<text>")) {
			IASTNode parent = node.getParent();
			if(parent != null) {
				CodeLocation codeLoc = NewFromFileLocation(parent);
				if(codeLoc != null)
					return codeLoc;
			}
		}
	
		int startingLine = fileLocation.getStartingLineNumber();
		int offset = fileLocation.getNodeOffset();
		CodeLocation codeLoc = new CodeLocation(fileName, startingLine, offset);
		DebugOptions.setCurrCodeLocation(codeLoc);
		return codeLoc;
	}
	
}
