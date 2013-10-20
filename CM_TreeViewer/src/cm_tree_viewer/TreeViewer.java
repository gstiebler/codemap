package cm_tree_viewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import gvpl.common.OutputManager;
import gvpl.common.OutputManager.VarInfo;

public class TreeViewer {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		visualizeTree(args[0]);
	}
	
	static void visualizeTree(String fileName) {
		OutputManager outputManager = OutputManager.loadFromFile(fileName);
		
		Display display = new Display ();
		Shell shell = new Shell (display);
		final List list = new List (shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		
		java.util.List<VarInfo> vars = outputManager.getVars();
		for (VarInfo varInfo : vars) 
			list.add(varInfo.name);
		
		Rectangle clientArea = shell.getClientArea ();
		list.setBounds (clientArea.x, clientArea.y, 100, 100);
		list.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				String string = "";
				int [] selection = list.getSelectionIndices ();
				for (int i=0; i<selection.length; i++) string += list.getItem(selection[i]) + " ";
				System.out.println ("Selection={" + string + "}");
			}
		});
		list.addListener (SWT.DefaultSelection, new Listener () {
			public void handleEvent (Event e) {
				String string = "";
				int [] selection = list.getSelectionIndices ();
				for (int i=0; i<selection.length; i++) string += selection [i] + " ";
				System.out.println ("DefaultSelection={" + string + "}");
			}
		});
		shell.pack ();
		shell.open ();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}
}
