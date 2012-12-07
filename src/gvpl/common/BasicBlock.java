package gvpl.common;

import java.util.List;

public abstract class BasicBlock {
	
	
	public abstract void getAccessedVars(List<InExtVarPair> read, List<InExtVarPair> written,
			List<InExtVarPair> ignored, InToExtVar inToExtMap);
}
