package gvpl.common;

import gvpl.cdt.AstLoaderCDT;
import gvpl.cdt.ClassDeclCDT;
import gvpl.graph.Graph;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IBinding;


/**
 * The class that holds information about the entire software being interpreted
 * 
 * @author stiebler
 * 
 */
public class AstInterpreter extends AstLoaderCDT {
	
	static Logger logger = LogManager.getLogger(AstLoaderCDT.class.getName());

	protected Map<TypeId, ClassDeclCDT> _typeIdToClass;
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

	public TypeId getPrimitiveType() {
		return _primitiveType;
	}

	@Override
	protected IVar getVarFromBinding(IBinding binding) {
		// TODO Global funcs??
		logger.fatal("not implemented (?)");
		return null;
	}

}
