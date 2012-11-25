package gvpl.common;

public class CodeLocation {
	
	int _startingLine;
	String _fileName;
	
	public CodeLocation(String fileName, int startingLine) {
		_fileName = fileName;
		_startingLine = startingLine;
	}
	
	public int getStartingLine() {
		return _startingLine;
	}
	
	public String getFileName() {
		return _fileName;
	}
	
	public boolean equals(CodeLocation other) {
		if(other._startingLine != _startingLine)
			return false;
			
		if(!other._fileName.equals(_fileName))
			return false;
		
		return true;
	}

}
