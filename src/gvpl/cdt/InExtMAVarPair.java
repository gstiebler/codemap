package gvpl.cdt;

import gvpl.common.GeneralOutputter;
import gvpl.common.MemAddressVar;

public class InExtMAVarPair {
	public MemAddressVar _in;
	public MemAddressVar _ext;
	
	public InExtMAVarPair(MemAddressVar in, MemAddressVar ext) {
		if(ext == null)
			GeneralOutputter.fatalError("ext cannot be null");
		_in = in;
		_ext = ext;
	}
}