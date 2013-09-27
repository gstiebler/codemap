package gvpl.common;

public class CodeLocation implements Comparable<CodeLocation>, java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8037068295401443662L;
	
	static String _currentFileName;
	static CodeLocation _lastCodeLocation;

	String _fileName;
	int _startingLine;
	int _offset;
	
	public CodeLocation(String fileName, int startingLine, int offset) {
		if(fileName.equals("<text>"))
			_fileName = _currentFileName;
		else
			_fileName = normalizeFileName(fileName);
		_startingLine = startingLine;
		_offset = offset;
		
		_lastCodeLocation = this;
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
	
	public static String getCurrentFileName() {
		return _currentFileName;
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
	
	public static CodeLocation getLastCodeLocation() {
		return _lastCodeLocation;
	}

}
