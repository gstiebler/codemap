package cm_tree_viewer;

import cm_tree_viewer.swt.MainWindow;
import gvpl.common.OutputManager;
import gvpl.common.OutputManager.VarInfo;

public class TreeViewer {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		(new TreeViewer()).visualizeTree(args[0]);
	}
	
	void visualizeTree(String fileName) {
		OutputManager outputManager = OutputManager.loadFromFile(fileName);
		
		MainWindow mainWindow = new MainWindow();

		java.util.List<VarInfo> vars = outputManager.getVars();
		for (VarInfo varInfo : vars) 
			mainWindow.addVar(varInfo.name);
		
		mainWindow.showModal();
	}
	
	
}
