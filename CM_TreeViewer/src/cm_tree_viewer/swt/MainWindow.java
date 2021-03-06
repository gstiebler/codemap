package cm_tree_viewer.swt;

import java.util.Observable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class MainWindow {

	Display _display = new Display();
	List _list = null;
	Tree _tree = null;
	Shell _shell = null;
	
	public Observable varClick = new Observable();
	
	public MainWindow() {
		_shell = new Shell(_display);
		
		createWidgets(_shell);
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
		data1.top = new FormAttachment(0, 5);
		data1.bottom = new FormAttachment(0, 200);
		_list.setLayoutData(data1);
		
		_tree = new Tree (shell, SWT.BORDER);

		FormData data2 = new FormData();
		data2.top = new FormAttachment(_list, 5);
		data2.left = new FormAttachment(0, 5);
		data2.right = new FormAttachment(100, -5);
		data2.bottom = new FormAttachment(100, -5);
		_tree.setLayoutData(data2);

		shell.setSize(700, 700);
	}
	
	public void showModal() {
		_shell.open ();
		while (!_shell.isDisposed ()) {
			if (!_display.readAndDispatch ()) 
				_display.sleep ();
		}
		_display.dispose ();
	}
	
	public void addVar(String name) {
		_list.add(name);
	}
	
	public Object clearTree(String rootText) {
		_tree.removeAll();
		TreeItem root = new TreeItem (_tree, 0);
		root.setText(rootText);
		return root;
	}
	
	public Object addTreeItem(Object parent, String text) {
		TreeItem pTreeItem = (TreeItem) parent;
		pTreeItem.setExpanded(true);
		TreeItem lItem = new TreeItem(pTreeItem, 0);
		lItem.setText(text);
		return lItem;
	}
	
	public void addVarClickListener(Listener listener) {
		_list.addListener(SWT.Selection, listener);
	}	
	
	public String getSelectedVar() {
		int [] selection = _list.getSelectionIndices ();
		return _list.getItem(selection[0]);
	}
}
