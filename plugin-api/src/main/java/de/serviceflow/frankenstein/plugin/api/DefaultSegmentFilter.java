/*
 * Copyright 2017 Oliver Rode, https://github.com/olir/Frankenstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.serviceflow.frankenstein.plugin.api;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public abstract class DefaultSegmentFilter implements SegmentVideoFilter {
	private SegmentConfigController configController = null;

	private final String identifier;
	private final PropertyResourceBundle bundleConfiguration;

	protected DefaultSegmentFilter(String identifier) {
		this.identifier = identifier;

		bundleConfiguration = (PropertyResourceBundle) ResourceBundle.getBundle(
				this.getClass().getPackage().getName().replace('.', '/') + '/' + identifier, getLocale(),
				this.getClass().getClassLoader());
	}

	private Locale getLocale() {
		return getConfigManager().getLocale();
	}

	protected ConfigManager getConfigManager() {
		Class<?> fxMain;
		try {
			fxMain = Class.forName("de.serviceflow.frankenstein.fxml.FxMain");
			Class<?> parameterTypes[] = {};
			Method main = fxMain.getDeclaredMethod("getInstance", parameterTypes);
			Object[] invokeArgs = {};
			return (ConfigManager) main.invoke(fxMain, invokeArgs);
		} catch (Throwable e) {
			throw new Error(e);
		}
	}

	public final String toString() {
		return bundleConfiguration.getString("name");
	}

	@Override
	public final SegmentVideoFilter createInstance() {
		try {
			return getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public final Scene createConfigurationScene(String stylesheet) {
		FXMLLoader loader = new FXMLLoader(this.getClass().getResource(identifier + ".fxml"), bundleConfiguration);
		configController = instantiateController();
		loader.setController(configController);
		try {
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException("Failed to create configuration scene for video filter '" + this + "'", e);
		}
		Scene scene = new Scene(loader.getRoot());
		scene.getStylesheets().add(stylesheet);
		// configController = loader.getController();
		initializeController(); // custom initialization possible here
		return scene;
	}

	abstract protected SegmentConfigController instantiateController();

	abstract protected void initializeController();

	public SegmentConfigController getConfigController() {
		return configController;
	}
}
