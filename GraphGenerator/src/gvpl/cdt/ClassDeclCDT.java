package gvpl.cdt;

import gvpl.cdt.function.Function;
import gvpl.cdt.function.MemberFunc;
import gvpl.common.ClassDecl;
import gvpl.common.ClassMember;
import gvpl.common.CodeLocation;
import gvpl.common.FuncParameter;
import gvpl.common.MemberId;
import gvpl.common.TypeId;
import gvpl.graph.Graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTOperatorName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPScope.CPPScopeProblem;

import debug.DebugOptions;

public class ClassDeclCDT extends ClassDecl{

	static Logger logger = LogManager.getLogger(ClassDeclCDT.class.getName());

	private AstInterpreterCDT _astInterpreter;

	//TODO debug, should be private
	public Map<IBinding, ClassMember> _memberIdMap;
	private Map<CodeLocation, ClassMember> _membersLocation = new TreeMap<CodeLocation, ClassMember>();
	private Map<IBinding, MemberFunc> _memberFuncIdMap;
	private Map<CodeLocation, MemberFunc> _membersFuncLocation = new TreeMap<CodeLocation, MemberFunc>();
	private List<ClassDeclCDT> _parentClasses = new ArrayList<ClassDeclCDT>();
	ICPPASTCompositeTypeSpecifier _classDecl;

	public ClassDeclCDT(AstInterpreterCDT astInterpreter, CodeLocation location, IBinding binding) {
		super(location, binding);
		logger.debug("Loading class in file {}", location.getFileName());
		_astInterpreter = astInterpreter;
	}
	
	private void getCDTMembers(ICPPASTCompositeTypeSpecifier classDecl, 
			List<IASTFunctionDeclarator> functionDeclarators, 
			List<IASTFunctionDefinition> functionDefinitions, 
			List<IASTDeclarator> membersDeclaration) {
		IASTDeclaration[] members = classDecl.getMembers();

		// load every field
		for (IASTDeclaration member : members) {
			logger.debug("Member declaration: {}, type: {}", member.getRawSignature(), 
					member.getClass());
			if (member instanceof IASTFunctionDefinition)
				functionDefinitions.add((IASTFunctionDefinition) member);
			
			if (!(member instanceof IASTSimpleDeclaration)) {
				continue;
			}

			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) member;
			
			IASTDeclSpecifier declSpec = simpleDecl.getDeclSpecifier();
			if(declSpec instanceof IASTEnumerationSpecifier) {
				EnumCDT.loadEnum( (IASTEnumerationSpecifier) declSpec, _astInterpreter );
				continue;
			} else if (declSpec instanceof IASTNamedTypeSpecifier) {
				IASTName name = ((IASTNamedTypeSpecifier) declSpec).getName();
				if(name instanceof CPPASTTemplateId) {
					//((CPPASTNamedTypeSpecifier) declSpec).
					logger.warn("Not implemented: CPPASTTemplateId, {}", name.getRawSignature());
					continue;
				}
			}
			
			
			IASTDeclarator[] declarators = simpleDecl.getDeclarators();
			// for each variable declared in a line
			for (IASTDeclarator declarator : declarators) {
				String rs = declarator.getRawSignature();
				logger.debug("Member declarator: {}, type: {}", rs, declarator.getClass());
				if (declarator instanceof IASTFunctionDeclarator)
					functionDeclarators.add((IASTFunctionDeclarator) declarator);	
				else
					membersDeclaration.add(declarator);
			}
		}
	}
	
	/**
	 * Loads the declaration of a composite type
	 * @param classDecl Declaration of the type
	 * @param graph The Graph
	 */
	public void loadAstDecl(ICPPASTCompositeTypeSpecifier classDecl, Graph graph) {
		_classDecl = classDecl;

		_memberIdMap = new LinkedHashMap<IBinding, ClassMember>();
		_memberFuncIdMap = new LinkedHashMap<IBinding, MemberFunc>();
		
		loadBaseClasses(_classDecl.getBaseSpecifiers(), _astInterpreter);
		
		List<IASTFunctionDeclarator> functionDeclarators = new ArrayList<IASTFunctionDeclarator>();
		List<IASTFunctionDefinition> functionDefinitions = new ArrayList<IASTFunctionDefinition>();
		List<IASTDeclarator> membersDeclaration = new ArrayList<IASTDeclarator>();
		getCDTMembers(_classDecl, functionDeclarators, functionDefinitions, membersDeclaration);
		
		for(IASTDeclarator memberDeclarator : membersDeclaration) {
			MemberId memberId = new MemberId();
			
			IASTName memberName = memberDeclarator.getName();
			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) memberDeclarator.getParent();

			TypeId paramType = _astInterpreter.getType(simpleDecl.getDeclSpecifier());
			
			IASTPointerOperator[] pointerOps = memberDeclarator.getPointerOperators();		
			FuncParameter.IndirectionType indirectionType = Function.getIndirectionType(pointerOps);
			
			//TODO insert the correct IndirectionType
			ClassMember classMember = new ClassMember(memberId, memberName.toString(), paramType, indirectionType);
			
			IASTDeclSpecifier declSpec = simpleDecl.getDeclSpecifier();
			classMember._isStatic = declSpec.getStorageClass() == IASTDeclSpecifier.sc_static;
			
			if(classMember._isStatic) {
				_astInterpreter.addGlobalVar(memberDeclarator, declSpec);
			} else {
				_memberVarGraphNodes.put(classMember.getMemberId(), classMember);
				_memberIdMap.put(memberName.resolveBinding(), classMember);
				CodeLocation memberLocation = CodeLocationCDT.NewFromFileLocation(memberDeclarator);
				_membersLocation.put(memberLocation, classMember);
			}
		}
		
		for(IASTFunctionDeclarator functionDeclarator : functionDeclarators)
			loadMemberFuncDecl(functionDeclarator, _astInterpreter);
		
		for(IASTFunctionDefinition functionDefinition : functionDefinitions)
			loadMemberFunc(functionDefinition, _astInterpreter);	
	}
	
	public void updateBindings(ICPPASTCompositeTypeSpecifier classDecl) {
		//_memberIdMap = new LinkedHashMap<IBinding, ClassMember>();
		//_memberFuncIdMap = new LinkedHashMap<IBinding, MemberFunc>();
		
		List<IASTFunctionDeclarator> functionDeclarators = new ArrayList<IASTFunctionDeclarator>();
		List<IASTFunctionDefinition> functionDefinitions = new ArrayList<IASTFunctionDefinition>();
		List<IASTDeclarator> membersDeclaration = new ArrayList<IASTDeclarator>();
		getCDTMembers(classDecl, functionDeclarators, functionDefinitions, membersDeclaration);
		
		for(IASTFunctionDeclarator functionDeclarator : functionDeclarators){
			CodeLocation funcLocation = CodeLocationCDT.NewFromFileLocation(functionDeclarator);
			IBinding memberFuncBinding = functionDeclarator.getName().resolveBinding();
			MemberFunc memberFunc = _membersFuncLocation.get(funcLocation);
			_memberFuncIdMap.put(memberFuncBinding, memberFunc);
		}
		
		for(IASTFunctionDefinition functionDefinition : functionDefinitions) {
			IASTFunctionDeclarator funcDecl = functionDefinition.getDeclarator();
			CodeLocation funcLocation = CodeLocationCDT.NewFromFileLocation(funcDecl);
			IBinding memberFuncBinding = funcDecl.getName().resolveBinding();
			MemberFunc memberFunc = _membersFuncLocation.get(funcLocation);
			_memberFuncIdMap.put(memberFuncBinding, memberFunc);
		}
		
		for(IASTDeclarator memberDeclarator : membersDeclaration) {
			CodeLocation memberLocation = CodeLocationCDT.NewFromFileLocation(memberDeclarator);
			IASTName memberName = memberDeclarator.getName();
			ClassMember structMember = _membersLocation.get(memberLocation);
			IBinding binding = memberName.resolveBinding();
			_memberIdMap.put(binding, structMember);
		}
	}
	
	public void setBinding(IASTName name) {
		_name = name.toString();
	}
	
	void loadBaseClasses(ICPPASTBaseSpecifier[] baseSpecs, AstInterpreterCDT astInterpreter) {
		for(ICPPASTBaseSpecifier baseSpec : baseSpecs) {
			IASTName baseSpecName = baseSpec.getName();
			IBinding binding = baseSpecName.resolveBinding();
			if(binding instanceof CPPScopeProblem)
			{
				logger.error("Problema in base class. Class {}, Base Class {}",
						_name, baseSpecName.toString());
				continue;
			}
			ClassDeclCDT parentClass = astInterpreter.getClassDecl(binding);
			if(parentClass == null){
				logger.error("Parent class from {} is null", _name);
				continue;
			}
			_parentClasses.add(parentClass);
		}
	}
	
	private MemberFunc loadMemberFuncDecl(IASTFunctionDeclarator funcDeclarator, AstInterpreterCDT astInterpreter) {
		CodeLocation funcLocation = CodeLocationCDT.NewFromFileLocation(funcDeclarator);
		IBinding memberFuncBinding = funcDeclarator.getName().resolveBinding();
		if(memberFuncBinding instanceof ITypedef)
		{
			logger.warn("Not implemented: CPPTypedef {}", memberFuncBinding.getName());
			return null;
		}
		
		MemberFunc memberFunc = _memberFuncIdMap.get(memberFuncBinding);
		if(memberFunc != null)
			return memberFunc;
		
		memberFunc = new MemberFunc(this, astInterpreter, memberFuncBinding);
		memberFunc.loadDeclaration(funcDeclarator);

		_memberFuncIdMap.put(memberFuncBinding, memberFunc);
		_membersFuncLocation.put(funcLocation, memberFunc);
		
		if(funcDeclarator.getName() instanceof CPPASTOperatorName)
		{
			String name = memberFunc.getName();
			String op = name.substring(name.length() - 1);
			Integer opId = CppMaps._opAssignStrToId.get(op);
			_opOverloadMethods.put(opId, memberFunc);
		}
		
		return memberFunc;
	}

	public void loadMemberFunc(IASTFunctionDefinition member, AstInterpreterCDT astInterpreter) {
		IASTDeclarator declarator = member.getDeclarator();
		DebugOptions.setStartingLine(declarator.getFileLocation().getStartingLineNumber());
		IASTFunctionDeclarator funcDeclarator = (IASTFunctionDeclarator) declarator;
		
		MemberFunc memberFunc = loadMemberFuncDecl(funcDeclarator, astInterpreter);
		memberFunc.initializeDefinition(funcDeclarator.getConstructorChain(), member);
	}

	public ClassMember getMember(IBinding binding) {
		ClassMember member = _memberIdMap.get(binding);
		if(member != null)
			return member;
		
		for(ClassDeclCDT parent : _parentClasses) {
			member = parent.getMember(binding);
			if(member != null)
				return member;
		}
		
		return null;
	}
//
	public MemberFunc getMemberFunc(IBinding binding) {
		if(_memberFuncIdMap == null)
		{
			logger.debug("Class {} not implemented", _name);
			return null;
		}
		
		MemberFunc memberFunc = _memberFuncIdMap.get(binding);
		if(memberFunc != null)
			return memberFunc;
		
		for(MemberFunc memberF : _memberFuncIdMap.values()) {
			if(memberF == null)
				continue;
			MemberFunc parentMemberFunc = memberF.getParentMemberFunc();
			if(parentMemberFunc == null)
				continue;
			
			if(parentMemberFunc.getBinding() == binding)
				return memberF;
		}
		
		for(ClassDeclCDT parent : _parentClasses) {
			memberFunc = parent.getMemberFunc(binding);
			if(memberFunc != null)
				return memberFunc;
		}
		
		return null;
	}
	
	public Iterable<ClassDeclCDT> getParentClassesCDT() {
		return _parentClasses;
	}
	
	//TODO review efficiency
	@Override
	public Iterable<ClassDecl> getParentClasses() {
		List<ClassDecl> list = new ArrayList<ClassDecl>();
		list.addAll(_parentClasses);
		return list;
	}
	
	@Override
	protected Iterable<MemberFunc> getMemberFuncList() {
		return _memberFuncIdMap.values();
	}
}
