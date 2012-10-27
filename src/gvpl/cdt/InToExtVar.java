package gvpl.cdt;

import gvpl.common.IVar;
import gvpl.graph.Graph;

import java.util.LinkedHashMap;
import java.util.Map;

public class InToExtVar {
	private Map<IVar, IVar> _inToExtVar = new LinkedHashMap<IVar, IVar>();
	Graph _extGraph;
	
	public InToExtVar(Graph extGraph) {
		_extGraph = extGraph;
	}
	
	public IVar get(IVar internalVar) {
		return _inToExtVar.get(internalVar);
	}
	
	public void put(IVar internalVar, IVar externalVar) {
		_inToExtVar.put(internalVar, externalVar);
	}
	
	public Graph getExtGraph() {
		return _extGraph;
	}
}