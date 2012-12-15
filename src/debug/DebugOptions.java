package debug;

import java.util.ArrayList;
import java.util.List;

public class DebugOptions {

	static int _startingLine = -797;
	static List<Integer> _startingLineHistory = new ArrayList<Integer>();

	public static boolean printDotSrcVarId() {
		return false;
	}

	public static boolean printDotSrcNodes() {
		return false;
	}

	public static void setStartingLine(int startingLine) {
		_startingLine = startingLine;
		_startingLineHistory.add(startingLine);
	}

	public static int getStartingLine() {
		return _startingLine;
	}
	
	public static List<Integer> lastVisitedLines() {
		int size = _startingLineHistory.size();
		if(size < 3)
			return _startingLineHistory;
		else {
			List<Integer> result = new ArrayList<Integer>();
			for(int i = size - 3; i < size; ++i)
				result.add(_startingLineHistory.get(i));
			
			return result;
		}
	}

}
