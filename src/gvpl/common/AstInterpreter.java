package gvpl.common;

import gvpl.cdt.AstLoaderCDT;
import gvpl.cdt.ClassDeclCDT;
import gvpl.graph.Graph;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * The class that holds information about the entire software being interpreted
 * 
 * @author stiebler
 * 
 */
public class AstInterpreter extends AstLoaderCDT {

	protected Map<TypeId, ClassDeclCDT> _typeIdToClass = new LinkedHashMap<TypeId, ClassDeclCDT>();
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
	}
	

	public ClassDeclCDT getClassDecl(TypeId type) {
		return _typeIdToClass.get(type);
	}

	public boolean isPrimitiveType(TypeId type) {
		return type.equals(_primitiveType);
	}

	public TypeId getPrimitiveType() {
		return _primitiveType;
	}

}
