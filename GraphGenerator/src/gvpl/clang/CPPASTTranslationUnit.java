package gvpl.clang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.ParserLanguage;

class BindingInfo {
	public int bindingId;
	public String location;
	public String name;
	public String type;
}

public class CPPASTTranslationUnit implements IASTTranslationUnit {

	static Logger logger = LogManager.getLogger(CPPASTTranslationUnit.class.getName());
	
	public List<IASTDeclaration> _declarations = new ArrayList<IASTDeclaration>();
	private Map<Integer, IBinding> _bindings = new TreeMap<Integer, IBinding>();
	static CPPASTTranslationUnit _instance;

	public CPPASTTranslationUnit(String path, String fileName) {
		_instance = this;
		String astFileName = fileName.substring(0, fileName.length() - 4) + ".ast";
		Cursor cursor = new Cursor(astFileName);
		cursor.nextLine();
		cursor.nextLine();
		cursor.nextLine();
		while (!cursor.theEnd()) {
			String line = cursor.getLine();
			String type = getType(line);
			if (type.equals("FunctionDecl")) {
				_declarations.add(new CPPASTFunctionDeclaration(cursor.getSubCursor(), false));
			} else if (type.equals("CXXRecordDecl")) {
				_declarations.add(new ASTSimpleDeclaration(cursor.getSubCursor()));
			} else {
				logger.error("Not prepared for string " + line);
				cursor.nextLine();
			}
		}
	}

	public static String getType(String line) {
		String temp1 = line.split("-")[1];
		String temp2 = temp1.split(" ")[0];
		return temp2;
	}
	
	public static List<String> parseLine(String line) {
		List<String> result = new ArrayList<String>();

		String[] dash = line.split("-");
		String[] space = dash[1].split(" ");
		result.add(space[0]);
		
		String[] postX = space[1].split("0x");
		String bindText = postX[1].split(" ")[0];
		result.add(bindText);
		
		String[] postBico1 = dash[1].split("<");
		String[] postBico2 = postBico1[1].split(">");
		result.add(postBico2[0]);
		
		String[] plic = postBico2[1].split("'");
		for(String strPlic : plic) {
			if(strPlic.substring(0, 1).equals(" ")) {
				String[] strings = strPlic.split(" ");
				for(int i = 1; i < strings.length; ++i)
					result.add(strings[i]);
			} else {
				result.add(strPlic);
			}
		}
		
		return result;
	}
	
	public static BindingInfo parseBindingInfo(String line) {
		BindingInfo result = new BindingInfo();
		List<String> parsedLine = parseLine(line);
		result.bindingId = Integer.parseInt(parsedLine.get(1), 16);
		result.location = parsedLine.get(3);
		result.name = parsedLine.get(4);	
		result.type = parsedLine.get(5);
		return result;
	}

	@Override
	public boolean accept(ASTVisitor arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(IASTNode arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getContainingFilename() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTFileLocation getFileLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTNodeLocation[] getNodeLocations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTNode getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ASTNodeProperty getPropertyInParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRawSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTTranslationUnit getTranslationUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParent(IASTNode arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPropertyInParent(ASTNodeProperty arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDeclaration(IASTDeclaration arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public IASTFileLocation flattenLocationsToFile(IASTNodeLocation[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTPreprocessorStatement[] getAllPreprocessorStatements() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTPreprocessorMacroDefinition[] getBuiltinMacroDefinitions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTComment[] getComments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContainingFilename(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTDeclaration[] getDeclarations() {
		IASTDeclaration[] decls = new IASTDeclaration[_declarations.size()];
		return _declarations.toArray(decls);
	}

	@Override
	public IName[] getDeclarations(IBinding arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTName[] getDeclarationsInAST(IBinding arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IName[] getDefinitions(IBinding arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTName[] getDefinitionsInAST(IBinding arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDependencyTree getDependencyTree() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFilePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTPreprocessorIncludeStatement[] getIncludeDirectives() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IIndex getIndex() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTNodeLocation[] getLocationInfo(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTPreprocessorMacroDefinition[] getMacroDefinitions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParserLanguage getParserLanguage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTProblem[] getPreprocessorProblems() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTName[] getReferences(IBinding arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IScope getScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUnpreprocessedSignature(IASTNodeLocation[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTNode selectNodeForLocation(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setComments(IASTComment[] arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIndex(IIndex arg0) {
		// TODO Auto-generated method stub

	}

	public static void addBinding(int bindingId, IBinding binding) {
		_instance._bindings.put(bindingId, binding);
	}
	
	public static IBinding getBinding(int bindingId) {
		return _instance._bindings.get(bindingId);
	}

}
