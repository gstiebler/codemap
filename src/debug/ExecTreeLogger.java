package debug;

import java.util.ArrayList;
import java.util.List;

public class ExecTreeLogger {

	static ExecTreeLogger _instance = new ExecTreeLogger();
	static List<String> _lastStack = new ArrayList<String>();
	
	public ExecTreeLogger(){
		
	}
	
	public static void init() {
		_instance = new ExecTreeLogger();
	}
	
	public static void log() {
		List<String> currStack = stackStrings();
		int numSpaces = 0;
		String spaces = "";
		for(int i = 0; i < currStack.size(); ++i) {
			if (_lastStack.size() > i && currStack.get(i).equals(_lastStack.get(i))) {
				numSpaces++;
				// TODO generate a string of spaces
				spaces = spaces.concat(" ");
				continue; 
			}
			else
				break;
		}
		
		for(int i = numSpaces; i < currStack.size(); ++i) {
			String tmp = spaces + currStack.get(i);
			System.out.println(tmp);
			spaces = spaces.concat(" ");
		}
		
		_lastStack = currStack;
	}
	
	static List<String> stackStrings() {
		List<String> result = new ArrayList<String>();
		int numDeepStackLines = 24;
		int numExecTreeLoggerLines = 3;
		StackTraceElement ste[] = Thread.currentThread().getStackTrace();
		for (int i = ste.length - numDeepStackLines; i >= numExecTreeLoggerLines; --i )
			result.add(ste[i].toString());
		return result;
	}
}
