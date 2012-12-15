package tests;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cesta.parsers.dot.DotTree;

public class SubGraphNodes {
	public String _id;
	public List<Integer> _startingLines;
	
	/** <Label of the node, List of the nodes with this label, topologically ordered> */
	public Map<String, LinkedList<DotTree.Node>> _nodes = new LinkedHashMap<String, LinkedList<DotTree.Node>>();
	
	public Map<String, SubGraphNodes> _subGraphs = new LinkedHashMap<String, SubGraphNodes>();
}