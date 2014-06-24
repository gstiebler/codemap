package gvpl.common;

// TODO diferentiate each type of primitive types, and if the type is unsigned, long, etc.
/**
 * Class that serves to identify each type. The type can be a user defined class or a primitive type
 * @author gstiebler
 *
 */
public class TypeId implements java.io.Serializable {
	
	private static final long serialVersionUID = -3058979588525657326L;
	
	ClassDecl _parent;
	
	public TypeId(ClassDecl parent) {
		_parent = parent;
	}
	
	@Override
	public String toString() {
		if(_parent != null)
			return _parent.toString();
		else
			return "";
	}
}