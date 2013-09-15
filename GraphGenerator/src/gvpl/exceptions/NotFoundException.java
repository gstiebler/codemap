package gvpl.exceptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NotFoundException extends Exception {

	private static final long serialVersionUID = -4650954653861435915L;
	static Logger logger = LogManager.getLogger(NotFoundException.class.getName());
	private String _itemName;

	public NotFoundException(String itemName) {
		_itemName = itemName;
		logger.error("Item {} not found.", itemName);
	}
	
	public String getItemName() {
		return _itemName;
	}
	
}
