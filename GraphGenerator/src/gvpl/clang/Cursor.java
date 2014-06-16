package gvpl.clang;

import gvpl.common.FileFuncs;

import java.util.List;

public class Cursor {
	
	private List<String> _lines;
	private int _pos = 1;
	private Cursor _parent = null;
	private boolean _theEnd = false;
	private int _firstIndent = -1;

	public Cursor(String astFileName) {
		try {
			_lines = FileFuncs.readLines(new java.io.File(astFileName));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Cursor opening file " + astFileName);
	}
	
	private Cursor(List<String> lines, int pos, Cursor parent) {
		_parent = parent;
		_lines = lines;
		_pos = pos;
		
		String firstLine = _lines.get(pos);
		_firstIndent = Cursor.indentation(firstLine);
	}
	
	public String getLine() {
		return _lines.get(_pos);
	}
	
	public String nextLine() {
		if(theEnd())
			return "";
		
		String result = _lines.get(_pos);
		setPos(_pos + 1);
		return result;
	}
	
	public boolean theEnd() {
		return _theEnd;
	}
	
	private static int indentation(String line) {
		return line.indexOf('-');
	}

	public void back() {
		_pos--;
	}
	
	public Cursor getSubCursor() {
		return new Cursor(_lines, _pos, this);
	}
	
	public Cursor getDetachedSubCursor() {
		return new Cursor(_lines, _pos, null);
	}
	
	private void setTheEnd() {
		_theEnd = true;
		if(_parent != null) {
			_parent.setPos(_pos);
		}
	}
	
	private void setPos(int pos) {
		_pos = pos;
		if (_pos >= _lines.size())
		{
			setTheEnd();
			return;
		}
		
		int currentIndent = Cursor.indentation(_lines.get(_pos));
		if (currentIndent <= _firstIndent)  
			setTheEnd();
	}
	
	public void runToTheEnd() {
		Cursor cursor = getSubCursor();
		while(!cursor.theEnd())
			cursor.nextLine();
	}
	
	public int getPos() {
		return _pos;
	}
	
	public String toString() {
		return _pos + " - " + getLine();
	}
}
