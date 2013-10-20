package gvpl.common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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

	public static void saveToFile(Serializable serializable, String filePath) {
		try {
			FileOutputStream fileOut = new FileOutputStream(filePath);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(serializable);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}
	
	public static Serializable loadFromFile(String filePath) {
		try {
			FileInputStream fileIn = new FileInputStream(filePath);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Serializable obj = (Serializable) in.readObject();
			in.close();
			fileIn.close();
			return obj;
		} catch (IOException i) {
			i.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			c.printStackTrace();
			return null;
		}
	}
	
}
