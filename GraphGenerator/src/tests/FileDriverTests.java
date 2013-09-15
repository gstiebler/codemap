package tests;

import java.util.List;

import gvpl.graphviz.FileDriver;

public class FileDriverTests extends FileDriver {

	public List<PropertyPair> _properties;
	public String _nodeLabel;
	
	@Override
	protected void insertNode(int node_id, String nodeLabel, List<PropertyPair> properties){
		_properties = properties;
		_nodeLabel = nodeLabel;
	}
	
}
