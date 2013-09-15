package gvpl.cdt.function;

import gvpl.common.MemberId;

import java.util.ArrayList;
import java.util.List;

public class InitializedMembers {

	List<MemberId> _members = new ArrayList<MemberId>();
	
	public boolean contains(MemberId memberId) {
		return _members.contains(memberId);
	}
	
}
