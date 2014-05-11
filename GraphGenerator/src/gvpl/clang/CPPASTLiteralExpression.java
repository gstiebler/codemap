package gvpl.clang;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;

public class CPPASTLiteralExpression extends ASTNode implements org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression {
	
	static Logger logger = LogManager.getLogger(CPPASTLiteralExpression.class.getName());

	String _value;
	
	public CPPASTLiteralExpression(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		String line = cursor.nextLine();
		List<String> parsedLine = CPPASTTranslationUnit.parseLine(line);
		_value = parsedLine.get(4);
	}
	
	public String toString() {
		return _value;
	}

	@Override
	public int getKind() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public IType getExpressionType() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setKind(int arg0) {}

	@Override
	public void setValue(String arg0) {}
	

}
