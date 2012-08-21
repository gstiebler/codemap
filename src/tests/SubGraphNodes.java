package tests;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.cesta.parsers.dot.DotTree;

public class SubGraphNodes {
	public String _name;
	public int _startingLine;
	
	/** <Label of the node, List of the nodes with this label, topologically ordered> */
	public Map<String, LinkedList<DotTree.Node>> _nodes = new HashMap<String, LinkedList<DotTree.Node>>();
	
	public Map<NameLineKey, SubGraphNodes> _subGraphs = new TreeMap<NameLineKey, SubGraphNodes>();
}