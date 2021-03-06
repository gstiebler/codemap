package gvpl.clang;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;

public class CPPASTCompositeTypeSpecifier extends CPPASTBaseDeclSpecifier implements org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier {

	static Logger logger = LogManager.getLogger(CPPASTCompositeTypeSpecifier.class.getName());

	List<IASTDeclaration> _members = new ArrayList<IASTDeclaration>();
	List<ICPPASTBaseSpecifier> _baseSpecs = new ArrayList<ICPPASTBaseSpecifier>();
	IASTName _name;
	
	public CPPASTCompositeTypeSpecifier(Cursor cursor, IASTNode parent) {
		super(cursor, parent);
		String line = cursor.getLine();
		IBinding binding = new CPPClassType(line, this);
		_name = CPPASTName.loadASTName(binding, line, this);
		cursor.nextLine();
		initialize(cursor);
	}
	
	public CPPASTCompositeTypeSpecifier(Cursor cursor, IASTNode parent, IASTName name) {
		super(cursor, parent);
		_name = name;
		initialize(cursor);
	}
	
	void initialize(Cursor cursor) {
		if(cursor.theEnd()) // probably a forward declaration
			return;
		String line = cursor.getLine();
		String classType;
		while(true) {
			classType = CPPASTTranslationUnit.getType(cursor.getLine());
			if(!classType.equals("AccessSpecifier:public")) { //TODO private and protected
				break;
			}

			_baseSpecs.add(new CPPASTBaseSpecifier(cursor.getSubCursor(), this));
		}
		cursor.nextLine();
		//_name = new CPPASTName(binding, line);
		if(!classType.equals("CXXRecordDecl"))
			logger.error("Type not expected " + classType);
		
		CPPASTTranslationUnit.lastClassName = _name;
		
		while(!cursor.theEnd()) {
			String type = CPPASTTranslationUnit.getType( cursor.getLine() );
			if(type.equals("CXXMethodDecl") || type.equals("CXXDestructorDecl")) {
				line = cursor.getLine();
				ClangLine parsedLine = CPPASTTranslationUnit.lineToMap(line);
				Set<String> funcDeclSpec = parsedLine.getSet("funcDecl");
				if(!funcDeclSpec.contains("noexcept-unevaluated")) {
					IASTDeclaration funcDecl = CPPASTTranslationUnit.loadFuncDecl(cursor.getSubCursor(), true, this);
					_members.add(funcDecl);
				} else {
					cursor.runToTheEnd();
				}
			} else if(type.equals("FieldDecl") || 
					type.equals("VarDecl") || 
					type.equals("EnumDecl")) {
				_members.add(new CPPASTSimpleDeclaration(cursor.getSubCursor(), this));
			} else if(type.equals("CXXConstructorDecl")) {
				_members.add(CPPASTTranslationUnit.loadFuncDecl(cursor.getSubCursor(), true, this));
			} else if(type.equals("AccessSpecDecl")) {
				logger.info("AccessSpecDecl");
				cursor.runToTheEnd();
			} else {
				logger.error("Type not expected " + type);
				cursor.nextLine();
			}
		}
		logger.debug("wow");
	}
	
	public String toString() {
		return _name.toString();
	}

	@Override
	public ICPPASTBaseSpecifier[] getBaseSpecifiers() {
		ICPPASTBaseSpecifier[] result = new ICPPASTBaseSpecifier[_baseSpecs.size()];
		return _baseSpecs.toArray(result);
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
		logger.error("Not implemented");
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

}
