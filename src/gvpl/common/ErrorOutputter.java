package gvpl.common;

public class ErrorOutputter {
	
	public static void warning(String error) {
		System.out.println(error);
	}

	public static void fatalError(String error) {
		System.out.println("FATAL ERROR!: " + error);
	}
	
}
