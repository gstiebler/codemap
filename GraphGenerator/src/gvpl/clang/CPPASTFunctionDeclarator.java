package gvpl.clang;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;

public class CPPASTFunctionDeclarator implements org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator{

	static Logger logger = LogManager.getLogger(CPPASTFunctionDeclarator.class.getName());
	
	public CPPASTName _name = null;
	public IASTNode _parent = null;
	CPPASTFileLocation _location;
	IASTPointerOperator[] _pointerOperators;
	List<IASTParameterDeclaration> _parameters = new ArrayList<IASTParameterDeclaration>();
	List<ICPPASTConstructorChainInitializer> _constrChainInit = new ArrayList<ICPPASTConstructorChainInitializer>();
	
	public CPPASTFunctionDeclarator(IBinding binding, IASTNode parent, Cursor cursor) {
		_name = CPPASTName.loadASTName(binding, cursor.getLine(), this);
		_parent = parent;
		_pointerOperators = CPPASTDeclarator.loadPointerOps(cursor.getLine(), this);
		_location = new CPPASTFileLocation(cursor.nextLine(), parent);
		
		while(!cursor.theEnd()) {
			String type = CPPASTTranslationUnit.getType(cursor.getLine());
			if (type.equals("ParmVarDecl")) {
				_parameters.add(new CPPASTParameterDeclaration(cursor.getSubCursor(), this));
			} else if (type.equals("CXXCtorInitializer")) {
				_constrChainInit.add(new CPPASTConstructorChainInitializer(cursor.getSubCursor(), this));
			} else {
				return;
				//logger.error("Error reading " + type);
			}
		}
	}
	
	@Override
	public String toString() {
		return _name.toString();
	}
	
	@Override
	public void addParameterDeclaration(IASTParameterDeclaration arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IASTParameterDeclaration[] getParameters() {
		CPPASTParameterDeclaration[] result = new CPPASTParameterDeclaration[_parameters.size()];
		return _parameters.toArray(result);
	}

	@Override
	public ICPPASTConstructorChainInitializer[] getConstructorChain() {
		ICPPASTConstructorChainInitializer[] result = new ICPPASTConstructorChainInitializer[_constrChainInit.size()];
		return _constrChainInit.toArray(result);
	}

	@Override
	public IASTNode getParent() {
		return _parent;
	}

	@Override
	public IASTName getName() {
		return _name;
	}

	@Override
	public IASTFileLocation getFileLocation() {
		return _location;
	}

	@Override
	public IASTPointerOperator[] getPointerOperators() {
		return _pointerOperators;
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
	public IASTInitializer getInitializer() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTDeclarator getNestedDeclarator() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public boolean accept(ASTVisitor arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean contains(IASTNode arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public String getContainingFilename() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTNodeLocation[] getNodeLocations() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public ASTNodeProperty getPropertyInParent() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public String getRawSignature() {
		return _name.toString();
	}

	@Override
	public IASTTranslationUnit getTranslationUnit() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public int getRoleForName(IASTName arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return 0;
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
	public void addPointerOperator(IASTPointerOperator arg0) {}

	@Override
	public void setParent(IASTNode arg0) {}

	@Override
	public void setPropertyInParent(ASTNodeProperty arg0) {}

	@Override
	public void addConstructorToChain(ICPPASTConstructorChainInitializer arg0) {}

	@Override
	public void addExceptionSpecificationTypeId(IASTTypeId arg0) {}

	@Override
	public void setInitializer(IASTInitializer arg0) {}

	@Override
	public void setName(IASTName arg0) {}

	@Override
	public void setNestedDeclarator(IASTDeclarator arg0) {}

	@Override
	public void setConst(boolean arg0) {}

	@Override
	public void setPureVirtual(boolean arg0) {}

	@Override
	public void setVolatile(boolean arg0) {}

}