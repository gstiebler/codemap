package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class ASTDeclSpecifier implements org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier{

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
	public String getRawSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStorageClass() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isConst() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInline() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVolatile() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setConst(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInline(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStorageClass(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVolatile(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

}
