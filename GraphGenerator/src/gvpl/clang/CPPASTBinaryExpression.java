package gvpl.clang;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IType;

public class CPPASTBinaryExpression implements org.eclipse.cdt.core.dom.ast.IASTBinaryExpression {

	String _operator;
	
	public CPPASTBinaryExpression(Cursor cursor) {
		String line = cursor.nextLine();
		List<String> parsedLine = CPPASTTranslationUnit.parseLine(line);
		_operator = parsedLine.get(6);
		
		op1 = cursor.nextLine();
		op2 = cursor.nextLine();
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
	public IType getExpressionType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTExpression getOperand1() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTExpression getOperand2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getOperator() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setOperand1(IASTExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOperand2(IASTExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOperator(int arg0) {
		// TODO Auto-generated method stub
		
	}

}
