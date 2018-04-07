package de.serviceflow.frankenstein.plugin.api;

import java.util.Locale;

/**
 * Callback to Application Configuration
 */
public interface ConfigManager {
	/**
	 * Current active Locale.
	 * 
	 * @return Locale object
	 */
	Locale getLocale();
}
