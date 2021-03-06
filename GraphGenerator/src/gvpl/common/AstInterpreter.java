package gvpl.common;

import gvpl.cdt.BaseScopeCDT;
import gvpl.cdt.ClassDeclCDT;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The class that holds information about the entire software being interpreted
 * 
 * @author stiebler
 * 
 */
public abstract class AstInterpreter extends BaseScope {
	
	static Logger logger = LogManager.getLogger(BaseScopeCDT.class.getName());

	protected Map<TypeId, ClassDeclCDT> _typeIdToClass;
	/** the same for all primitive types */
	protected static TypeId _primitiveType = new TypeId(null);

	/**
	 * The base function. It all starts here.
	 * 
	 * @param gvplGraph
	 *            The almight graph
	 * @param root
	 *            The root of the program
	 */
	public AstInterpreter() {
		super(null);
		_typeIdToClass = new LinkedHashMap<TypeId, ClassDeclCDT>();
	}

	public ClassDeclCDT getClassDecl(TypeId type) {
		return _typeIdToClass.get(type);
	}

	public boolean isPrimitiveType(TypeId type) {
		return type.equals(_primitiveType);
	}

	public static TypeId getPrimitiveType() {
		return _primitiveType;
	}

}
