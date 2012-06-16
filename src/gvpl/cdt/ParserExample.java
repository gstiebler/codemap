package gvpl.cdt;

import gvpl.Graph;
import gvpl.GraphBuilder;
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

            String code = File.readFileToString(File.examplesPath() + "main.cpp");
            
            CodeReader reader = new CodeReader(code.toCharArray());
            @SuppressWarnings("rawtypes")
			Map definedSymbols = new HashMap();
            String[] includePaths = new String[0];
            IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
            ICodeReaderFactory readerFactory = FileCodeReaderFactory.getInstance();

            IASTTranslationUnit translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(reader, info, readerFactory, null, log);
            Visitor visitor = new Visitor(translationUnit);

            visitor.shouldVisitNames = true;
            visitor.shouldVisitDeclarations = true;
            visitor.shouldVisitDeclarators = true;
            visitor.shouldVisitDeclSpecifiers = true;
            visitor.shouldVisitExpressions = true;
            visitor.shouldVisitInitializers = true;
            visitor.shouldVisitProblems = true;
            visitor.shouldVisitStatements = true;
            visitor.shouldVisitTypeIds = true;
            
            translationUnit.accept(visitor);
            
    		Graph gvpl_graph = new Graph();
    		GraphBuilder graph_builder = new GraphBuilder(gvpl_graph);
    		new AstInterpreter(graph_builder, visitor._root);
    		
    		new gvpl.graphviz.FileDriver(graph_builder._gvpl_graph, File.examplesPath() + "first.dot");
      }
}