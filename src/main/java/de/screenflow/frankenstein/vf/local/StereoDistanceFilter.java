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
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import de.screenflow.frankenstein.vf.LocalVideoFilter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class StereoDistanceFilter implements LocalVideoFilter {

	private Mat newFrame = null;

	public StereoDistanceFilter() {
	}

	@Override
	public Mat configure(Mat sourceFrame) {
		return sourceFrame;
	}

	@Override
	public Mat process(Mat sourceFrame, int frameId) {
		if (newFrame==null || newFrame.cols()!=sourceFrame.cols() || newFrame.rows()!=sourceFrame.rows()) {
			newFrame = sourceFrame.clone();
			newFrame.setTo(new Scalar(0, 0, 0)); // left/right borders are not repainted. 
		}
		
		Rect roi = new Rect(val(0, getPerspective(), 0, -1), 0, (sourceFrame.cols() >> 1) - Math.abs(getPerspective()),
				sourceFrame.rows());
		sourceFrame
				.submat(new Rect((Math.abs(getPerspective()) >> 1), 0,
						(sourceFrame.cols() >> 1) - Math.abs(getPerspective()), sourceFrame.rows()))
				.copyTo(new Mat(newFrame, roi));

		roi = new Rect(val(sourceFrame.cols() >> 1, -getPerspective(), sourceFrame.cols() >> 1, -1), 0,
				(sourceFrame.cols() >> 1) - Math.abs(getPerspective()), sourceFrame.rows());
		sourceFrame
				.submat(new Rect((sourceFrame.cols() >> 1) + (Math.abs(getPerspective()) >> 1), 0,
						(sourceFrame.cols() >> 1) - Math.abs(getPerspective()), sourceFrame.rows()))
				.copyTo(new Mat(newFrame, roi));
		return newFrame;
	}

	private int val(int value, int offset, int lowerBound, int upperBound) {
		int v = value + offset;
		if (v < lowerBound && lowerBound != -1) {
			return lowerBound;
		} else if (v > upperBound && upperBound != -1) {
			return upperBound;
		} else {
			return v;
		}
	}

	@Override
	public LocalVideoFilter createInstance() {
		return new StereoDistanceFilter();
	}

	public String toString() {
		return "Stereo Distance";
	}

	private StereoDistanceConfigController configController = null;

	@Override
	public Scene createConfigurationScene(Locale locale, String stylesheet) {
		String filterName = "stereodistance";
		PropertyResourceBundle bundleConfiguration = (PropertyResourceBundle) ResourceBundle
				.getBundle(getClass().getPackage().getName().replace('.', '/') + '/' + filterName, locale);
		FXMLLoader loader = new FXMLLoader(getClass().getResource(filterName + ".fxml"), bundleConfiguration);
		try {
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException("Failed to create configuration scene for video filter '" + this + "'", e);
		}
		Scene scene = new Scene(loader.getRoot());
		scene.getStylesheets().add(stylesheet);
		configController = loader.getController();
		configController.initialize(); // custom initialization possible here
		return scene;
	}

	/**
	 * -2n ... -4,-2,0,2,4, ... +2n : Negative values for farer away, positive
	 * for closer
	 */
	private int getPerspective() {
		return configController.getPerspective();
	}

}
