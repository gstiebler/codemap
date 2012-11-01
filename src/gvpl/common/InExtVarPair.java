package gvpl.common;

import gvpl.graph.Graph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class InExtVarPair {
	public IVar _in;
	public IVar _ext;
	
	public InExtVarPair(IVar in, IVar ext) {
		
		final Logger logger = LogManager.getLogger(Graph.class.getName());
		
		if(ext == null)
			logger.fatal("ext cannot be null");
		_in = in;
		_ext = ext;
	}
}