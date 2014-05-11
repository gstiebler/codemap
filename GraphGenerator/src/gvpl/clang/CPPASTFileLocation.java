package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;

public class CPPASTFileLocation implements org.eclipse.cdt.core.dom.ast.IASTFileLocation{

	static Logger logger = LogManager.getLogger(ASTNode.class.getName());

	int _line = -1;
	
	public CPPASTFileLocation(String line) {
		String[] firstBico = line.split("<");
		String[] secondBico = firstBico[1].split(">");
		String[] comma = secondBico[0].split(", ");
		if(comma[0].contains("col")) {
		} else if (comma[0].contains("line")) {
			String[] tp = comma[0].split(":");
			_line = Integer.parseInt(tp[1]);
		} else {
			String[] tp = comma[0].split(":");
			_line = Integer.parseInt(tp[tp.length - 2]);
		}
	}
	
	@Override
	public IASTFileLocation asFileLocation() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public int getNodeLength() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public int getNodeOffset() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public int getEndingLineNumber() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return "nada.cpp";
	}

	@Override
	public int getStartingLineNumber() {
		return _line;
	}

}
