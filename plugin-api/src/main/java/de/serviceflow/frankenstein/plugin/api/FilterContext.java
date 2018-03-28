package de.serviceflow.frankenstein.plugin.api;

public interface FilterContext {
	Object getValue(String Key);
	Object putValue(String Key, Object value);
}
