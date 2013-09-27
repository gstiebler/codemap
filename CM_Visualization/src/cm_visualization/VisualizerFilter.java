package cm_visualization;

import gvpl.graph.Graph;
import gvpl.graph.GraphNode;
import gvpl.graphviz.IGraphOutput;
import gvpl.graphviz.Visualizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class VisualizerFilter extends Visualizer {
	
	private TreeSet<CodeLine> _visibleNodes = new TreeSet<CodeLine>();

	public VisualizerFilter(IGraphOutput graphOutput, String visibleNodesPath, Graph graph) {
		super(graphOutput);
		loadVisibleNodesFromFile(visibleNodesPath, graph);
	}
	
	@Override
	protected boolean shouldPrintNode(GraphNode graphNode) {
		CodeLine cl = new CodeLine(graphNode.getCodeLocation());
		boolean nodeIsVisible = _visibleNodes.contains( cl );
		return hasNeighbours(graphNode) && nodeIsVisible;
	}
	
	@SuppressWarnings("unchecked")
	private void loadVisibleNodesFromFile(String filePath, Graph graph) {
		XStream xstream = new XStream(new StaxDriver());
		try {
			String xml = FileUtils.readFileToString(new File(filePath));
			_visibleNodes = (TreeSet<CodeLine>)xstream.fromXML(xml);
		} catch (IOException e) {
			GraphNode node = graph.getNodeById(1);
			_visibleNodes.add(new CodeLine(node.getCodeLocation()));
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
			_visibleNodes.add(new CodeLine(neighbourNode.getCodeLocation()));
		
		for( GraphNode neighbourNode : node.getSourceNodes() )
			_visibleNodes.add(new CodeLine(neighbourNode.getCodeLocation()));
	}
}
