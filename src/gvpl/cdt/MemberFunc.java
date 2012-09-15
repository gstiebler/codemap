package gvpl.cdt;

import gvpl.common.ClassMember;
import gvpl.common.ClassVar;
import gvpl.common.ErrorOutputter;
import gvpl.common.FuncParameter;
import gvpl.common.MemberId;
import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;

public class MemberFunc extends Function {

	private ClassDecl _parentClass;
	private Map<MemberId, Var> _varFromMembersMap = new HashMap<MemberId, Var>();
	//private Map<Var, MemberId> _memberFromVar = new HashMap<Var, MemberId>();

	public MemberFunc(ClassDecl parent, AstInterpreter astInterpreter, int startingLine) {
		super(new Graph(startingLine), null, astInterpreter);
		_parentClass = parent;

		//List<ClassMember> members = _parentClass.getMembers();
		// declare a variable for each member of the struct
		/*for (ClassMember member : members) {
			Var member_var = addVarDecl(member.getName(), member.getMemberType(), null);
			member_var.initializeGraphNode(NodeType.E_VARIABLE, _gvplGraph, this, _astInterpreter,
					startingLine);
			addMember(member_var, member.getMemberId());
		}*/
	}

	/*private void addMember(Var var, MemberId id) {
		_varFromMembersMap.put(id, var);
		_memberFromVar.put(var, id);
	}*/

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
			ClassMember classMember = _parentClass.getMember(member_binding);
			MemberId memberId = classMember.getMemberId();
			Var var = _varFromMembersMap.get(memberId);
			
			InstructionLine instructionLine = new InstructionLine(_gvplGraph, this, _astInterpreter);
			instructionLine.loadConstructorInitializer(var, expr, startingLine);
		}
	}

	@Override
	/**
	 * Returns the DirectVarDecl of the reference to a variable
	 * @return The DirectVarDecl of the reference to a variable
	 */
	public Var getVarFromExpr(IASTExpression expr) {

		IASTIdExpression id_expr = null;
		if (expr instanceof IASTIdExpression)
			id_expr = (IASTIdExpression) expr;
		else
			ErrorOutputter.fatalError("problem here");

		// Check if the variable is declared inside the own block
		Var var_decl = getLocalVarFromIdExpr(id_expr);
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
	public GraphNode loadMemberFuncRef(ClassVar classVar, List<FuncParameter> parameter_values,
			Graph graph, AstLoader astLoader, int startingLine) {
		Map<GraphNode, GraphNode> map = graph.addSubGraph(_gvplGraph, this, startingLine);

		List<InExtVarPair> readVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> writtenVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> ignoredVars = new ArrayList<InExtVarPair>();
		for (Map.Entry<List<IBinding>, Var> entry : _extToInVars.entrySet()) {
			getAccessedVars(entry.getValue(), entry.getKey(), readVars, writtenVars, ignoredVars, startingLine);
		}
		
		for(InExtVarPair readPair : readVars) {
			GraphNode firstNodeInNewGraph = map.get(readPair._in.getFirstNode());
			readPair._ext.getCurrentNode(startingLine).addDependentNode(firstNodeInNewGraph,
					astLoader, startingLine);
		}

		for(InExtVarPair writtenPair : writtenVars) {
			GraphNode currNodeInNewGraph = map.get(writtenPair._in.getCurrentNode(startingLine));
			writtenPair._ext.receiveAssign(NodeType.E_VARIABLE, currNodeInNewGraph, astLoader,
					startingLine);
		}

		return addParametersReferenceAndReturn(parameter_values, map, startingLine);
	}

}
