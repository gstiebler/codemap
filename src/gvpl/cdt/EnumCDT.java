package gvpl.cdt;

import gvpl.common.IVar;
import gvpl.common.TypeId;
import gvpl.common.Value;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class EnumCDT {
	
	static Logger logger = LogManager.getLogger(InstructionLine.class.getName());

	public static void loadEnum( IASTEnumerationSpecifier enumSpec, AstInterpreterCDT astInterpreter ) {
		IASTEnumerator[] enumerators = enumSpec.getEnumerators();
		Graph graph = astInterpreter.getGraph();
		int counter = 0;
		for(IASTEnumerator enumerator : enumerators) {
			TypeId type = astInterpreter.getType(enumSpec);
			IASTName name = enumerator.getName();
			IVar var = BaseScopeCDT.addVarDecl(name.toString(), type,
					null, graph, null, astInterpreter);
			IBinding binding = name.resolveBinding();
			astInterpreter.addGlobalVar(binding, var);

			IASTExpression enumValExpr = enumerator.getValue();
			Value val;
			if( enumValExpr != null ) {
				InstructionLine il = new InstructionLine(graph, null, astInterpreter);
				val = il.loadValue(enumValExpr);
				String nodeName = val.getNode().getName();
				try {
					counter = Integer.decode(nodeName);
				} catch (Exception e) {
					logger.error( e );
				}
				counter++;
			} else {
				String strCounter = String.valueOf(counter++);
				GraphNode node = graph.addGraphNode(strCounter, NodeType.E_DIRECT_VALUE);
				val = new Value(node);
			}
			
			var.receiveAssign(NodeType.E_DIRECT_VALUE, val, graph);
		}
	}
	
}
