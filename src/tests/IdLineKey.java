package tests;


public class IdLineKey implements Comparable<IdLineKey> {

	String _id;
	int _startingLine;
	
	public IdLineKey(String id, int startingLine) {
		_id = id;
		_startingLine = startingLine;
	}
	
	@Override
	public int compareTo(IdLineKey other) {
		Integer thisSL = new Integer(_startingLine);
		Integer otherSL = new Integer(((IdLineKey)other)._startingLine);
		
		int slCompare = thisSL.compareTo(otherSL);
		if(slCompare != 0)
			return slCompare;
		
		return 0;
		//return _id.compareTo(((IdLineKey)other)._id);
	}
	
	@Override
	public boolean equals(Object otherObj) {
		IdLineKey other = (IdLineKey) otherObj;
		if(_startingLine != other._startingLine)
			return false;
		
		return true;
	}
	
}
