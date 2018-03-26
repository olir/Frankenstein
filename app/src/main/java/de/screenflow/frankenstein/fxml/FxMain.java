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
package de.screenflow.frankenstein.fxml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.net.URLClassLoader;

import de.screenflow.frankenstein.Configuration;
import de.screenflow.frankenstein.MovieProcessor;
import de.screenflow.frankenstein.vf.SegmentVideoFilter;
import de.screenflow.frankenstein.vf.segment.BWFilter;
import de.screenflow.frankenstein.vf.segment.GLExampleFilter;
import de.screenflow.frankenstein.vf.segment.NativeExampleFilter;
import de.screenflow.frankenstein.vf.segment.StereoDistanceFilter;
import de.screenflow.frankenstein.vf.segment.VideoEqualizerFilter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import samplefilters.SampleFilter;

public class FxMain extends Application {

	private Stage theStage;
	private Scene configurationScene, processingScene;

	private ProcessingSceneController pController;
	private ConfigurationSceneController cController;

	private List<SegmentVideoFilter> segmentFilters;

	public static final String APP_NAME = "Frankenstein VR";

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
				.getBundle("de/screenflow/frankenstein/bundles/configuration", locale);
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
				.getBundle("de/screenflow/frankenstein/bundles/processing", locale);
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

	public static Locale getLocale() {
		return locale;
	}

	public List<SegmentVideoFilter> getLocalFilters() {
		return segmentFilters;
	}

	public void createSegmentFilters() {
		segmentFilters = new ArrayList<SegmentVideoFilter>();
		segmentFilters.add(new BWFilter());
		segmentFilters.add(new StereoDistanceFilter());
		segmentFilters.add(new SampleFilter());
		segmentFilters.add(new GLExampleFilter());
		
		try {
			segmentFilters.add(new NativeExampleFilter()); // try to load from plugin jar
			segmentFilters.add(new VideoEqualizerFilter()); // try to load from plugin jar
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
	}

}
