package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

public class ASTDeclSpecifier extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier{

	static Logger logger = LogManager.getLogger(ASTDeclSpecifier.class.getName());
	
	int _storageClass = -1;
	
	public ASTDeclSpecifier(Cursor cursor) {
		super(cursor.getLine());
		if(CPPMethod.isStatic(cursor.getLine()))
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
