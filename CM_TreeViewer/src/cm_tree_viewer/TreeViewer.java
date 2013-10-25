package cm_tree_viewer;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import cm_tree_viewer.swt.MainWindow;
import gvpl.common.OutputManager;
import gvpl.common.OutputManager.NodesTree;
import gvpl.common.OutputManager.VarInfo;

public class TreeViewer {
	
	MainWindow _mainWindow = new MainWindow();
	OutputManager _outputManager = null;
	
	public static void main(String[] args) {
		(new TreeViewer()).visualizeTree(args[0]);
	}
	
	void visualizeTree(String fileName) {
		_outputManager = OutputManager.loadFromFile(fileName);

		java.util.List<VarInfo> vars = _outputManager.getVars();
		for (VarInfo varInfo : vars) 
			_mainWindow.addVar(varInfo.name);
		
		_mainWindow.addVarClickListener(new Listener() {
			public void handleEvent(Event arg0) {
				varClicked();
			}
		} );
		
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
	
	void varClicked() {
		String selectedVar = _mainWindow.getSelectedVar();
		
		NodesTree nodesTree = _outputManager.buildNodesTree(selectedVar);
		buildWindowTree(nodesTree);
	}
	
}
