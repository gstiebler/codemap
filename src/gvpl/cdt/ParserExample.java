package gvpl.cdt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.core.resources.*;
import org.eclipse.cdt.internal.core.parser.scanner2.FileCodeReaderFactory;


public class ParserExample {
	
	
      public static void main(String[] args) throws Exception {
            IParserLogService log = new DefaultLogService();

            String code = "class Class { public: int x,y; Class(); ~Class(); private: Class f(); }; int function(double parameter) { return parameter; };";
            CodeReader reader = new CodeReader(code.toCharArray());
            Map definedSymbols = new HashMap();
            String[] includePaths = new String[0];
            IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
            ICodeReaderFactory readerFactory = FileCodeReaderFactory.getInstance();

            IASTTranslationUnit translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(reader, info, readerFactory, null, log);
            
            ASTVisitor visitor = new ASTVisitor() {
                  public int visit(IASTName name) {
                        System.out.println(name.getRawSignature());
                        return ASTVisitor.PROCESS_CONTINUE;
                  }
            };
            visitor.shouldVisitNames = true;
            translationUnit.accept(visitor);
      }
}