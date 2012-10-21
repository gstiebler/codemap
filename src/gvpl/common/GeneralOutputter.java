package gvpl.common;

public class GeneralOutputter {
	
	public static void warning(String error) {
		System.out.println(error);
	}

	public static void fatalError(String error) {
		System.out.println("FATAL ERROR!: " + error);
		System.exit(1);
	}

	public static void debug(String msg) {
		System.out.println( msg);
	}
	
}
