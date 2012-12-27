
var globalScriptManager = 0;

function signal_connect(objectPointer, eventStr, func, userData) {
	out.println('event1 added in javascript');
	out.println(eventStr.getNode().getName());
	if(eventStr.getNode().getName() == '"clicked"') {
		var paramsArray = new Array(objectPointer, userData);
		globalScriptManager.addEventFunc(func, paramsArray);
	}
}

function main(scriptManager) {
	globalScriptManager = scriptManager;
	globalScriptManager.addFunction('gtk_signal_connect', signal_connect);
}