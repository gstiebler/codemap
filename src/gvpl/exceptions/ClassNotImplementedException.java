package gvpl.exceptions;

import gvpl.cdt.InstructionLine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClassNotImplementedException extends Exception{

	private static final long serialVersionUID = 1L;
	static Logger logger = LogManager.getLogger(InstructionLine.class.getName());

	public ClassNotImplementedException(String className) {
		logger.error("Class not implemented: " + className);
	}
}
