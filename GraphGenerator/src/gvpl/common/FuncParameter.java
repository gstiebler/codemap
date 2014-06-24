package gvpl.common;

import gvpl.cdt.function.Function;

public class FuncParameter {
	
	public enum IndirectionType {
		E_VARIABLE, E_POINTER, E_REFERENCE, E_FUNCTION_POINTER, E_INDIFERENT
	};
	
	private Value _value = null;
	private Function _functionPointer = null;
	private IndirectionType _indirectionType = null;
	
	public FuncParameter(Value value, IndirectionType type) {
		_value = value;
		_indirectionType = type;
	}
	
	// TODO remove this constructor
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
	
	public TypeId getTypeId() {
		if(_value == null)
			return AstInterpreter.getPrimitiveType();
		
		if(_value.getVar() == null)
			return AstInterpreter.getPrimitiveType();
		
		return _value.getVar().getType();
	}
	
	public Function getFunction() {
		return _functionPointer;
	}
	
	public boolean isEquivalent(FuncParameter other) {
		if(_indirectionType != other._indirectionType)
			return false;
	
		if(getTypeId() != other.getTypeId())
			return false;
		
		return true;
	}
}
