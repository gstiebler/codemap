package gvpl.common;

public class CodeLocation implements Comparable<CodeLocation> {
	
	static String _currentFileName;

	String _fileName;
	int _startingLine;
	int _offset;
	
	public CodeLocation(String fileName, int startingLine, int offset) {
		_fileName = normalizeFileName(fileName);
		_startingLine = startingLine;
		_offset = offset;
	}
	
	private static String normalizeFileName(String weirdFileName) {
		return weirdFileName.replace('\\', '/');
	}
	
	public int getStartingLine() {
		return _startingLine;
	}
	
	public String getFileName() {
		return _fileName;
	}
	
	public static void setCurrentFileName(String currentFileName) {
		_currentFileName = normalizeFileName(currentFileName);
	}
	
	@Override
	public String toString() {
		return _fileName + " - " + _startingLine;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof CodeLocation))
			return false;
		
		CodeLocation otherCL = (CodeLocation) other; 
		if(otherCL._offset != _offset)
			return false;
		
		if(otherCL._startingLine != _startingLine)
			return false;
			
		if(!otherCL._fileName.equals(_fileName))
			return false;
		
		return true;
	}

	@Override
	public int compareTo(CodeLocation other) {
		if(other._offset < _offset)
			return -1;
		if(other._offset > _offset)
			return 1;
		
		if(other._startingLine < _startingLine)
			return -1;
		if(other._startingLine > _startingLine)
			return 1;
		
		return other._fileName.compareTo(_fileName);
	}

}
