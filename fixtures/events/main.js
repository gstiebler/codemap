
var globalScriptManager = 0;

function signal_connect(objectPointer, eventStr, func, userData) {
	out.println('event1 added in javascript');
	if(eventStr == 'clicked')
		globalScriptManager.addEventFunc(func, {objectPointer, userData});
}

function main(scriptManager) {
	globalScriptManager = scriptManager;
	globalScriptManager.addFunction('gtk_signal_connect', signal_connect);
	return event1;
}
