package gvpl.clang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	IASTExpression _operand1;
	IASTExpression _operand2;
	Map<String, Integer> _opMap = new HashMap<String, Integer>();
	
	public CPPASTBinaryExpression(Cursor cursor) {
		String line = cursor.nextLine();
		List<String> parsedLine = CPPASTTranslationUnit.parseLine(line);
		_operator = parsedLine.get( parsedLine.size() - 1 );
		
		_operand1 = loadOperand(cursor);
		_operand2 = loadOperand(cursor);
		
		_opMap.put("*", 1);
		_opMap.put("/", 2);
		_opMap.put("%", 3);
		_opMap.put("+", 4);
		_opMap.put("-", 5);
		_opMap.put("<<", 6);
		_opMap.put("*", 7);
		_opMap.put("<", 8);
		_opMap.put(">", 9);
		_opMap.put("<=", 10);
		_opMap.put(">=", 11);
		_opMap.put("&", 12);
		_opMap.put("^", 13);
		_opMap.put("|", 14);
		_opMap.put("&&", 15);
		_opMap.put("||", 16);
		_opMap.put("=", 17);

		// Field descriptor #8 I
/*		public static final int op_multiplyAssign = 18;

		// Field descriptor #8 I
		public static final int op_divideAssign = 19;

		// Field descriptor #8 I
		public static final int op_moduloAssign = 20;

		// Field descriptor #8 I
		public static final int op_plusAssign = 21;

		// Field descriptor #8 I
		public static final int op_minusAssign = 22;

		// Field descriptor #8 I
		public static final int op_shiftLeftAssign = 23;

		// Field descriptor #8 I
		public static final int op_shiftRightAssign = 24;

		// Field descriptor #8 I
		public static final int op_binaryAndAssign = 25;

		// Field descriptor #8 I
		public static final int op_binaryXorAssign = 26;

		// Field descriptor #8 I
		public static final int op_binaryOrAssign = 27;

		// Field descriptor #8 I
		public static final int op_equals = 28;

		// Field descriptor #8 I
		public static final int op_notequals = 29;

		// Field descriptor #8 I
		public static final int op_last = 29;*/
	}

	IASTExpression loadOperand(Cursor cursor) {
		String line = cursor.nextLine();
		cursor.back();
		String type = CPPASTTranslationUnit.getType(line);
		if(type.equals("DeclRefExpr")) {
			return new CPPASTIdExpression(cursor);
		} else if(type.equals("IntegerLiteral")) {
			return new CPPASTLiteralExpression(cursor);
		} else if(type.equals("BinaryOperator")) {
			return new CPPASTBinaryExpression(cursor);
		} else if(type.equals("ImplicitCastExpr")) {
			cursor.nextLine();
			return new CPPASTIdExpression(cursor);
		} else
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
	public IType getExpressionType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTExpression getOperand1() {
		return _operand1;
	}

	@Override
	public IASTExpression getOperand2() {
		return _operand2;
	}

	@Override
	public int getOperator() {
		return _opMap.get(_operator);
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
