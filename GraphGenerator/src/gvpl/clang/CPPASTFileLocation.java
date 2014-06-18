package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class CPPASTFileLocation implements org.eclipse.cdt.core.dom.ast.IASTFileLocation{

	static Logger logger = LogManager.getLogger(CPPASTFileLocation.class.getName());

	int _line = -1;
	int _col = -1;
	String _file;
	IASTNode _parent;
	
	public CPPASTFileLocation(String line, IASTNode parent) {
		_parent = parent;
		
		if(line.contains("<invalid sloc>") || line.contains("<<<NULL>>>"))
			return;
		
		if(!line.contains("<"))
			return;
		
		String[] firstBico = line.split("<");
		String[] secondBico = firstBico[1].split(">");
		String[] comma = secondBico[0].split(", ");
		String text = comma[0];
		if(comma[0].contains("col")) {
			String[] tp = text.split(":");
			_col = Integer.parseInt(tp[1]);
		} else if (text.contains("line")) {
			String[] tp = text.split(":");
			_line = Integer.parseInt(tp[1]);
			_col = Integer.parseInt(tp[2]);
		} else {
			String[] tp = text.split(":");
			int lenthFile = tp.length - 2;
			_file = tp[0];
			for(int i = 1; i < lenthFile; i++)
				_file = _file + ":" + tp[i];
			
			CPPASTTranslationUnit.setFileName( _file );
			
			_line = Integer.parseInt(tp[lenthFile]);
			_col = Integer.parseInt(tp[lenthFile + 1]);
		}
		
		if(_file != null && _file.substring(0, 2).equals("./"))
			_file = _file.substring(2, _file.length());
	}
	
	public CPPASTFileLocation(String fileName) {
		_file = fileName;
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
		return getStartingLineNumber() * 1000 + _col;
	}

	@Override
	public int getEndingLineNumber() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public String getFileName() {
		if(_file != null)
			return _file;
		
		if(_parent == null)
			return CPPASTTranslationUnit.getFileName();
			
		return _parent.getFileLocation().getFileName();
	}

	@Override
	public int getStartingLineNumber() {
		if(_line >= 0)
			return _line;

		if(_parent == null)
		{
			logger.error("Unexpected null parent");
			return -1;
		}
		
		return _parent.getFileLocation().getStartingLineNumber();
	}

}
