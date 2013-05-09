package debug;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	static boolean _insideTests = true;
	
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

		Map<String, String> env = System.getenv();
		String ut = env.get("UNIT_TESTS");
		_insideTests = ut != null;
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
	
	boolean stackLineIsEqual( StackTraceElement ste1, StackTraceElement ste2 ) {
		if( !ste1.getMethodName().equals(ste2.getMethodName()) )
			return false;
		
		return ste1.getClassName().equals(ste2.getClassName());
	}
	
	String stripClassName(String fullClassName) {
		String[] strings = fullClassName.split("\\.");
		return strings[strings.length - 1];
	}
	
	void instanceLog(String args) {
		List<StackTraceElement> currStack = stackStrings();
		int numSpaces = 0;
		for(int i = 0; i < currStack.size(); ++i) {
			if (_lastStack.size() > i && stackLineIsEqual(currStack.get(i), _lastStack.get(i))) {
				numSpaces++;
				continue; 
			}
			else
				break;
		}
		
		if(currStack.size() == numSpaces)
			numSpaces--;
		
		Element baseElement = _elementsStack.get(numSpaces);
		_elementsStack = _elementsStack.subList(0, numSpaces + 1);
		for(int i = numSpaces; i < currStack.size(); ++i) {
			StackTraceElement ste = currStack.get(i);
			
			
			String label = stripClassName(ste.getClassName()) + "." + ste.getMethodName();
			label = label.replace('<', '.').replace('>', '.');
			Element childEl = _doc.createElement( label );
			//childEl.setAttribute("file_name", ste.getFileName());
			//childEl.setAttribute("class_name", ste.getClassName());
			//childEl.setAttribute("line_number", Integer.toString(ste.getLineNumber()));
			//childEl.setAttribute("args", args);
			childEl.appendChild(_doc.createTextNode(args));
			baseElement.appendChild(childEl);
			
			_elementsStack.add(childEl);
			baseElement = childEl;
		}
		
		_lastStack = currStack;
	}
	
	public static void log(String args) {
		if(_insideTests)
			_instance.instanceLog(args);
	}
	
	static List<StackTraceElement> stackStrings() {
		List<StackTraceElement> result = new ArrayList<StackTraceElement>();

		int numDeepStackLines = 0;
		int numExecTreeLoggerLines = 0;
		if( _insideTests )
		{
			numDeepStackLines = 25;
			numExecTreeLoggerLines = 4;
		}
		StackTraceElement ste[] = Thread.currentThread().getStackTrace();
		for (int i = ste.length - numDeepStackLines - 1; i >= numExecTreeLoggerLines; --i )
			result.add(ste[i]);
		return result;
	}
}
