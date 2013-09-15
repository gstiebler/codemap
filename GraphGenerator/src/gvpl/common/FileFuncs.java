package gvpl.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileFuncs {
	
	//read file content into a string
	public static String readFileToString(String filePath) throws IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
 
		char[] buf = new char[10];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
 
		reader.close();
 
		return  fileData.toString();	
	}
	
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
