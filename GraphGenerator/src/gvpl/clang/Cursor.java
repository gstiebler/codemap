package gvpl.clang;

import gvpl.common.FileFuncs;

import java.util.List;

public class Cursor {
	
	private List<String> _lines;
	private int _pos = 1;

	public Cursor(String astFileName) {
		try {
			_lines = FileFuncs.readLines(new java.io.File(astFileName));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String nextLine() {
		return _lines.get(_pos++);
	}
	
	public boolean theEnd() {
		return _pos >= _lines.size();
	}
	
	public static int indentation(String line) {
		return line.indexOf('-');
	}

	public void back() {
		_pos--;
	}
	
}
