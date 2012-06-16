package gvpl.cdt;

import gvpl.common.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.parser.scanner2.FileCodeReaderFactory;


public class ParserExample {
	
	
      public static void main(String[] args) throws Exception {
            IParserLogService log = new DefaultLogService();

    		String file = "main.cpp";
            String code = File.readFileToString(File.examplesPath() + file);
            
            CodeReader reader = new CodeReader(code.toCharArray());
            Map definedSymbols = new HashMap();
            String[] includePaths = new String[0];
            IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
            ICodeReaderFactory readerFactory = FileCodeReaderFactory.getInstance();

            IASTTranslationUnit translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(reader, info, readerFactory, null, log);

            translationUnit.getParent();
            Visitor visitor = new Visitor(translationUnit);

            visitor.shouldVisitNames = true;
            visitor.shouldVisitDeclarations = true;
            visitor.shouldVisitExpressions = true;
            visitor.shouldVisitProblems = true;
            visitor.shouldVisitStatements = true;
            
            translationUnit.accept(visitor);
      }
}