package debug;

import gvpl.common.CodeLocation;

public class DebugOptions {

	static int _startingLine = -797;
	static CodeLocation _currentCodeLocation= null;

	public static boolean printDotSrcVarId() {
		return false;
	}

	public static boolean printDotSrcNodes() {
		return false;
	}

	public static void setStartingLine(int startingLine) {
		_startingLine = startingLine;
	}

	public static int getStartingLine() {
		return _startingLine;
	}
	
	public static void setCurrentCodeLocation(CodeLocation currCodeLocation) {
		_currentCodeLocation = currCodeLocation;
	}
	
	public static CodeLocation getCurrentCodeLocation() {
		return _currentCodeLocation;
	}

}
