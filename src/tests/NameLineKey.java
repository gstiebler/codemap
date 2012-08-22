package tests;


public class NameLineKey implements Comparable<NameLineKey> {

	String _name;
	int _startingLine;
	
	public NameLineKey(String name, int startingLine) {
		_name = name;
		_startingLine = startingLine;
	}
	
	@Override
	public int compareTo(NameLineKey other) {
		Integer thisSL = new Integer(_startingLine);
		Integer otherSL = new Integer(((NameLineKey)other)._startingLine);
		
		int slCompare = thisSL.compareTo(otherSL);
		if(slCompare != 0)
			return slCompare;
		
		return _name.compareTo(((NameLineKey)other)._name);
	}
	
	@Override
	public boolean equals(Object otherObj) {
		NameLineKey other = (NameLineKey) otherObj;
		if(_startingLine != other._startingLine)
			return false;
		
		return _name.equals(other._name);
	}
	
}
