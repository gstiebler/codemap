package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class Binding  {

	static Logger logger = LogManager.getLogger(Binding.class.getName());
	
	public static int getBindingId(IBinding binding) {
		if(binding == null) {
			logger.error("Binding null");
			return -1;
		}
		
		if(binding instanceof CPPField) {
			return ((CPPField)binding)._bi.bindingId;
		} else if(binding instanceof CPPMethod) {
			return ((CPPMethod)binding)._bindingId;
		} else if(binding instanceof CPPVariable) {
			return ((CPPVariable)binding)._bindingInfo.bindingId;
		} else if(binding instanceof CPPFunction) {
			return ((CPPFunction)binding)._bindingId;
		} else if(binding instanceof CPPClassType) {
			return ((CPPClassType)binding)._bindingInfo.bindingId;
		} else if(binding instanceof CPPEnumerator) {
			return ((CPPEnumerator)binding)._bindingInfo.bindingId;
		} else if(binding instanceof CPPTypedef) {
			return ((CPPTypedef)binding)._bindingInfo.bindingId;
		} else if(binding instanceof CPPClassTemplate) {
			return ((CPPClassTemplate)binding)._bindingInfo.bindingId;
		} else if(binding instanceof CPPClassInstance) {
			return ((CPPClassInstance)binding)._bindingInfo.bindingId;
		} else if(binding instanceof CPPClassSpecialization) {
			return ((CPPClassSpecialization)binding)._bindingInfo.bindingId;
		} else if(binding instanceof CPPEnumeration) {
			return ((CPPEnumeration)binding)._bindingInfo.bindingId;
		} else {
			logger.error("Class not found: {}", binding.getClass());
			return -1;
		}
	}

}
