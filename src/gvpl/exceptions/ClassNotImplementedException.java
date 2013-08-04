package gvpl.exceptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClassNotImplementedException extends Exception{

	private static final long serialVersionUID = 2716975096075783278L;
	static Logger logger = LogManager.getLogger(ClassNotImplementedException.class.getName());

	public ClassNotImplementedException(String className) {
		logger.error("Class not implemented: " + className);
	}
}
