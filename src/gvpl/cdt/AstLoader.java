package gvpl.cdt;

import gvpl.graph.GraphBuilder.FuncId;
import gvpl.graph.GraphBuilder.TypeId;
import gvpl.graph.GraphBuilder.VarDecl;
import gvpl.graph.GraphBuilder.VarId;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;

public interface AstLoader {
	
	TypeId getType(IASTDeclSpecifier decl_spec);
	
	VarDecl getVarDecl(IASTExpression expr);
	
	FuncId getFuncId(IBinding binding);
	
	void addVarDecl(IBinding binding, VarId id);
}
