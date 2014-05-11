package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class ASTNode implements org.eclipse.cdt.core.dom.ast.IASTNode {

	static Logger logger = LogManager.getLogger(ASTNode.class.getName());
	
	CPPASTFileLocation _location;
	protected IASTNode _parent;
	
	public ASTNode(String line, IASTNode parent) {
		_parent = parent;
		_location = new CPPASTFileLocation(line, parent);
	}

	@Override
	public IASTNode getParent() {
		return _parent;
	}

	@Override
	public IASTFileLocation getFileLocation() {
		return _location;
	}
	
	@Override
	public boolean accept(ASTVisitor arg0) {
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean contains(IASTNode arg0) {
		logger.error("Not implemented");
		return false;
	}

	@Override
	public String getContainingFilename() {
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTNodeLocation[] getNodeLocations() {
		logger.error("Not implemented");
		return null;
	}

	@Override
	public ASTNodeProperty getPropertyInParent() {
		logger.error("Not implemented");
		return null;
	}

	@Override
	public String getRawSignature() {
		return _location.toString();
	}

	@Override
	public IASTTranslationUnit getTranslationUnit() {
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setParent(IASTNode arg0) { }

	@Override
	public void setPropertyInParent(ASTNodeProperty arg0) { }

}
