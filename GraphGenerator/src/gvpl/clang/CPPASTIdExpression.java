package gvpl.clang;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;

public class CPPASTIdExpression implements org.eclipse.cdt.core.dom.ast.IASTIdExpression {

	public CPPASTName _name = null;
		
	public CPPASTIdExpression(Cursor cursor) {
		String line = cursor.nextLine();
		List<String> parsedLine = CPPASTTranslationUnit.parseLine(line);
		String bindingStr = parsedLine.get(6);
		bindingStr = bindingStr.split("0x")[1];
		int bindingId = Integer.parseInt(bindingStr, 16);
		
		IBinding binding = CPPASTTranslationUnit.getBinding(bindingId);
		_name = CPPASTName.loadASTName(binding, line);
	}
	
	public String toString() {
		return _name.toString();
	}
	
	@Override
	public IType getExpressionType() {
		// TODO Auto-generated method stub
		return null;
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
	public int getRoleForName(IASTName arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IASTName getName() {
		return _name;
	}

	@Override
	public void setName(IASTName arg0) {
		// TODO Auto-generated method stub
		
	}

}
