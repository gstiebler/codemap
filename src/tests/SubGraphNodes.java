package tests;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.cesta.parsers.dot.DotTree;

public class SubGraphNodes {
	public String _name;
	
	/** <Label of the node, List of the nodes with this label, topologically ordered> */
	public Map<String, LinkedList<DotTree.Node>> _nodes = new HashMap<String, LinkedList<DotTree.Node>>();
	
	public Map<String, SubGraphNodes> _subGraphs = new HashMap<String, SubGraphNodes>();
}