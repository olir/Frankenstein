package de.serviceflow.frankenstein.vf;

import java.util.HashMap;

import de.serviceflow.frankenstein.plugin.api.FilterContext;

public class DefaultFilterContext implements FilterContext {
	private HashMap<String,Object> valMap = new HashMap<String,Object>();

	public DefaultFilterContext() {
	}

	@Override
	public Object getValue(String key) {
		return valMap.get(key);
	}

	@Override
	public Object putValue(String key, Object value) {
		return valMap.put(key, value);
	}
}
