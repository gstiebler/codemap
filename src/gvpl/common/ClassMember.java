package gvpl.common;


/**
 * Class member. Unique per class declaration
 */
public class ClassMember {
	private MemberId _id;
	private String _name;
	private TypeId _type;

	public ClassMember(MemberId id, String name, TypeId type) {
		_id = id;
		_name = name;
		_type = type;
	}

	public MemberId getMemberId() {
		return _id;
	}

	public TypeId getMemberType() {
		return _type;
	}

	public String getName() {
		return _name;
	}
}