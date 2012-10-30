package gvpl.common;

import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;

public abstract class AstLoader {

	protected Graph _gvplGraph;
	
	public static IVar instanceVar(IndirectionType indirectionType, String name, TypeId typeId,
			Graph graph, AstLoader astLoader, AstInterpreter astInterpreter) {
		switch (indirectionType) {
		case E_VARIABLE:
			if (astInterpreter.isPrimitiveType(typeId))
				return new Var(graph, name, typeId);

			ClassDecl classDecl = astInterpreter.getClassDecl(typeId);
			return new ClassVar(graph, name, classDecl, astLoader);
		case E_POINTER:
			return new PointerVar(graph, name, typeId);
		case E_REFERENCE:
			return new ReferenceVar(graph, name, typeId);
		case E_INDIFERENT:
			{
				GeneralOutputter.fatalError("Not expected");
				return null;
			}
		}
		return null;
	}
	
	public IVar addVarDecl(String name, TypeId type) {
		return instanceVar(IndirectionType.E_VARIABLE, name, type, _gvplGraph, this, getAstInterpreter());
	}
	
	protected abstract AstInterpreter getAstInterpreter();

}
