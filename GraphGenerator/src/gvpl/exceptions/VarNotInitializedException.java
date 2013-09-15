package gvpl.exceptions;

import gvpl.common.IVar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VarNotInitializedException extends Exception {

	private static final long serialVersionUID = -5756702044764649145L;
	static Logger logger = LogManager.getLogger(VarNotInitializedException.class.getName());
	
	public VarNotInitializedException(IVar var) {
		logger.error("Var {} was not initialized.", var.getName());
	}

}
