package de.serviceflow.frankenstein.plugin.api;

import java.util.Properties;

public class AbstractConfigController {
	private Properties props = new Properties();
	
	/**
	 * Properties of this instance.
	 * 
	 * @return Properties
	 */
	public Properties getProperties() {
		return props;
	}
}
