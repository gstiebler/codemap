
importPackage(Packages.java.gvpl.common);

function event1(arg1, arg2) {
	out.println('event1 called in javascript');
}

function main(scriptManager) {
	out.println('main called in javascript');
	
	sm = gvpl.common.ScriptManager;
	scriptManager.nada();
	scriptManager.addFunction('event1', event1);
	return event1;
}
