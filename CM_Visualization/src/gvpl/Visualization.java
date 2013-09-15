package gvpl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import gvpl.graph.Graph;

public class Visualization {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try
	      {
	         FileInputStream fileIn = new FileInputStream("C:/Projetos/GVPL/GraphGenerator/fixtures/basic/graph_ser.out");
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         Graph graph = (Graph) in.readObject();
	         in.close();
	         fileIn.close();
	      }catch(IOException i)
	      {
	         i.printStackTrace();
	         return;
	      }catch(ClassNotFoundException c)
	      {
	          System.out.println("Employee class not found");
	          c.printStackTrace();
	          return;
	       }
	}

}
