package cm_visualization;

import gvpl.common.CodeLocation;

class CodeLine implements Comparable<CodeLine> {
	public String fileName;
	public int line;
	
	public CodeLine(CodeLocation codeLoc) {
		fileName = codeLoc.getFileName();
		line = codeLoc.getStartingLine();
	}

	@Override
	public int compareTo(CodeLine other) {
		if( line < other.line )
			return -1;
		else if ( line > other.line )
			return 1;
		
		return other.fileName.compareTo(fileName);
	}
	
	@Override
	public boolean equals(Object otherCodeLine) {
		CodeLine other = (CodeLine) otherCodeLine;
		return other.line == line && other.fileName.compareTo(fileName) == 0;
	}
	
	@Override
	public String toString() {
		return fileName + " - " + line;
	}

}