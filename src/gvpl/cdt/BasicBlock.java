package gvpl.cdt;

import gvpl.common.ClassVar;
import gvpl.common.MemberId;
import gvpl.common.Var;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class BasicBlock extends AstLoader {
	
	class InExtVarPair {
		Var _in;
		Var _ext;
		
		public InExtVarPair(Var in, Var ext) {
			_in = in;
			_ext = ext;
		}
	}
	
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
		
		for (Map.Entry<Var, Var> entry : _extToInVars.entrySet()) {
			List<InExtVarPair> readVars = new ArrayList<InExtVarPair>();
			List<InExtVarPair> writtenVars = new ArrayList<InExtVarPair>();
			List<InExtVarPair> ignoredVars = new ArrayList<InExtVarPair>();
			getAccessedVars(extGraph, entry.getValue(), entry.getKey(), readVars, writtenVars, ignoredVars, startingLine);
			
			for(InExtVarPair readPair : readVars) {
				GraphNode intVarFirstNode = readPair._in.getFirstNode();
				// if someone read from internal var
				if (intVarFirstNode.getNumDependentNodes() > 0) {
					GraphNode extVarCurrNode = readPair._ext.getCurrentNode(startingLine);
					extGraph.mergeNodes(extVarCurrNode, intVarFirstNode, startingLine);
				}
			}
			
			for(InExtVarPair writtenPair : writtenVars) {
				GraphNode intVarCurrNode = writtenPair._in.getCurrentNode(startingLine);
				//if someone has written in the internal var
				if(intVarCurrNode.getNumSourceNodes() > 0) {
					writtenPair._ext.initializeGraphNode(NodeType.E_VARIABLE, extGraph, this, _astInterpreter, startingLine);
					GraphNode extVarCurrNode = writtenPair._ext.getCurrentNode(startingLine);
					extGraph.mergeNodes(extVarCurrNode, intVarCurrNode, startingLine);
				}
			}
			
			for(InExtVarPair ignoredPair : ignoredVars) {
				extGraph.removeNode(ignoredPair._in.getFirstNode());
			}
		}
	}
	
	private void getAccessedVars(Graph extGraph, Var intVar, Var extVar, List<InExtVarPair> read, List<InExtVarPair> written, List<InExtVarPair> ignored, int startingLine) {
		if(intVar instanceof ClassVar) {
			ClassVar extClassVar = (ClassVar) extVar;
			ClassVar intClassVar = (ClassVar) intVar;
			for(MemberId memberId : intClassVar.getClassDecl().getMemberIds()) {
				Var memberExtVar = extClassVar.getMember(memberId);
				Var memberIntVar = intClassVar.getMember(memberId);
				getAccessedVars(extGraph, memberIntVar, memberExtVar, read, written, ignored, startingLine);
			}
		} else {
			boolean accessed = false;
			GraphNode intVarFirstNode = intVar.getFirstNode();
			if (intVarFirstNode.getNumDependentNodes() > 0) {
				read.add(new InExtVarPair(intVar, extVar));
				accessed = true;
			}
			
			GraphNode intVarCurrNode = intVar.getCurrentNode(startingLine);
			if(intVarCurrNode.getNumSourceNodes() > 0) {
				written.add(new InExtVarPair(intVar, extVar));
				accessed = true;
			}
			
			if(!accessed)
				ignored.add(new InExtVarPair(intVar, extVar));
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
