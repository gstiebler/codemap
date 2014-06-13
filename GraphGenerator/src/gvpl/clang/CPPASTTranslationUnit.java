package gvpl.clang;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
			IASTDeclaration decl = loadDeclaration(cursor);
			if(decl != null)
				_declarations.add(decl);
		}
		fixBindingSynonyms();
	}
	
	static IASTDeclaration loadDeclaration(Cursor cursor) {
		String line = cursor.getLine();
		if(line.contains("<invalid sloc>")) {
			cursor.runToTheEnd();
			return null;
		}
		
		String type = getType(line);
		if (type.equals("FunctionDecl")) {
			return loadFuncDecl(cursor);
		} else if (type.equals("CXXMethodDecl") || type.equals("CXXConstructorDecl")) {
			List<Integer> ids = getIds(line);
			int parentId = ids.get(1);
			int prevId = ids.get(2);
			IBinding binding = getBinding(prevId);
			if(binding == null)
				logger.error("Prev Id {} not found", prevId);

			CPPASTFunctionDeclaration funcDecl = loadFuncDecl(cursor);
			
			CPPClassType ct = (CPPClassType) getBinding(parentId);
			ct._parent.replaceFuncDecl(binding, funcDecl);
					
			return funcDecl;
		} else if (type.equals("CXXRecordDecl") || type.equals("VarDecl")) {
			return new CPPASTSimpleDeclaration(cursor.getSubCursor(), null);
		} else if (type.equals("NamespaceDecl")) {
			return new CPPASTNamespaceDefinition(cursor.getSubCursor(), null);
		} else if (type.equals("EmptyDecl") || 
				type.equals("UsingDecl") || 
				type.equals("UsingDirectiveDecl") || 
				type.equals("UsingShadowDecl")) {
			cursor.runToTheEnd();
			return null;
		} else {
			logger.error("Not prepared for type {}, line {}", type, cursor.getPos());
			cursor.runToTheEnd();
			return null;
		}
	}
	
	static CPPASTFunctionDeclaration loadFuncDecl(Cursor cursor) {
		String line = cursor.getLine();
		CPPASTFunctionDeclaration funcDecl = new CPPASTFunctionDeclaration(cursor.getSubCursor(), false, null);
		List<Integer> ids = getIds(line);
		// has previous binding
		if(ids.size() > 1) {
			int oldId = ids.get(1);
			if(ids.size() == 3) // has parent id
				oldId = ids.get(2);
			_instance._bindingSynonyms.put(oldId, funcDecl._binding);
		}
		return funcDecl;
	}

	public static String getType(String line) {
		String temp1 = line.split("-")[1];
		String temp2 = temp1.split(" ")[0];
		return temp2;
	}
	
	public static String simplifyType(String line) {
		return line.replace("class", "").replace("struct", "").replace("*", "").replace("&", "").trim();
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
	
	public static List<Integer> getIds(String line) {
		List<String> parsedLine = parseLineSimple(line);
		
		List<Integer> ids = new ArrayList<Integer>();
		for(String pLine : parsedLine) {
			int currBindingId = hexStrToInt(pLine);
			if(currBindingId != -1)
				ids.add(currBindingId);
		}
		return ids;
	}
	
	private static List<String> parseLineSimple(String line) {
		List<String> result = new ArrayList<String>();
		
		String[] plic = line.split("'");
		for(int i = 0; i < plic.length; ++i) {
			if((i % 2) == 0) { // outside plics
				
				String[] postBico1 = plic[i].split("<");
				String[] space = postBico1[0].split(" ");
				for(String splited : space)
					result.add(splited);
				
				for(int j = 1; j < postBico1.length; ++j) {
					String[] postBico2 = postBico1[j].split(">");
					result.add(postBico2[0]);
					
					for(int k = 1; k < postBico2.length; ++k) {
						String[] space2 = postBico2[k].split(" ");
						for(String splited : space2)
							result.add(splited);
					}
				}
			} else { // what's inside plics
				result.add(plic[i]);
			}
		}
		
		List<String> result2 = new ArrayList<String>();
		for(String res : result) {
			if(!res.equals(""))
				result2.add(res);
		}
			
		return result2;
	}
	
	public static List<String> parseLine(String line) {
		int dashIndex = line.indexOf('-');
		String dash1 = line.substring(dashIndex + 1);
		List<String> result = parseLineSimple(dash1);
		return result;
	}
	
	public static BindingInfo parseBindingInfo(String line) {
		BindingInfo result = new BindingInfo();
		List<String> parsedLine = parseLine(line);

		result.bindingId = hexStrToInt(parsedLine.get(1));
		
		if(parsedLine.get(0).equals("public")) {
			return result;
		}
		
		if(parsedLine.get(2).equals("prev")) {
			result.location = parsedLine.get(4);
			result.type = parsedLine.get(7);
			result.name = result.type;
			return result;
		}
		
		if(parsedLine.get(0).equals("CXXNewExpr")) {
			result.location = parsedLine.get(2);
			result.type = parsedLine.get(3);
			return result;
		} else if (parsedLine.get(0).equals("CXXCtorInitializer")) {
			result.bindingId = hexStrToInt(parsedLine.get(2));
			result.type = parsedLine.get(3);
			result.name = parsedLine.get(4);
			return result;
		} else if (parsedLine.get(0).equals("CXXConstructExpr")) {
			result.location = parsedLine.get(2);
			result.type = parsedLine.get(3);
			result.name = parsedLine.get(4);
			return result;
		}
		
		result.location = parsedLine.get(3);
		result.name = parsedLine.get(4);
		if(parsedLine.get(0).equals("ParmVarDecl")) {
			if(parsedLine.size() < 6)
				return result;
		}
		
		result.type = parsedLine.get(5);
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
				((CPPASTName)bindingOwner.owner)._binding = binding;
			} else if (bindingOwner.owner instanceof CPPASTFunctionDeclaration) {
				((CPPASTFunctionDeclaration)bindingOwner.owner)._binding = binding;
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
