package gvpl.clang;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
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
	public int bindingId = -1;
	public String location = "";
	public String name = "";
	public String type = "";
}

class BindingOwner {
	int originalBindingId;
	Object owner;
	
	BindingOwner(int originalBindingId, Object owner) {
		this.originalBindingId = originalBindingId;
		this.owner = owner;
	}
}

class ClangLine {
	Map<String, List<String> > _values = new TreeMap<String, List<String> >();
	
	public static List<String> initList(String firstElement) {
		List<String> result = new ArrayList<String>();
		result.add(firstElement);
		return result;
	}
	
	public void put(String key, String value) {
		if(_values.containsKey(key)) {
			_values.get(key).add(value);
		} else {
			_values.put(key, initList(value));
		}
	}
	
	public String get(String key) {
		return _values.get(key).get(0);
	}
	
	public String getAndCheck(String key) {
		if(_values.containsKey(key)) {
			return get(key);
		} else {
			return "";
		}
	}
	
	public String get(String key, int index) {
		return _values.get(key).get(index);
	}
	
	public Set<String> getSet(String key) {
		Set<String> result = new HashSet<String>();
		if(_values.containsKey(key)) {
			List<String> list = _values.get(key);
			for(String listItem : list) {
				result.add(listItem);
			}
		} 
		return result;
	}
	
	public boolean containsKey(String key) {
		return _values.containsKey(key);
	}
	
	@Override
	public String toString() {
		return _values.toString();
	}
}

public class CPPASTTranslationUnit implements IASTTranslationUnit {

	static Logger logger = LogManager.getLogger(CPPASTTranslationUnit.class.getName());
	
	public List<IASTDeclaration> _declarations = new ArrayList<IASTDeclaration>();
	private Map<Integer, IBinding> _idToBinding = new TreeMap<Integer, IBinding>();
	private Map<String, IBinding> _typeNameToBinding = new TreeMap<String, IBinding>();
	private Map<String, CPPConstructor> _constructorsBinding = new TreeMap<String, CPPConstructor>();
	Deque<String> _namespaces = new ArrayDeque<String>();
	static CPPASTTranslationUnit _instance;
	static public IASTName lastClassName;
	static String _fileName;
	
	private List<BindingOwner> _bindingOwners = new ArrayList<BindingOwner>();
	public Map<Integer, IBinding> _bindingSynonyms = new TreeMap<Integer, IBinding>();

	public CPPASTTranslationUnit(String path, String fileName) {
		_instance = this;
		String astFileName = fileName.substring(0, fileName.length() - 4) + ".ast";
		Cursor cursor = new Cursor(astFileName);
		cursor.nextLine();
		cursor.nextLine();
		cursor.nextLine();
		while (!cursor.theEnd()) {
			IASTDeclaration decl = loadDeclaration(cursor, null);
			if(decl != null)
				_declarations.add(decl);
		}
		fixBindingSynonyms();
	}
	
	static IASTDeclaration loadDeclaration(Cursor cursor, ASTNode parent) {
		String line = cursor.getLine();
		if(line.contains("<invalid sloc>")) {
			cursor.runToTheEnd();
			return null;
		}
		
		String type = getType(line);
		if (type.equals("FunctionDecl")) {
			return loadFuncDecl(cursor, false, null);
		} else if (type.equals("CXXMethodDecl") || type.equals("CXXConstructorDecl")) {
			ClangLine strings = CPPASTTranslationUnit.lineToMap(line);
			int parentId = hexStrToInt(strings.get("parent"));
			int prevId = hexStrToInt(strings.get("prev"));
			IBinding binding = getBinding(prevId);
			if(binding == null)
				logger.error("Prev Id {} not found", prevId);

			IASTDeclaration funcDecl = loadFuncDecl(cursor, true, null);
			
			if(funcDecl instanceof CPPASTFunctionDefinition) {
				CPPClassType ct = (CPPClassType) getBinding(parentId);
				ct._parent.replaceFuncDecl(binding, (CPPASTFunctionDefinition)funcDecl);
			}
					
			return funcDecl;
		} else if (type.equals("CXXRecordDecl") || 
				type.equals("VarDecl") || 
				type.equals("EnumDecl")) {
			return new CPPASTSimpleDeclaration(cursor.getSubCursor(), parent);
		} else if (type.equals("NamespaceDecl")) {
			return new CPPASTNamespaceDefinition(cursor.getSubCursor(), parent);
		} else if (type.equals("TypedefDecl")) {
			ClangLine parsedLine = CPPASTTranslationUnit.lineToMap(line);
			String userType = CPPASTTranslationUnit.getUserType(parsedLine);
			if(userType.contains("("))
			{
				IBinding binding = new CPPTypedef(cursor.getSubCursor());
				IASTDeclarator funcDecl = new CPPASTFunctionDeclarator(binding, parent, cursor.getSubCursor());
				IASTDeclSpecifier declSpec = new CPPASTSimpleDeclSpecifier(cursor.getSubCursor(), parent);
				return new CPPASTSimpleDeclaration(line, parent, declSpec, funcDecl);
			} else
				cursor.runToTheEnd();
			return null;
		} else if (type.equals("EmptyDecl") || 
				type.equals("UsingDecl") || 
				type.equals("UsingDirectiveDecl") || 
				type.equals("UsingShadowDecl") || 
				type.equals("TypedefDecl")) {
			cursor.runToTheEnd();
			return null;
		} else if (type.equals("ClassTemplateDecl")) {
			return new CPPASTTemplateDeclaration(cursor.getSubCursor(), parent);
		} else if (type.equals("ClassTemplateSpecializationDecl")) {
			return new CPPASTTemplateSpecialization(cursor.getSubCursor(), parent);
		} else {
			logger.error("Not prepared for type {}, line {}", type, cursor.getPos());
			cursor.runToTheEnd();
			return null;
		}
	}
	
	static IASTDeclaration loadFuncDecl(Cursor cursor, boolean isMethod, ASTNode parent) {
		String line = cursor.getLine();
		CPPASTFunctionDefinition funcDefinition = new CPPASTFunctionDefinition(cursor.getSubCursor(), isMethod, parent);
		ClangLine parsedLine = lineToMap(line);
		addBindingSynonymIfNecessary(parsedLine, funcDefinition._binding);
		if(funcDefinition._body != null) {
			return funcDefinition;
		} else {
			return new CPPASTSimpleDeclaration(line, parent, null, funcDefinition._declarator);
		}
	}
	
	public static void addBindingSynonymIfNecessary(ClangLine parsedLine, IBinding binding) {
		// has previous binding
		if(parsedLine.containsKey("prev")) {
			int oldId = hexStrToInt( parsedLine.get("prev") );
			_instance._bindingSynonyms.put(oldId, binding);
		}
	}

	public static String getType(String line) {
		String temp1 = line.split("-")[1];
		String temp2 = temp1.split(" ")[0];
		return temp2;
	}
	
	public static String simplifyType(String line) {
		String result = line.replace("class", "")
				.replace("struct", "")
				.replace("enum", "")
				.replace("*", "")
				.replace("&", "").trim();
		return result.split(" ")[0];
	}
	
	public static int hexStrToInt(String hexStr) {
		String[] strings = hexStr.split("0x");
		if(strings.length != 2)
			return -1;
		if(!strings[0].equals(""))
			return -1;
		
		String bindingStr = strings[1];
		try {
			return Integer.parseInt(bindingStr, 16);
		} catch(NumberFormatException e) {
			return -1;
		}
	}
	
	public static String[] breakIn2(String line, char splitter) {
		String[] result = new String[2];
		int pos = line.indexOf(splitter);
		if(pos >= 0) {
			result[0] = line.substring(0, pos);
			result[1] = line.substring(pos + 1, line.length());
		} else {
			result[0] = line;
			result[1] = "";
		}
		
		return result;
	}
	
	public static ClangLine lineToMap(String line) {
		ClangLine result = new ClangLine();
		{
			String[] linesDivComma = breakIn2(line, '-');
			linesDivComma = breakIn2(linesDivComma[1], ' ');
			String mainType = linesDivComma[0];
			result.put("mainType", mainType);
			line = linesDivComma[1];
		}
		while(true) {
			String key = "";
			String afterComma = "";
			{
				String[] linesDivCommaInside = breakIn2(line, ':');
				key = linesDivCommaInside[0];
				afterComma = linesDivCommaInside[1];
			}
			{
				String[] parsed = key.split(" ");
				for(int i = parsed.length - 1; i >= 0; --i) {
					if(parsed[i].length() > 0) {
						key = parsed[i];
						break;
					}
				}
			}
			if(afterComma.length() == 0) {
				line = afterComma;
				if(line.length() == 0)
					break;
				continue;
			}
			String value = "";
			String remainingLine = "";
			// at this point, lines[0] is what's before ':', lines[1] have what's left
			char firstChar = afterComma.charAt(0);
			remainingLine = afterComma;
			while(true) {
				if(firstChar == '\'') {
					String excludeFirstchar = remainingLine.substring(1);
					String[] lines = breakIn2(excludeFirstchar, '\'');
					value = value + lines[0];
					remainingLine = lines[1];
					if(remainingLine.length() > 0)
						firstChar = remainingLine.charAt(0);
					else
						break;
				} else if(firstChar == ':') {
					String excludeFirstchar = remainingLine.substring(1);
					remainingLine = excludeFirstchar;
					firstChar = remainingLine.charAt(0);
					value = value + "%";
				} else {
					String[] lines = breakIn2(remainingLine, ' ');
					value = value + lines[0];
					remainingLine = lines[1];
					break;
				}
			}
			// at this point, lines[0] could be whatever, but lines[1] should be the rest of parameters
			result.put(key.trim(), value);
			line = remainingLine;
			if(line.length() == 0)
				break;
		}
		return result;
	}
	
	public static String getUserType(ClangLine line) {
		return getUserType(line, 0);
	}
	
	public static String getSimplifiedUserType(String line) {
		ClangLine parsedLine = lineToMap(line);
		String userType = getUserType(parsedLine);
		return simplifyType(userType);
	}
	
	public static String getUserType(ClangLine line, int index) {
		String result = line.get("type", index);
		String[] strings = result.split("[%]");
		return strings[strings.length - 1];
	}
	
	public static BindingInfo parseBindingInfo(String line) {
		BindingInfo result = new BindingInfo();
		ClangLine parsedLine = lineToMap(line);

		result.bindingId = hexStrToInt(parsedLine.get("pointer"));
		result.location = parsedLine.get("srcRange");
		result.type = parsedLine.getAndCheck("type");
		result.name = parsedLine.getAndCheck("name");
		
		return result;
	}

	public static void addBinding(BindingInfo bindingInfo, IBinding binding) {
		_instance._idToBinding.put(bindingInfo.bindingId, binding);
	}

	public static void addBinding(String typeName, IBinding binding) {
		_instance._typeNameToBinding.put(typeName, binding);
		logger.debug("Adding type {}, {} to bindings", typeName, binding);
	}	
	
	public static IBinding getBinding(int bindingId) {
		return _instance._idToBinding.get(bindingId);
	}
	
	public static IBinding getBinding(String typeName) {
		return _instance._typeNameToBinding.get(typeName);
	}

	@Override
	public IASTDeclaration[] getDeclarations() {
		IASTDeclaration[] decls = new IASTDeclaration[_declarations.size()];
		return _declarations.toArray(decls);
	}

	@Override
	public IASTFileLocation getFileLocation() {
		return new CPPASTFileLocation(_fileName);
	}

	public static void setFileName(String file) {
		_fileName = file;
	}
	
	public static String getFileName() {
		return _fileName;
	}

	public static void addConstructorBinding(CPPConstructor cppConstructor, String classStr, String params) {
		String key = classStr + "-<>-" + params;
		_instance._constructorsBinding.put(key, cppConstructor);
	}

	public static CPPConstructor getConstructorBinding(String classStr, String params) {
		String key = classStr + "-<>-" + params;
		return _instance._constructorsBinding.get(key);
	}
	
	public static void addBindingOwner(int bindingId, Object bindingOwner) {
		_instance._bindingOwners.add(new BindingOwner(bindingId, bindingOwner));
	}
	
	public void fixBindingSynonyms() {
		for(BindingOwner bindingOwner : _bindingOwners) {
			IBinding binding = _bindingSynonyms.get(bindingOwner.originalBindingId);
			if(binding == null)
				continue;
			if(bindingOwner.owner instanceof CPPASTName) {
				((CPPASTName)bindingOwner.owner).setBinding( binding );
			} else if (bindingOwner.owner instanceof CPPASTFunctionDefinition) {
				((CPPASTFunctionDefinition)bindingOwner.owner)._binding = binding;
			}
		}
	}

	public static void addNamespace(String namespaceName) {
		_instance._namespaces.push(namespaceName);
	}
	
	public static void removeNamespace() {
		if(_instance._namespaces.size() == 0) {
			logger.error("Empty namespaces stack");
			return;
		}
		_instance._namespaces.remove();
	}
	
	public static String getCurrentNamespace() {
		String result = "";
		for(String namespace : _instance._namespaces) {
			result = result + namespace + "::";
		}
		return result;
	}
	
	@Override
	public boolean accept(ASTVisitor arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean contains(IASTNode arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public String getContainingFilename() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTNodeLocation[] getNodeLocations() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTNode getParent() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public ASTNodeProperty getPropertyInParent() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public String getRawSignature() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTTranslationUnit getTranslationUnit() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void addDeclaration(IASTDeclaration arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
	}

	@Override
	public IASTFileLocation flattenLocationsToFile(IASTNodeLocation[] arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTPreprocessorStatement[] getAllPreprocessorStatements() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTPreprocessorMacroDefinition[] getBuiltinMacroDefinitions() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTComment[] getComments() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public String getContainingFilename(int arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IName[] getDeclarations(IBinding arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTName[] getDeclarationsInAST(IBinding arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IName[] getDefinitions(IBinding arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTName[] getDefinitionsInAST(IBinding arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IDependencyTree getDependencyTree() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public String getFilePath() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTPreprocessorIncludeStatement[] getIncludeDirectives() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IIndex getIndex() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTNodeLocation[] getLocationInfo(int arg0, int arg1) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTPreprocessorMacroDefinition[] getMacroDefinitions() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public ParserLanguage getParserLanguage() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTProblem[] getPreprocessorProblems() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTName[] getReferences(IBinding arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IScope getScope() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public String getUnpreprocessedSignature(IASTNodeLocation[] arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTNode selectNodeForLocation(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setComments(IASTComment[] arg0) {}

	@Override
	public void setIndex(IIndex arg0) {}

	@Override
	public void setParent(IASTNode arg0) {}

	@Override
	public void setPropertyInParent(ASTNodeProperty arg0) {}

}
