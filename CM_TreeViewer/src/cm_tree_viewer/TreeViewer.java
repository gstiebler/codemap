package cm_tree_viewer;

import gvpl.common.OutputManager;
import gvpl.common.OutputManager.VarInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeViewer {
	
	List _list = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		(new TreeViewer()).visualizeTree(args[0]);
	}
	
	void visualizeTree(String fileName) {
		OutputManager outputManager = OutputManager.loadFromFile(fileName);
		
		Display display = new Display ();
		Shell shell = new Shell (display);
		
		createWidgets(shell);

		java.util.List<VarInfo> vars = outputManager.getVars();
		for (VarInfo varInfo : vars) 
			_list.add(varInfo.name);

		shell.open ();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) 
				display.sleep ();
		}
		display.dispose ();
	}
	
	void createWidgets(Shell shell) {
		FormLayout layout = new FormLayout();
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		shell.setLayout(layout);
		
		_list = new List (shell, SWT.BORDER | SWT.V_SCROLL);
		FormData data1 = new FormData();
		data1.left = new FormAttachment(0, 5);
		data1.right = new FormAttachment(0, 150);
		_list.setLayoutData(data1);
		
		//Rectangle clientArea = shell.getClientArea ();
		//list.setBounds (clientArea.x, clientArea.y, 100, 100);
		_list.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				String string = "";
				int [] selection = _list.getSelectionIndices ();
				for (int i=0; i<selection.length; i++) string += _list.getItem(selection[i]) + " ";
				System.out.println ("Selection={" + string + "}");
			}
		});
		_list.addListener (SWT.DefaultSelection, new Listener () {
			public void handleEvent (Event e) {
				String string = "";
				int [] selection = _list.getSelectionIndices ();
				for (int i=0; i<selection.length; i++) string += selection [i] + " ";
				System.out.println ("DefaultSelection={" + string + "}");
			}
		});
		
		final Tree tree = new Tree (shell, SWT.BORDER);

		FormData data2 = new FormData();
		data2.top = new FormAttachment(_list, 5);
		data2.left = new FormAttachment(0, 5);
		data2.right = new FormAttachment(100, -5);
		tree.setLayoutData(data2);
		
		for (int i=0; i<4; i++) {
			TreeItem iItem = new TreeItem (tree, 0);
			iItem.setText ("TreeItem (0) -" + i);
			for (int j=0; j<4; j++) {
				TreeItem jItem = new TreeItem (iItem, 0);
				jItem.setText ("TreeItem (1) -" + j);
				for (int k=0; k<4; k++) {
					TreeItem kItem = new TreeItem (jItem, 0);
					kItem.setText ("TreeItem (2) -" + k);
					for (int l=0; l<4; l++) {
						TreeItem lItem = new TreeItem (kItem, 0);
						lItem.setText ("TreeItem (3) -" + l);
					}
				}
			}
		}
	}
}
