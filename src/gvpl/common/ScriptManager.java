package gvpl.common;

import gvpl.cdt.AstInterpreterCDT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class ScriptManager {
	Map<String, Function> _scriptFunctions = new LinkedHashMap<String, Function>();
	Context _cx;
	Scriptable _scope;
	Reader _javaScriptFile = null; 
	String _fileName;
	AstInterpreterCDT _interpreter;
	
	public ScriptManager(String path, AstInterpreterCDT interpreter) {
		_fileName = "main.js";
		_interpreter = interpreter;
		
		try {
			File file = new File(path + _fileName);
			if(!file.exists())
				return;
			_javaScriptFile = new FileReader(file);
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	
		_cx = Context.enter();
	    _scope = _cx.initStandardObjects();
	    
	    Object jsOut = Context.javaToJS(System.out, _scope);
	    ScriptableObject.putProperty(_scope, "out", jsOut);
	}
	
	public void execMainScript() {
		if(_javaScriptFile == null)
			return;
		
		try {
			_cx.evaluateReader(_scope, _javaScriptFile, _fileName, 1, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Function fct = (Function)_scope.get("main", _scope);
		Object[] funcArgs = {this};
		fct.call(_cx, _scope, null, funcArgs);
	}
	
	public void addFunction(String funcName, Function func) {
		_scriptFunctions.put(funcName, func);
	}
	
	public boolean functionExists(String name) {
		return _scriptFunctions.containsKey(name);
	}
	
	public void callFunc(String name, List<FuncParameter> parameterValues) {
		Function func = _scriptFunctions.get(name);
		Object[] jsParams = new Object [parameterValues.size()];
		for(int i = 0; i < parameterValues.size(); i++){
			Object jsOut = Context.javaToJS(parameterValues.get(i), _scope);
			jsParams[i] = jsOut;
		}
			
		func.call(_cx, _scope, null, jsParams);
	}
	
	public void addEventFunc(Object func, Object[] args) {
		System.out.println("todo: add function event");
		System.out.println(func);
		System.out.println(args);
	}
}
