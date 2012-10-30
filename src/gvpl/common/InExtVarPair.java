package gvpl.common;


public class InExtVarPair {
	public IVar _in;
	public IVar _ext;
	
	public InExtVarPair(IVar in, IVar ext) {
		if(ext == null)
			GeneralOutputter.fatalError("ext cannot be null");
		_in = in;
		_ext = ext;
	}
}