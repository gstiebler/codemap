package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;

public class CPPASTFileLocation implements org.eclipse.cdt.core.dom.ast.IASTFileLocation{

	int _line = -1;
	
	public CPPASTFileLocation(String line) {
		String[] firstBico = line.split("<");
		String[] secondBico = firstBico[1].split(">");
		String[] comma = secondBico[0].split(", ");
		if(comma[0].contains("col")) {
		} else if (comma[0].contains("line")) {
			
		} else {
			String[] tp = comma[0].split(":");
			_line = Integer.parseInt(tp[tp.length - 2]);
		}
	}
	
	@Override
	public IASTFileLocation asFileLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNodeLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNodeOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getEndingLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		return "nada.cpp";
	}

	@Override
	public int getStartingLineNumber() {
		return _line;
	}

}
