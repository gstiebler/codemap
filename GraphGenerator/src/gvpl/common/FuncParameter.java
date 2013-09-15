package gvpl.common;

import gvpl.cdt.function.Function;

public class FuncParameter {
	
	public enum IndirectionType {
		E_VARIABLE, E_POINTER, E_REFERENCE, E_FUNCTION_POINTER, E_INDIFERENT
	};
	
	private Value _value = null;
	private Function _functionPointer = null;
	private IndirectionType _indirectionType = null;
	int _type = -1;
	
	public FuncParameter(Value value, IndirectionType type) {
		_value = value;
		_indirectionType = type;
	}
	
	public FuncParameter(IndirectionType type) {
		_indirectionType = type;
	}
	
	public FuncParameter(Function functionPointer) {
		_functionPointer = functionPointer;
		_indirectionType = IndirectionType.E_FUNCTION_POINTER;
	}

	public Value getValue() {
		return _value;
	}
	
	public IndirectionType getType() {
		return _indirectionType;
	}
	
	public void setType(int type) {
		_type = type;
	}
	
	public Function getFunction() {
		return _functionPointer;
	}
	
	//TODO improve
	public boolean isEquivalent(FuncParameter other) {
		if(_indirectionType != other._indirectionType)
			return false;
		
		if(_type != other._type)
			return false;
		
		return true;
	}
}
