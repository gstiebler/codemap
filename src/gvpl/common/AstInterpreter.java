package gvpl.common;

import gvpl.cdt.AstLoader;
import gvpl.cdt.ClassDecl;
import gvpl.cdt.CppMaps;
import gvpl.graph.Graph;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * The class that holds information about the entire software being interpreted
 * 
 * @author stiebler
 * 
 */
public class AstInterpreter extends AstLoader {

	protected Map<TypeId, ClassDecl> _typeIdToClass = new LinkedHashMap<TypeId, ClassDecl>();
	/** the same for all primitive types */
	protected TypeId _primitiveType = new TypeId();

	/**
	 * The base function. It all starts here.
	 * 
	 * @param gvplGraph
	 *            The almight graph
	 * @param root
	 *            The root of the program
	 */
	public AstInterpreter(Graph gvplGraph) {
		super(gvplGraph, null, null);
		CppMaps.initialize();
	}
	

	public ClassDecl getClassDecl(TypeId type) {
		return _typeIdToClass.get(type);
	}

	public boolean isPrimitiveType(TypeId type) {
		return type.equals(_primitiveType);
	}

	public TypeId getPrimitiveType() {
		return _primitiveType;
	}

}
