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
package de.screenflow.frankenstein.vf.segment;

import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.opencv.core.Mat;

import de.screenflow.frankenstein.fxml.FxMain;
import de.screenflow.frankenstein.vf.SegmentVideoFilter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public abstract class DefaultSegmentFilter implements SegmentVideoFilter {
	private SegmentConfigController configController = null;

	private final String identifier;
	private final PropertyResourceBundle bundleConfiguration;

	protected DefaultSegmentFilter(String identifier) {
		this.identifier = identifier;

		bundleConfiguration = (PropertyResourceBundle) ResourceBundle
				.getBundle(getClass().getPackage().getName().replace('.', '/') + '/' + identifier, FxMain.getLocale());
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
		FXMLLoader loader = new FXMLLoader(getClass().getResource(identifier + ".fxml"), bundleConfiguration);
		try {
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException("Failed to create configuration scene for video filter '" + this + "'", e);
		}
		Scene scene = new Scene(loader.getRoot());
		scene.getStylesheets().add(stylesheet);
		configController = loader.getController();
		initializeController(); // custom initialization possible here
		return scene;
	}

	abstract protected void initializeController();

	public SegmentConfigController getConfigController() {
		return configController;
	}

	@Override
	public final Mat configure(Mat firstFrame) {
		return firstFrame;
	}
}
