package cm_visualization;

import gvpl.graph.Graph;
import gvpl.graph.GraphNode;
import gvpl.graphviz.IGraphOutput;
import gvpl.graphviz.Visualizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class VisualizerFilter extends Visualizer {
	
	private Set<Integer> _visibleNodes = new HashSet<Integer>();

	public VisualizerFilter(IGraphOutput graphOutput, String visibleNodesPath) {
		super(graphOutput);
		loadVisibleNodesFromFile(visibleNodesPath);
	}
	
	@Override
	protected boolean shouldPrintNode(GraphNode graphNode) {
		boolean nodeIsVisible = _visibleNodes.contains( graphNode.getId() );
		return hasNeighbours(graphNode) && nodeIsVisible;
	}
	
	@SuppressWarnings("unchecked")
	private void loadVisibleNodesFromFile(String filePath) {
		XStream xstream = new XStream(new StaxDriver());
		try {
			String xml = FileUtils.readFileToString(new File(filePath));
			_visibleNodes = (Set<Integer>)xstream.fromXML(xml);
		} catch (IOException e) {
			_visibleNodes.add(1);
			saveVisibleNodesToFile(filePath);
		}
	}
	
	public void saveVisibleNodesToFile(String filePath) {
		XStream xstream = new XStream(new StaxDriver());
		String xml = xstream.toXML(_visibleNodes);
		PrintWriter out = null;
		try {
			out = new PrintWriter(filePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.println(xml);
		
		out.close();
	}
	
	public void addGraphNode(Graph graph, int nodeId) {
		GraphNode node = graph.getNodeById(nodeId);
		for( GraphNode neighbourNode : node.getDependentNodes() )
			_visibleNodes.add(neighbourNode.getId());
		
		for( GraphNode neighbourNode : node.getSourceNodes() )
			_visibleNodes.add(neighbourNode.getId());
	}
}
