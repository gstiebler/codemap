package tests;

import gvpl.graph.GraphNode;

import org.cesta.parsers.dot.DotTree;

class NodeMatch {
	GraphNode _gvplNode;
	DotTree.Node _gvNode;
	public NodeMatch(GraphNode gvplNode, DotTree.Node gvNode) {
		_gvplNode = gvplNode;
		_gvNode = gvNode;
	}
}