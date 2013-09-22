package gvpl.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class FileFuncs {
	
	public static List<String> readLines(java.io.File file) throws Exception {
		if (!file.exists()) {
			return new ArrayList<String>();
		}
		BufferedReader reader = new BufferedReader(new FileReader(file));
		List<String> results = new ArrayList<String>();
		String line = reader.readLine();
		while (line != null) {
			results.add(line);
			line = reader.readLine();
		}
		reader.close();
		return results;
	}
	
	public static String examplesPath(){
		return "K:\\Projetos\\GVPL\\exemplos\\";
	}

}
