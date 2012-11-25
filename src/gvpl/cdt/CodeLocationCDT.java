package gvpl.cdt;

import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;

import gvpl.common.CodeLocation;

public class CodeLocationCDT {

	static public CodeLocation NewFromDecl(CPPASTCompositeTypeSpecifier decl) {
		String fileName = decl.getFileLocation().getFileName();
		int startingLine = decl.getFileLocation().getStartingLineNumber();
		return new CodeLocation(fileName, startingLine);
	}

	static public CodeLocation NewFromDecl(CPPASTFunctionDeclarator decl) {
		String fileName = decl.getFileLocation().getFileName();
		int startingLine = decl.getFileLocation().getStartingLineNumber();
		return new CodeLocation(fileName, startingLine);
	}

	static public CodeLocation NewFromDecl(IASTStatement decl) {
		String fileName = decl.getFileLocation().getFileName();
		int startingLine = decl.getFileLocation().getStartingLineNumber();
		return new CodeLocation(fileName, startingLine);
	}
	
}
