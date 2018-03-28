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
package de.serviceflow.frankenstein.fxml;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import de.serviceflow.frankenstein.Configuration;
import de.serviceflow.frankenstein.MovieProcessor;
import de.serviceflow.frankenstein.plugin.api.ConfigManager;
import de.serviceflow.frankenstein.plugin.api.NativeSegmentFilter;
import de.serviceflow.frankenstein.plugin.api.SegmentVideoFilter;
import de.serviceflow.frankenstein.vf.segment.BWFilter;
import de.serviceflow.frankenstein.vf.segment.GLExampleFilter;
import de.serviceflow.frankenstein.vf.segment.NativeExampleFilter;
import de.serviceflow.frankenstein.vf.segment.StereoDistanceFilter;
import de.serviceflow.frankenstein.vf.segment.VideoEqualizerFilter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import samplefilters.SampleFilter;

public class FxMain extends Application implements ConfigManager {

	private Stage theStage;
	private Scene configurationScene, processingScene;

	private ProcessingSceneController pController;
	private ConfigurationSceneController cController;

	private List<SegmentVideoFilter> segmentFilters;

	public static final String APP_NAME = "Frankenstein VR";

	public static ConfigManager configManager = null;

	private Configuration configuration;
	private static Configuration initialConfiguration;

	private static final double WIDTH = 1024.0;
	private static final double HEIGHT = 768.0;

	private static Locale locale = Locale.getDefault();

	public static void fxmain(Configuration c) {
		initialConfiguration = c;
		String[] args = {};
		launch(args);
	}

	public static ConfigManager getInstance() {
		return configManager;
	}

	public FxMain() {
		configManager = this;
	}

	@Override
	public void start(Stage primaryStage) {
		try {

			// https://github.com/openpnp/opencv
			try {
				Class.forName("nu.pattern.OpenCV").getMethod("loadShared", (Class<?>[]) null).invoke((Object[]) null,
						(Object[]) null);
				// nu.pattern.OpenCV.loadShared();
				// nu.pattern.OpenCV.loadLocal();
			} catch (ClassNotFoundException e) {
				System.out.println("WARNING: nu.pattern.OpenCV not found.");
			}
			System.out.println("Loading from " + System.getProperty("java.library.path"));
			System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);

			theStage = primaryStage;

			theStage.setTitle(APP_NAME);
			theStage.setMinHeight(HEIGHT);
			theStage.setMaxHeight(HEIGHT);
			theStage.setHeight(HEIGHT);
			theStage.setMinWidth(WIDTH);
			theStage.setMaxWidth(WIDTH);
			theStage.setWidth(WIDTH);

			buildUI();

			theStage.setScene(configurationScene);
			theStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void buildUI() {
		createSegmentFilters();

		BorderPane sceneRoot = null;
		FXMLLoader loader;

		PropertyResourceBundle bundleConfiguration = (PropertyResourceBundle) ResourceBundle
				.getBundle("de/serviceflow/frankenstein/bundles/configuration", locale);
		loader = new FXMLLoader(getClass().getResource("ConfigurationScene.fxml"), bundleConfiguration);
		try {
			sceneRoot = (BorderPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		configurationScene = new Scene(sceneRoot, WIDTH, HEIGHT);
		configurationScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		cController = (ConfigurationSceneController) loader.getController();
		cController.configure(this, theStage);

		PropertyResourceBundle bundleProcessing = (PropertyResourceBundle) ResourceBundle
				.getBundle("de/serviceflow/frankenstein/bundles/processing", locale);
		loader = new FXMLLoader(getClass().getResource("ProcessingScene.fxml"), bundleProcessing);
		try {
			sceneRoot = (BorderPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		processingScene = new Scene(sceneRoot, WIDTH, HEIGHT);
		processingScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		pController = (ProcessingSceneController) loader.getController();
		pController.configure(this, theStage);
	}

	public void showProcessing(Configuration configuration) {
		this.configuration = configuration;
		pController.initProcessing(cController, configuration);
		theStage.setScene(processingScene);
	}

	public void showConfigure() {
		Platform.runLater(() -> {
			theStage.sizeToScene();
			theStage.setScene(configurationScene);
		});
	}

	// public void startProcessing(Configuration configuration) {
	// pController.runProcessing(cController, configuration);
	// }

	// public void toConfiguration() {
	// theStage.setScene(configurationScene);
	// }

	public Configuration getConfiguration() {
		return configuration;
	}

	public Configuration getInitialConfiguration() {
		return initialConfiguration;
	}

	@Override
	public void stop() {
		// Stage is closing
		System.out.println("stopping");
		MovieProcessor.stop();
	}

	public void setDocumentInTitle(String name) {
		if (name != null)
			theStage.setTitle(APP_NAME + " - " + name);
		else
			theStage.setTitle(APP_NAME);
	}

	public Locale getLocale() {
		return locale;
	}

	public List<SegmentVideoFilter> getLocalFilters() {
		return segmentFilters;
	}

	public void createSegmentFilters() {
		segmentFilters = new ArrayList<SegmentVideoFilter>();

		try {
			segmentFilters.add(new BWFilter());
			segmentFilters.add(new StereoDistanceFilter());
			segmentFilters.add(new SampleFilter());
			segmentFilters.add(new GLExampleFilter());
		} catch (Throwable t) {
			t.printStackTrace();
		}

		// Filters with native proxy in jar
		try {
			segmentFilters.add(new NativeExampleFilter()); // try to load from
															// plugin jar
			segmentFilters.add(new VideoEqualizerFilter()); // try to load from
															// plugin jar
		} catch (Throwable t) {
			t.printStackTrace();
		}
/*
		// Filters completly in jar
		try {
			segmentFilters
					.add(loadExternalFilterInstance("de.serviceflow.frankenstein.vf.external.ExternalSampleFilter"));
		} catch (Throwable t) {
			t.printStackTrace();
		}
*/
	}

	private SegmentVideoFilter loadExternalFilterInstance(String filterClassName) {
		try {
			// use dynamic loading and reflection when loading jni proxy class
			// from jar, so app do not depend on it.
			URLClassLoader childLoader = getLoader();
			Class filterClass = Class.forName(filterClassName, true, childLoader);
			SegmentVideoFilter filter = (SegmentVideoFilter) filterClass.newInstance();
			Method filterInitMethod = filterClass.getMethod("init");
			filterInitMethod.invoke(filter);
			return filter;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
				| SecurityException | IllegalArgumentException | InvocationTargetException | MalformedURLException e) {
			throw new RuntimeException(
					"jni wrapper creation failed. Bug-Mining: Ensure the wrapper was added to the javahClassNames in pom.xml. Check NativeCode.h for existing and proper signatures.",
					e);
		}
	}

	static URLClassLoader loader = null;

	static synchronized URLClassLoader getLoader() throws MalformedURLException {
		if (loader == null) {
			final String RELEATIVE_TO_MAVEN_EXEC_CWD = "../../../target";
			String pluginpath = System.getProperty("pluginpath", RELEATIVE_TO_MAVEN_EXEC_CWD);
			File myJar = new File(new File(pluginpath), "plugin-opencv-0.3.1-SNAPSHOT.jar");
			URL[] urls = new URL[] { myJar.toURI().toURL() };
			loader = new URLClassLoader(urls, NativeSegmentFilter.class.getClassLoader());
		}
		return loader;
	}

}
