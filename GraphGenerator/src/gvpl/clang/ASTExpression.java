package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTExpression;

public class ASTExpression {

	static Logger logger = LogManager.getLogger(ASTExpression.class.getName());
	
	public static IASTExpression loadExpression(Cursor cursor) {
		String line = cursor.getLine();
		String type = CPPASTTranslationUnit.getType(line);
		if(type.equals("DeclRefExpr")) {
			return new CPPASTIdExpression(cursor);
		} else if(type.equals("IntegerLiteral") ||
				type.equals("FloatingLiteral")) {
			return new CPPASTLiteralExpression(cursor);
		} else if(type.equals("BinaryOperator")) {
			return new CPPASTBinaryExpression(cursor);
		} else if(type.equals("ImplicitCastExpr") ||
				type.equals("ParenExpr") ||
				type.equals("CStyleCastExpr") ||
				type.equals("ImplicitCastExpr") ||
				type.equals("CXXFunctionalCastExpr")) {
			cursor.nextLine();
			return loadExpression(cursor);
		} else if(type.equals("CallExpr")) {
			return new ASTFunctionCallExpression(cursor);
		} else {
			logger.error("Error reading " + type);
			return null;
		}
	}
	
}
