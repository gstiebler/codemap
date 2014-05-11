package gvpl.clang;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;

public class CPPASTFunctionDeclarator extends CPPASTDeclarator implements org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator{

	static Logger logger = LogManager.getLogger(CPPASTFunctionDeclarator.class.getName());
	
	public CPPASTName _name = null;
	public IASTNode _parentNode = null;
	CPPASTFileLocation _location;
	List<IASTParameterDeclaration> _parameters = new ArrayList<IASTParameterDeclaration>();
	
	public CPPASTFunctionDeclarator(IBinding binding, IASTNode parentNode, Cursor cursor) {
		super(cursor);
		_name = CPPASTName.loadASTName(binding, cursor.getLine());
		_parentNode = parentNode;
		_location = new CPPASTFileLocation(cursor.nextLine());
		while(!cursor.theEnd()) {
			String type = CPPASTTranslationUnit.getType(cursor.getLine());
			if (type.equals("ParmVarDecl")) {
				_parameters.add(new ASTParameterDeclaration(cursor));
			} else {
				return;
				//logger.error("Error reading " + type);
			}
		}
	}

	@Override
	public IASTNode getParent() {
		return _parentNode;
	}

	@Override
	public IASTFileLocation getFileLocation() {
		return _location;
	}

	@Override
	public IASTName getName() {
		return _name;
	}

	@Override
	public IASTParameterDeclaration[] getParameters() {
		ASTParameterDeclaration[] result = new ASTParameterDeclaration[_parameters.size()];
		return _parameters.toArray(result);
	}

	@Override
	public void addParameterDeclaration(IASTParameterDeclaration arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
	}

	@Override
	public void setVarArgs(boolean arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
	}

	@Override
	public boolean takesVarArgs() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public void addConstructorToChain(ICPPASTConstructorChainInitializer arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
	}

	@Override
	public void addExceptionSpecificationTypeId(IASTTypeId arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
	}

	@Override
	public ICPPASTConstructorChainInitializer[] getConstructorChain() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTTypeId[] getExceptionSpecification() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public ICPPFunctionScope getFunctionScope() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public boolean isConst() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isPureVirtual() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isVolatile() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public void setConst(boolean arg0) {}

	@Override
	public void setPureVirtual(boolean arg0) {}

	@Override
	public void setVolatile(boolean arg0) {}

}
