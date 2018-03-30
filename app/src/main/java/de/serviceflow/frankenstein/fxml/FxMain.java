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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import de.serviceflow.frankenstein.Configuration;
import de.serviceflow.frankenstein.MovieProcessor;
import de.serviceflow.frankenstein.plugin.api.ConfigManager;
import de.serviceflow.frankenstein.plugin.api.NativeSegmentFilter;
import de.serviceflow.frankenstein.plugin.api.SegmentVideoFilter;
import de.serviceflow.frankenstein.vf.segment.BWFilter;
import de.serviceflow.frankenstein.vf.segment.StereoDistanceFilter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

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
				System.out.println("Loading from " + System.getProperty("java.library.path"));
				System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
			} catch (ClassNotFoundException e) {
				System.out.println("WARNING: nu.pattern.OpenCV not found.");
			} catch (Throwable t) {
				System.out.println("WARNING: nu.pattern.OpenCV not loaded.");
			}

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


	private SegmentVideoFilter loadExternalFilterInstance(String filterClassName, String ref) {
		try {
			// use dynamic loading and reflection when loading jni proxy class
			// from jar, so app do not depend on it.
			System.out.println("loading " + filterClassName + " from " + ref + " ...");
			URLClassLoader childLoader = getLoader(ref);
			Class filterClass = Class.forName(filterClassName, true, childLoader);
			SegmentVideoFilter filter = (SegmentVideoFilter) filterClass.newInstance();
			return filter;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | MalformedURLException
				| SecurityException | IllegalArgumentException /* | InvocationTargetException | NoSuchMethodException */ e) {
			throw new RuntimeException(
					"Failed loading filter " + filterClassName + " from " + ref,
					e);
		}
	}

	static Map<String, URLClassLoader> loaders = new HashMap<String, URLClassLoader>();

	static synchronized URLClassLoader getLoader(String ref) throws MalformedURLException {
		ref = ref.intern();
		URLClassLoader loader = loaders.get(ref);
		if (loader == null) {
			URL[] urls = new URL[] { new URL(ref) };
			loader = new URLClassLoader(urls, FxMain.class.getClassLoader());
			loaders.put(ref, loader);
		}
		return loader;
	}

		
	public void createSegmentFilters() {
		segmentFilters = new ArrayList<SegmentVideoFilter>();

		try {
			segmentFilters.add(new BWFilter());
			segmentFilters.add(new StereoDistanceFilter());
		} catch (Throwable t) {
			t.printStackTrace();
		}

		try {
			String pluginOpenCVbase;
			String pluginJogAmpbase;

			String version = getClass().getPackage().getImplementationVersion();
			String fs;
			if (version != null && !version.endsWith("-SNAPSHOT")) {
				fs = "/";
				pluginOpenCVbase = "https://oss.sonatype.org/service/local/repositories/releases/content/de/serviceflow/frankenstein/plugin/opencv/plugin-opencv/"
						+ version;
				pluginJogAmpbase = "https://oss.sonatype.org/service/local/repositories/releases/content/de/serviceflow/frankenstein/plugin/jogamp/plugin-jogamp/"
						+ version;
			} else {
				// Assume development environment in packaged state cwd = app/src/main/resources
				// @todo Eclipse: https://stackoverflow.com/questions/6092200/how-to-fix-an-unsatisfiedlinkerror-cant-find-dependent-libraries-in-a-jni-pro
				fs = File.separator;
				version = "0.3.4-SNAPSHOT";
				String baseFromAppResources = ".." + fs + ".." + fs + ".." + fs + ".." + fs;
				pluginOpenCVbase = new File(System.getProperty("user.dir") + fs + baseFromAppResources + "plugin-opencv"
						+ fs + "java" + fs + "target").toURI().toURL().toExternalForm();
				pluginJogAmpbase = new File(System.getProperty("user.dir") + fs + baseFromAppResources + "plugin-jogamp"
						+ fs + "java" + fs + "target").toURI().toURL().toExternalForm();
			}

			System.out.println("version (e.g. 0.3.3) = " + version);

			String pluginOpenCVRef = pluginOpenCVbase + "/" + "plugin-opencv-" + version + ".jar";
			String pluginJogAmpRef = pluginJogAmpbase + "/" + "plugin-jogamp-" + version + ".jar";

			// Filters completly in jar
//			segmentFilters.add(loadExternalFilterInstance(
//					"de.serviceflow.frankenstein.plugin.opencv.NativeExampleFilter", pluginOpenCVRef));
			segmentFilters.add(loadExternalFilterInstance(
					"de.serviceflow.frankenstein.plugin.opencv.VideoEqualizerFilter", pluginOpenCVRef));
//			segmentFilters.add(loadExternalFilterInstance(
//					"de.serviceflow.frankenstein.plugin.opencv.ExternalSampleFilter", pluginOpenCVRef));
			segmentFilters.add(loadExternalFilterInstance("de.serviceflow.frankenstein.plugin.jogamp.GLExampleFilter",
					pluginJogAmpRef));
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

}
