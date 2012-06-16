package gvpl.cdt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.parser.scanner2.FileCodeReaderFactory;


public class ParserExample {
	
	
      public static void main(String[] args) throws Exception {
            IParserLogService log = new DefaultLogService();

            String code = "class Class { " +
            		" public: int x,y;" +
            		" Class(); " +
            		"~Class(); " +
            		"private: Class f(); " +
            		"}; " +
            		"int function(double parameter) { int x2 = 5; int y2 = 3 + x2; return parameter; };";
            CodeReader reader = new CodeReader(code.toCharArray());
            Map definedSymbols = new HashMap();
            String[] includePaths = new String[0];
            IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
            ICodeReaderFactory readerFactory = FileCodeReaderFactory.getInstance();

            IASTTranslationUnit translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(reader, info, readerFactory, null, log);

            translationUnit.getParent();
            Visitor visitor = new Visitor((ASTNode) translationUnit.getParent());

            visitor.shouldVisitNames = true;
            visitor.shouldVisitDeclarations = true;
            visitor.shouldVisitExpressions = true;
            visitor.shouldVisitProblems = true;
            visitor.shouldVisitStatements = true;
            
            translationUnit.accept(visitor);
      }
}