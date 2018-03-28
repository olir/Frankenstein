package de.serviceflow.frankenstein.vf;

public interface FilterContext {
	Object getValue(String Key);
	Object putValue(String Key, Object value);
}
