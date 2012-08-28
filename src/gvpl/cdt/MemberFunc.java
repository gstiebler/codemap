package gvpl.cdt;

import gvpl.common.ClassMember;
import gvpl.common.ClassVar;
import gvpl.common.Var;
import gvpl.common.ErrorOutputter;
import gvpl.common.FuncParameter;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.MemberId;
import gvpl.graph.GraphNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class MemberFunc extends Function {

	private ClassDecl _parentLoadStruct;
	private Map<MemberId, Var> _varFromMembersMap = new HashMap<MemberId, Var>();
	private Map<Var, MemberId> _memberFromVar = new HashMap<Var, MemberId>();
	private Map<Var, MemberId> _writtenMembers = new HashMap<Var, MemberId>();
	private Map<Var, MemberId> _readMembers = new HashMap<Var, MemberId>();

	public MemberFunc(ClassDecl parent, CppMaps cppMaps, AstInterpreter astInterpreter, int startingLine) {
		super(new GraphBuilder(cppMaps), null, cppMaps, astInterpreter);
		_parentLoadStruct = parent;

		List<ClassMember> members = _parentLoadStruct.getMembers();
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

	protected String calcName(String internalName) {
		return _parentLoadStruct.getName() + "::" + internalName;
	}

	@Override
	public IBinding load(IASTFunctionDefinition fd) {
		return super.load(fd);
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
		ClassMember structMember = _parentLoadStruct.getMember(binding);
		MemberId lhs_member_id = structMember.getMemberId();

		Var direct_var_decl = _varFromMembersMap.get(lhs_member_id);

		return direct_var_decl;
	}

	@Override
	public void varWrite(Var var, int startingLine) {
		if (_parent != null)
			_parent.varWrite(var, startingLine);

		if (_memberFromVar.containsKey(var))
			_writtenMembers.put(var, _memberFromVar.get(var));
	}

	@Override
	public void varRead(Var var) {
		if (_parent != null)
			_parent.varRead(var);

		if (_memberFromVar.containsKey(var))
			_readMembers.put(var, _memberFromVar.get(var));
	}

	/**
	 * Copy the internal graph to the main graph and bind the variables of the
	 * structure to the used variables in the member function
	 * 
	 * @param structVarDecl
	 * @param graphBuilder
	 */
	public GraphNode loadMemberFuncRef(ClassVar structVarDecl,
			List<FuncParameter> parameter_values, GraphBuilder graphBuilder, int startingLine) {
		Map<GraphNode, GraphNode> map = graphBuilder._gvplGraph.addSubGraph(
				_graphBuilder._gvplGraph, this, startingLine);

		for (Map.Entry<Var, MemberId> entry : _readMembers.entrySet()) {
			Var DirectVarDecl = entry.getKey();
			GraphNode firstNode = DirectVarDecl.getFirstNode();
			GraphNode firstNodeInNewGraph = map.get(firstNode);
			Var memberInstance = structVarDecl.findMember(entry.getValue());
			memberInstance.getCurrentNode(startingLine).addDependentNode(firstNodeInNewGraph, this, startingLine);
		}

		for (Map.Entry<Var, MemberId> entry : _writtenMembers.entrySet()) {
			Var DirectVarDecl = entry.getKey();
			GraphNode currNode = DirectVarDecl.getCurrentNode(startingLine);
			GraphNode currNodeInNewGraph = map.get(currNode);
			Var memberInstance = structVarDecl.findMember(entry.getValue());
			memberInstance.receiveAssign(NodeType.E_VARIABLE, currNodeInNewGraph, null, startingLine);
		}

		return addParametersReferenceAndReturn(parameter_values, map, startingLine);
	}

}
