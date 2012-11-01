package gvpl.cdt;

import gvpl.common.MemAddressVar;
import gvpl.graph.Graph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InExtMAVarPair {
	
	static Logger logger = LogManager.getLogger(Graph.class.getName());
	
	public MemAddressVar _in;
	public MemAddressVar _ext;
	
	public InExtMAVarPair(MemAddressVar in, MemAddressVar ext) {
		if(ext == null)
			logger.fatal("ext cannot be null");
		_in = in;
		_ext = ext;
	}
}