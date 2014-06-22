package gvpl.common;


/** typedef */
public class TypeId implements java.io.Serializable {
	
	/**
	 * 
	 */
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