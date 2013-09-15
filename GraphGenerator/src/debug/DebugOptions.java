package debug;

import gvpl.common.CodeLocation;

import java.util.ArrayList;
import java.util.List;

public class DebugOptions {

	static int _startingLine = -797;
	static List<Integer> _startingLineHistory = null;
	static String _currCpp;
	static CodeLocation _currCodeLocation;

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
			return new ArrayList<Integer>(_startingLineHistory);
		else {
			List<Integer> result = new ArrayList<Integer>();
			for(int i = size - 3; i < size; ++i)
				result.add(_startingLineHistory.get(i));
			
			return result;
		}
	}
	
	public static void resetLines() {
		_startingLineHistory = new ArrayList<Integer>();
	}
	
	public static void setCurrCpp(String currCpp) {
		_currCpp = currCpp;
	}

	public static String getCurrCpp() {
		return _currCpp;
	}
	
	public static void setCurrCodeLocation(CodeLocation codeLoc) {
		_currCodeLocation = codeLoc;
	}
	
	public static CodeLocation getCurrCodeLocation() {
		return _currCodeLocation;
	}
	
}
