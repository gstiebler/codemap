package gvpl.clang;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IScope;

public class CPPASTCompoundStatement implements org.eclipse.cdt.core.dom.ast.IASTCompoundStatement {

	public List<IASTStatement> _statements = new ArrayList<IASTStatement>();
	
	public CPPASTCompoundStatement(Cursor cursor) {
		String compoundLine = cursor.nextLine();
		while(!cursor.theEnd()) {
			String stmtLine = cursor.nextLine();
			cursor.back();
			String stmtType = CPPASTTranslationUnit.getType(stmtLine);
			if(stmtType.equals("DeclStmt"))
				_statements.add(new ASTDeclarationStatement(cursor.getSubCursor()));
			else if (stmtType.equals("BinaryOperator")) {
				//List<String> parsedLine = CPPASTTranslationUnit.parseLine(stmtType);
				
			} else
				_statements.add(new CPPASTStatement(cursor.getSubCursor()));
		}
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
	public void addStatement(IASTStatement arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IScope getScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTStatement[] getStatements() {
		IASTStatement[] result = new IASTStatement[_statements.size()];
		return _statements.toArray(result);
	}


}
