package cm_tree_viewer;

import cm_tree_viewer.swt.MainWindow;
import gvpl.common.OutputManager;
import gvpl.common.OutputManager.NodesTree;
import gvpl.common.OutputManager.VarInfo;

public class TreeViewer {
	
	MainWindow _mainWindow = new MainWindow();
	
	public static void main(String[] args) {
		(new TreeViewer()).visualizeTree(args[0]);
	}
	
	void visualizeTree(String fileName) {
		OutputManager outputManager = OutputManager.loadFromFile(fileName);

		java.util.List<VarInfo> vars = outputManager.getVars();
		for (VarInfo varInfo : vars) 
			_mainWindow.addVar(varInfo.name);
		
		NodesTree nodesTree = outputManager.buildNodesTree("A");
		buildWindowTree(nodesTree);
		
		_mainWindow.showModal();
	}
	
	void buildWindowTree(NodesTree nodesTree) {
		Object treeRoot = _mainWindow.clearTree(nodesTree.text);
		buildWindowTreeRecursive(nodesTree, treeRoot);
	}
	
	void buildWindowTreeRecursive(NodesTree nodesTree, Object parentTreeItem) {
		for(NodesTree localNodesTree : nodesTree.children) {
			Object localParentTreeItem = _mainWindow.addTreeItem(parentTreeItem, localNodesTree.text);
			buildWindowTreeRecursive(localNodesTree, localParentTreeItem);
		}
	}
	
}
