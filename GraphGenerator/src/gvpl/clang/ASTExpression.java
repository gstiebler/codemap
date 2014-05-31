package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class ASTExpression {

	static Logger logger = LogManager.getLogger(ASTExpression.class.getName());
	
	public static IASTExpression loadExpression(Cursor cursor, IASTNode parent) {
		String line = cursor.getLine();
		String type = CPPASTTranslationUnit.getType(line);
		if(type.equals("DeclRefExpr")) {
			return new CPPASTIdExpression(cursor, parent);
		} else if(type.equals("IntegerLiteral") ||
				type.equals("FloatingLiteral")) {
			return new CPPASTLiteralExpression(cursor, parent);
		} else if(type.equals("BinaryOperator") || type.equals("CompoundAssignOperator")) {
			return new CPPASTBinaryExpression(cursor, parent);
		} else if(type.equals("ImplicitCastExpr") ||
				type.equals("ParenExpr") ||
				type.equals("CStyleCastExpr") ||
				type.equals("ImplicitCastExpr") ||
				type.equals("CXXFunctionalCastExpr")) {
			cursor.nextLine();
			return loadExpression(cursor, parent);
		} else if(type.equals("CallExpr") || type.equals("CXXMemberCallExpr")) {
			return new ASTFunctionCallExpression(cursor, parent);
		} else if(type.equals("CXXConstructExpr")) {
			logger.error("Error reading " + type);
			cursor.runToTheEnd();
			return null;
		} else if(type.equals("MemberExpr")) {
			Cursor dcursor = cursor.getDetachedSubCursor();
			dcursor.nextLine();
			String secType = CPPASTTranslationUnit.getType(dcursor.getLine());
			if(secType.equals("CXXThisExpr")) {
				IASTExpression result = new CPPASTIdExpression(cursor, parent);
				cursor.runToTheEnd();
				return result;
			} else
				return new CPPASTFieldReference(cursor.getSubCursor(), parent);
		} else if(type.equals("ConditionalOperator")) {
			return new CPPASTConditionalExpression(cursor.getSubCursor(), parent);
		} else if(type.equals("UnaryOperator")) {
			return new CPPASTUnaryExpression(cursor.getSubCursor(), parent);
		} else if(type.equals("CXXThisExpr")) {
			logger.warn("Check if {} should be treated", type);
			cursor.runToTheEnd();
			return null;
		} else {
			logger.error("Error reading " + type);
			cursor.runToTheEnd();
			return null;
		}
	}
	
}
