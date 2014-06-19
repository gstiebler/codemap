package gvpl.clang;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class CPPASTBaseDeclSpecifier extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier{

	static Logger logger = LogManager.getLogger(CPPASTBaseDeclSpecifier.class.getName());
	
	int _storageClass = -1;
	
	public static IASTDeclSpecifier loadDeclSpec(Cursor cursor, IASTNode parent) {
		String line = cursor.getLine();
		String type = CPPASTTranslationUnit.getUserType(line);
		// separates *
		String simpleType = CPPASTTranslationUnit.simplifyType(type);
		//TODO improve, it will not work with typedefs or defines
		if(simpleType.equals("float") || 
				simpleType.equals("int") ||
				simpleType.equals("bool") ||
				simpleType.equals("void") )
			return new CPPASTSimpleDeclSpecifier(cursor, parent);
		else
			return new CPPASTNamedTypeSpecifier(cursor, parent);
	}
	
	CPPASTBaseDeclSpecifier(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		ClangLine parsedLine = CPPASTTranslationUnit.lineToMap(cursor.getLine());
		IBinding previousBinding = null;
		if(parsedLine.containsKey("prev")) {// has bind, prev and parent
			int prev = CPPASTTranslationUnit.hexStrToInt( parsedLine.get("prev") );
			previousBinding = CPPASTTranslationUnit.getBinding(prev);
		}
		if(previousBinding instanceof CPPMethod) {
			if( ((CPPMethod)previousBinding)._isStatic ) {
				_storageClass = IASTDeclSpecifier.sc_static;
			}
		} else if(CPPMethod.isStatic(cursor.getLine()))
			_storageClass = IASTDeclSpecifier.sc_static;
	}

	@Override
	public int getStorageClass() {
		return _storageClass;
	}

	@Override
	public boolean isConst() {
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isInline() {
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isVolatile() {
		logger.error("Not implemented");
		return false;
	}

	@Override
	public void setConst(boolean arg0) { }

	@Override
	public void setInline(boolean arg0) { }

	@Override
	public void setStorageClass(int arg0) { }

	@Override
	public void setVolatile(boolean arg0) { }

}
