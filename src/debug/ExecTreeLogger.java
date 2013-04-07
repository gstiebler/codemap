package debug;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExecTreeLogger {

	static ExecTreeLogger _instance = new ExecTreeLogger();
	static List<StackTraceElement> _lastStack = new ArrayList<StackTraceElement>();
	
	Document _doc = null;
	List<Element> _elementsStack = new ArrayList<Element>();
	
	public ExecTreeLogger(){

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			 
			// root elements
			_doc = docBuilder.newDocument();
			
			Element rootElement = _doc.createElement("log");
			_doc.appendChild(rootElement);
			_elementsStack.add(rootElement);
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void init() {
		_instance = new ExecTreeLogger();
	}
	
	public static void finish() {
		try {
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(_instance._doc);
			String file = System.getProperty("user.dir") + "/logs/debug_calling_tree.xml";
			StreamResult result = new StreamResult(new File(file));

			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void instanceLog(String args) {

		List<StackTraceElement> currStack = stackStrings();
		int numSpaces = 0;
		for(int i = 0; i < currStack.size(); ++i) {
			if (_lastStack.size() > i && currStack.get(i).equals(_lastStack.get(i))) {
				numSpaces++;
				continue; 
			}
			else
				break;
		}
		
		Element baseElement = _elementsStack.get(numSpaces);
		_elementsStack = _elementsStack.subList(0, numSpaces + 1);
		for(int i = numSpaces; i < currStack.size(); ++i) {
			StackTraceElement ste = currStack.get(i);
			
			Element childEl = _doc.createElement( ste.getMethodName() );
			childEl.setAttribute("file_name", ste.getFileName());
			childEl.setAttribute("class_name", ste.getClassName());
			childEl.setAttribute("line_number", Integer.toString(ste.getLineNumber()));
			childEl.setAttribute("args", args);
			baseElement.appendChild(childEl);
			// salary.appendChild(doc.createTextNode("100000"));
			
			_elementsStack.add(childEl);
			baseElement = childEl;
		}
		
		_lastStack = currStack;
	}
	
	public static void log(String args) {
		_instance.instanceLog(args);
	}
	
	static List<StackTraceElement> stackStrings() {
		List<StackTraceElement> result = new ArrayList<StackTraceElement>();
		int numDeepStackLines = 24;
		int numExecTreeLoggerLines = 4;
		StackTraceElement ste[] = Thread.currentThread().getStackTrace();
		for (int i = ste.length - numDeepStackLines; i >= numExecTreeLoggerLines; --i )
			result.add(ste[i]);
		return result;
	}
}
