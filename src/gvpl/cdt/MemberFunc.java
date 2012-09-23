package gvpl.cdt;

import gvpl.common.ClassMember;
import gvpl.common.ClassVar;
import gvpl.common.FuncParameter;
import gvpl.common.MemberId;
import gvpl.common.Var;
import gvpl.common.VarInfo;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;

public class MemberFunc extends Function {

	private ClassDecl _parentClass;
	private ClassVar _tempClassVar = null;
	
	public MemberFunc(ClassDecl parent, AstInterpreter astInterpreter, int startingLine) {
		super(new Graph(startingLine), null, astInterpreter);
		_parentClass = parent;
	}

	protected String calcName() {
		return _parentClass.getName() + "::" + _funcName;
	}

	@Override
	public IBinding load(IASTFunctionDefinition fd) {
		IBinding result = super.load(fd);
		if (_funcName.equals(_parentClass.getName()))
			_parentClass.setConstructorFunc(this);
		return result;
	}

	@Override
	void loadConstructorChain(ICPPASTConstructorChainInitializer[] constructorInit) {
		for (ICPPASTConstructorChainInitializer initializer : constructorInit) {
			int startingLine = initializer.getFileLocation().getStartingLineNumber();
			
			IASTExpression expr = initializer.getInitializerValue();
			
			IASTName memberInitId = initializer.getMemberInitializerId();
			IBinding member_binding = memberInitId.resolveBinding();
			Var var = getVarFromBinding(member_binding);
			
			InstructionLine instructionLine = new InstructionLine(_gvplGraph, this, _astInterpreter);
			instructionLine.loadConstructorInitializer(var, expr, startingLine);
		}
	}

	@Override
	protected Var getVarFromBinding(IBinding binding) {
		ClassMember member = _parentClass.getMember(binding);
		if(member != null && _tempClassVar != null) {
			MemberId memberId = member.getMemberId();
			Var var = _tempClassVar.getMember(memberId);
			if(var != null)
				return var;
		}
		
		// search the variable in the function parameters
		Var var = super.getVarFromBinding(binding);
		if(var != null)
			return var;
		
		//if the variable isn't in scope, create it
		return createVarFromBinding(binding, -2);
	}
	
	@Override
	protected VarInfo getTypeFromVarBinding(IBinding binding) {
		ClassMember classMember = _parentClass.getMember(binding);
		if(classMember != null)
			return classMember.getVarInfo();
		
		VarInfo varInfo = super.getTypeFromVarBinding(binding);
		if(varInfo != null)
			return varInfo;
		
		return _parent.getTypeFromVarBinding(binding);
	}
	
	/**
	 * Copy the internal graph to the main graph and bind the variables of the
	 * structure to the used variables in the member function
	 * 
	 * @param classVar
	 * @param graphBuilder
	 */
	public GraphNode loadMemberFuncRef(ClassVar classVar, List<FuncParameter> parameter_values,
			Graph graph, AstLoader astLoader, int startingLine) {
		_tempClassVar = classVar;
		Map<GraphNode, GraphNode> map = addSubGraph(graph, astLoader, startingLine);
		_tempClassVar = null;
		
		return addParametersReferenceAndReturn(parameter_values, map, startingLine);
	}

}
