package cm_visualization;

import gvpl.graph.Graph;
import gvpl.graph.GraphNode;
import gvpl.graphviz.IGraphOutput;
import gvpl.graphviz.Visualizer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

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
		try {
			FileInputStream fileIn = new FileInputStream(filePath);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			_visibleNodes = (Set<Integer>) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			_visibleNodes.add(1);
			saveVisibleNodesToFile(filePath);
			i.printStackTrace();
		} catch (ClassNotFoundException c) {
			c.printStackTrace();
		}
	}
	
	public void saveVisibleNodesToFile(String filePath) {
		try {
			FileOutputStream fileOut = new FileOutputStream(filePath);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(_visibleNodes);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}
	
	public void addGraphNode(Graph graph, int nodeId) {
		GraphNode node = graph.getNodeById(nodeId);
		for( GraphNode neighbourNode : node.getDependentNodes() )
			_visibleNodes.add(neighbourNode.getId());
		
		for( GraphNode neighbourNode : node.getSourceNodes() )
			_visibleNodes.add(neighbourNode.getId());
	}
}
