package de.serviceflow.frankenstein.plugin.api;

/**
 * Context of filters can be used access context information or to exchange
 * data, like results, between different filters in the pipeline.
 */
public interface FilterContext {
	Object getValue(String key);

	Object putValue(String key, Object value);
}
