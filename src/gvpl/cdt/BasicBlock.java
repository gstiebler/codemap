package gvpl.cdt;

import gvpl.common.ClassVar;
import gvpl.common.MemberId;
import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class BasicBlock extends AstLoader {
	
	private Map<Var, Var> _extToInVars = new HashMap<Var, Var>();

	public BasicBlock(AstLoader parent, AstInterpreter astInterpreter) {
		super(new Graph(-1), parent, astInterpreter);
	}

	public void load(IASTStatement baseStatement) {
		int startingLine = baseStatement.getFileLocation().getStartingLineNumber();
		IASTStatement[] statements = null;
		if (baseStatement instanceof IASTCompoundStatement)
			statements = ((IASTCompoundStatement) baseStatement).getStatements();
		else if (baseStatement instanceof IASTExpressionStatement) {
			statements = new IASTStatement[1];
			statements[0] = baseStatement;
		}

		for (IASTStatement statement : statements) {
			InstructionLine instructionLine = new InstructionLine(_gvplGraph, this, _astInterpreter);
			instructionLine.load(statement);
		}
		
		addToExtGraph(startingLine);
	}
	
	void addIf(GraphNode conditionNode, int startingLine) {
		/*if (conditionNode != null) {
			for (VarNodePair varNodePair : _writtenVar) {
				Var var = varNodePair._varDecl;
				_gvplGraph.addIf(var, var.getCurrentNode(startingLine), varNodePair._graphNode,
						conditionNode, null, startingLine);
			}
		}*/
	}
	
	void addToExtGraph(int startingLine) {
		Graph extGraph = _parent._gvplGraph;
		extGraph.merge(_gvplGraph);
		
		//TODO work with class vars also
		for (Map.Entry<Var, Var> entry : _extToInVars.entrySet()) {
			Var extVar = entry.getKey();
			Var intVar = entry.getValue();
			
			bindInVarsRecursive(extGraph, extVar, intVar, startingLine);
			
			bindOutVarsRecursive(extGraph, extVar, intVar, startingLine);
		}
	}
	
	private void bindInVarsRecursive(Graph extGraph, Var extVar, Var intVar, int startingLine) {
		if(extVar instanceof ClassVar) {
			ClassVar extClassVar = (ClassVar) extVar;
			ClassVar intClassVar = (ClassVar) intVar;
			for(MemberId memberId : extClassVar.getClassDecl().getMemberIds()) {
				Var memberExtVar = extClassVar.getMember(memberId);
				Var memberIntVar = intClassVar.getMember(memberId);
				bindInVarsRecursive(extGraph, memberExtVar, memberIntVar, startingLine);
			}
		} else {
			GraphNode intVarFirstNode = intVar.getFirstNode();
			// if someone read from internal var
			if (intVarFirstNode.getNumDependentNodes() > 0) {
				GraphNode extVarCurrNode = extVar.getCurrentNode(startingLine);
				extGraph.mergeNodes(extVarCurrNode, intVarFirstNode, startingLine);
			}
		}
	}
	
	private void bindOutVarsRecursive(Graph extGraph, Var extVar, Var intVar, int startingLine) {
		if(extVar instanceof ClassVar) {
			ClassVar extClassVar = (ClassVar) extVar;
			ClassVar intClassVar = (ClassVar) intVar;
			for(MemberId memberId : extClassVar.getClassDecl().getMemberIds()) {
				Var memberExtVar = extClassVar.getMember(memberId);
				Var memberIntVar = intClassVar.getMember(memberId);
				bindOutVarsRecursive(extGraph, memberExtVar, memberIntVar, startingLine);
			}
		} else {
			GraphNode intVarCurrNode = intVar.getCurrentNode(startingLine);
			//if someone has written in the internal var
			if(intVarCurrNode.getNumSourceNodes() > 0) {
				extVar.receiveAssign(NodeType.E_VARIABLE, intVarCurrNode, this, startingLine);
			}
		}
	}

	@Override
	protected Var getVarFromBinding(IASTExpression expr) {
		int startingLine = expr.getFileLocation().getStartingLineNumber();
		Var var = getVarFromBindingInternal(expr);

		if (var != null) 
			return var;
		else
		{
			Var superVar = super.getVarFromBinding(expr);
			if(_extToInVars.containsKey(superVar))
				return _extToInVars.get(superVar);
			
			//TODO review the null in the last parameter
			var = addVarDecl(superVar.getName(), superVar.getType(), null);
			var.initializeGraphNode(NodeType.E_VARIABLE, _gvplGraph, this, _astInterpreter, startingLine);
			_extToInVars.put(superVar, var);
			return var;
		}
	}

}
