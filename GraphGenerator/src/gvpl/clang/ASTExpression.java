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
				type.equals("FloatingLiteral") ||
				type.equals("CXXBoolLiteralExpr") ||
				type.equals("StringLiteral")) {
			return new CPPASTLiteralExpression(cursor, parent);
		} else if(type.equals("CXXThisExpr")) {
			return new CPPASTLiteralExpression(cursor.getSubCursor(), parent, "this");
		} else if(type.equals("BinaryOperator") || 
				type.equals("CompoundAssignOperator") || 
				type.equals("CXXOperatorCallExpr")) {
			return new CPPASTBinaryExpression(cursor.getSubCursor(), parent);
		} else if(type.equals("ImplicitCastExpr") ||
				type.equals("ParenExpr") ||
				type.equals("CStyleCastExpr") ||
				type.equals("ImplicitCastExpr") ||
				type.equals("CXXFunctionalCastExpr")) {
			cursor.nextLine();
			return loadExpression(cursor, parent);
		} else if(type.equals("CallExpr") || 
				type.equals("CXXMemberCallExpr")) {
			return new CPPASTFunctionCallExpression(cursor, parent);
		} else if(type.equals("CXXConstructExpr")) {
			cursor.nextLine();
			return new CPPASTExpressionList(cursor, parent);
		} else if(type.equals("MemberExpr")) {
			Cursor dcursor = cursor.getDetachedSubCursor();
			dcursor.nextLine();
			while(!dcursor.theEnd()) {
				String secType = CPPASTTranslationUnit.getType(dcursor.getLine());
				if(secType.equals("CXXThisExpr")) {
					IASTExpression result = new CPPASTIdExpression(cursor.getSubCursor(), parent);
					cursor.runToTheEnd();
					return result;
				} else if (secType.equals("MemberExpr")) {
					break;
				}
				dcursor.nextLine();
			}
			return new CPPASTFieldReference(cursor.getSubCursor(), parent);
		} else if(type.equals("ConditionalOperator")) {
			return new CPPASTConditionalExpression(cursor.getSubCursor(), parent);
		} else if(type.equals("UnaryOperator")) {
			return new CPPASTUnaryExpression(cursor.getSubCursor(), parent);
		} else if(type.equals("CXXNewExpr")) {
			return new CPPASTNewExpression(cursor.getSubCursor(), parent);
		} else if(type.equals("ArraySubscriptExpr")) {
			return new CPPASTArraySubscriptExpression(cursor.getSubCursor(), parent);
		} else if(type.equals("CXXDeleteExpr")) {
			return new CPPASTDeleteExpression(cursor.getSubCursor(), parent);
		} else if(type.equals("UnaryExprOrTypeTraitExpr")) {//TODO check if it's always sizeof
			return new ClangSizeof(cursor.getSubCursor(), parent);
		} else if(type.equals("CXXThisExpr")) {
			logger.warn("Check if {} should be treated", type);
			cursor.runToTheEnd();
			return null;
		} else {
			logger.error("Error reading {}, line {}", type, cursor.getPos());
			cursor.runToTheEnd();
			return null;
		}
	}
	
}
