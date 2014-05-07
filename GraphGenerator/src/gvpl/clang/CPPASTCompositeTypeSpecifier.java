package gvpl.clang;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;

public class CPPASTCompositeTypeSpecifier extends ASTDeclSpecifier implements org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier {

	static Logger logger = LogManager.getLogger(CPPASTCompositeTypeSpecifier.class.getName());

	List<IASTDeclaration> _members = new ArrayList<IASTDeclaration>();
	IASTName _name;
	
	public CPPASTCompositeTypeSpecifier(Cursor cursor) {
		super(cursor);
		
		String line = cursor.nextLine();
		//_name = new CPPASTName(binding, line);
		String classType = CPPASTTranslationUnit.getType( line );
		if(!classType.equals("CXXRecordDecl"))
			logger.error("Type not expected " + classType);
		
		//This line or the next?
		IBinding binding = new CPPClassType(line);
		_name = new CPPASTName(binding, line);
		
		while(!cursor.theEnd()) {
			String type = CPPASTTranslationUnit.getType( cursor.getLine() );
			if(type.equals("CXXMethodDecl")) {
				_members.add(new CPPASTFunctionDeclaration(cursor));
			} else {
				logger.error("Type not expected " + classType);
			}
			cursor.nextLine();
		}
	}

	@Override
	public void addMemberDeclaration(IASTDeclaration arg0) { }

	@Override
	public int getKey() {
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public IASTDeclaration[] getMembers() {
		IASTDeclaration[] result = new IASTDeclaration[_members.size()];
		return _members.toArray(result);
	}

	@Override
	public IASTName getName() {
		return _name;
	}

	@Override
	public IScope getScope() {
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setKey(int arg0) { }

	@Override
	public void setName(IASTName arg0) { }

	@Override
	public int getRoleForName(IASTName arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isExplicit() {
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isFriend() {
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isVirtual() {
		logger.error("Not implemented");
		return false;
	}

	@Override
	public void setExplicit(boolean arg0) { }

	@Override
	public void setFriend(boolean arg0) { }

	@Override
	public void setVirtual(boolean arg0) { }

	@Override
	public void addBaseSpecifier(ICPPASTBaseSpecifier arg0) { }

	@Override
	public ICPPASTBaseSpecifier[] getBaseSpecifiers() {
		// TODO Auto-generated method stub
		return new ICPPASTBaseSpecifier[0];
	}

}
