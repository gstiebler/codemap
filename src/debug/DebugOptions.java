package debug;

public class DebugOptions {

	static int _startingLine = -797;

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

}
