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
package de.screenflow.frankenstein.vf.local;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import de.screenflow.frankenstein.vf.LocalVideoFilter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class BWFilter implements LocalVideoFilter {

	Mat grayFrame;

	public BWFilter() {
	}

	@Override
	public Mat configure(Mat sourceFrame) {
		
		// configController.getXY() ...
		
		grayFrame = sourceFrame.clone();
		Imgproc.cvtColor(sourceFrame, grayFrame, Imgproc.COLOR_RGB2GRAY);
		Imgproc.cvtColor(grayFrame, sourceFrame, Imgproc.COLOR_GRAY2RGB);
		return sourceFrame;
	}

	@Override
	public Mat process(Mat sourceFrame, int frameId) {
		Imgproc.cvtColor(sourceFrame, grayFrame, Imgproc.COLOR_RGB2GRAY);
		Imgproc.cvtColor(grayFrame, sourceFrame, Imgproc.COLOR_GRAY2RGB);
		return sourceFrame;
	}

	@Override
	public LocalVideoFilter createInstance() {
		return new BWFilter();
	}

	public String toString() {
		return "Black & White";
	}

	private BWConfigController configController = null;

	@Override
	public Scene createConfigurationScene(Locale locale, String stylesheet) {
		String filterName = "bw";
		PropertyResourceBundle bundleConfiguration = (PropertyResourceBundle) ResourceBundle.getBundle(getClass().getPackage().getName().replace('.', '/')+'/'+filterName, locale);
		FXMLLoader loader = new FXMLLoader(getClass().getResource(filterName+".fxml"), bundleConfiguration);
		try {
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException("Failed to create configuration scene for video filter '" + this + "'", e);
		}
		Scene scene = new Scene(loader.getRoot());
		scene.getStylesheets().add(stylesheet);
		configController = loader.getController();
		return scene;
	}
}
