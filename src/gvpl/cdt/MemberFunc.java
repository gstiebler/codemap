package gvpl.cdt;

import gvpl.common.ClassMember;
import gvpl.common.ClassVar;
import gvpl.common.ErrorOutputter;
import gvpl.common.FuncParameter;
import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;

public class MemberFunc extends Function {

	private ClassDecl _parentClass;
	private Map<MemberId, Var> _varFromMembersMap = new HashMap<MemberId, Var>();
	private Map<Var, MemberId> _memberFromVar = new HashMap<Var, MemberId>();
	private Set<Var> _writtenVars = new HashSet<Var>();
	private Set<Var> _readVars = new HashSet<Var>();

	public MemberFunc(ClassDecl parent, CppMaps cppMaps, AstInterpreter astInterpreter, int startingLine) {
		super(new GraphBuilder(cppMaps), null, cppMaps, astInterpreter);
		_parentClass = parent;

		List<ClassMember> members = _parentClass.getMembers();
		// declare a variable for each member of the struct
		for (ClassMember member : members) {
			Var member_var = addVarDecl(member.getName(), member.getMemberType(), null);
			member_var.initializeGraphNode(NodeType.E_VARIABLE, _graphBuilder._gvplGraph, this, _astInterpreter, startingLine);
			addMember(member_var, member.getMemberId());

			if (member_var instanceof ClassVar) {
				ClassVar structVarDecl = (ClassVar) member_var;
				for (Map.Entry<MemberId, Var> entry : structVarDecl.getInternalVariables()
						.entrySet()) {
					addMember((Var) entry.getValue(), entry.getKey());
				}
			}
		}
	}
	
	private void addMember(Var var, MemberId id) {
		_varFromMembersMap.put(id, var);
		_memberFromVar.put(var, id);
	}

	protected String calcName() {
		return _parentClass.getName() + "::" + _funcName;
	}

	@Override
	public IBinding load(IASTFunctionDefinition fd) {
		IBinding result = super.load(fd);
		if(_funcName.equals(_parentClass.getName()))
			_parentClass.setConstructorFunc(this);

		Graph.getAccessedVars(_graphBuilder._gvplGraph, _writtenVars, _readVars);
		
		return result;
	}
	
	@Override
	void loadConstructorChain(ICPPASTConstructorChainInitializer[] constructorInit) {
		for(ICPPASTConstructorChainInitializer initializer : constructorInit) {
			int startingLine = initializer.getFileLocation().getStartingLineNumber();
			IASTExpression expr = initializer.getInitializerValue();
			InstructionLine instructionLine = new InstructionLine(_graphBuilder, this, _cppMaps, _astInterpreter);
			List<FuncParameter> parameters = instructionLine.loadFunctionParameters(this, expr);

			IBinding member_binding = initializer.getMemberInitializerId().resolveBinding();
			ClassMember classMember = _parentClass.getMember(member_binding);
			MemberId memberId = classMember.getMemberId();
			Var var = _varFromMembersMap.get(memberId);
			var.constructor(parameters, NodeType.E_VARIABLE, _graphBuilder._gvplGraph, this, 
					_astInterpreter, startingLine);
		}
	}
	
	@Override
	/**
	 * Returns the DirectVarDecl of the reference to a variable
	 * @return The DirectVarDecl of the reference to a variable
	 */
	public Var getVarOfReference(IASTExpression expr) {

		IASTIdExpression id_expr = null;
		if (expr instanceof IASTIdExpression)
			id_expr = (IASTIdExpression) expr;
		else
			ErrorOutputter.fatalError("problem here");

		// Check if the variable is declared inside the own block
		Var var_decl = getVarDeclOfLocalReference(id_expr);
		if (var_decl != null)
			return var_decl;
		// Ok, if the function did not returned until here, the variable is a
		// member.

		IASTName name = id_expr.getName();
		IBinding binding = name.resolveBinding();
		ClassMember structMember = _parentClass.getMember(binding);
		MemberId lhs_member_id = structMember.getMemberId();

		Var direct_var_decl = _varFromMembersMap.get(lhs_member_id);

		return direct_var_decl;
	}

	/**
	 * Copy the internal graph to the main graph and bind the variables of the
	 * structure to the used variables in the member function
	 * 
	 * @param classVar
	 * @param graphBuilder
	 */
	public GraphNode loadMemberFuncRef(ClassVar classVar,
			List<FuncParameter> parameter_values, Graph graph, int startingLine) {
		Map<GraphNode, GraphNode> map = graph.addSubGraph(
				_graphBuilder._gvplGraph, startingLine);
		
		for (Var readVar : _readVars) {
			GraphNode firstNode = readVar.getFirstNode();
			GraphNode firstNodeInExtGraph = map.get(firstNode);
			MemberId memberId = _memberFromVar.get(readVar);
			Var memberInstance = classVar.findMember(memberId);
			if(memberInstance == null)
				continue;
			GraphNode miNode = memberInstance.getCurrentNode(startingLine);
			miNode.addDependentNode(firstNodeInExtGraph, startingLine);
		}

		for (Var writtenVar : _writtenVars) {
			GraphNode currNode = writtenVar.getCurrentNode(startingLine);
			GraphNode currNodeInExtGraph = map.get(currNode);
			MemberId memberId = _memberFromVar.get(writtenVar);
			Var memberInstance = classVar.findMember(memberId);
			if(memberInstance == null)
				continue;
			memberInstance.receiveAssign(graph, NodeType.E_VARIABLE, currNodeInExtGraph, startingLine);
		}

		return addParametersReferenceAndReturn(parameter_values, graph, map, startingLine);
	}

}
