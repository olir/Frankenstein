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
package de.serviceflow.frankenstein.vf.segment;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import de.serviceflow.frankenstein.plugin.api.DefaultSegmentFilter;
import de.serviceflow.frankenstein.plugin.api.FilterContext;
import de.serviceflow.frankenstein.plugin.api.DefaultSegmentConfigController;

public class StereoDistanceFilter extends DefaultSegmentFilter {

	private Mat newFrame = null;
    private int perspectiveMultiplier = 1;
    
	public StereoDistanceFilter() {
		super("stereodistance");
	}

	@Override
	public Mat process(Mat sourceFrame, int frameId, FilterContext context) {
		if (newFrame == null || newFrame.cols() != sourceFrame.cols() || newFrame.rows() != sourceFrame.rows()) {
			newFrame = sourceFrame.clone();
			perspectiveMultiplier = sourceFrame.cols() / 320;
			if (perspectiveMultiplier<1)
				perspectiveMultiplier = 1;
		}

		newFrame.setTo(new Scalar(0, 0, 0));
		
		Rect roi = new Rect(val(0, perspective(), 0, -1), 0, (sourceFrame.cols() >> 1) - Math.abs(perspective()),
				sourceFrame.rows());
		sourceFrame.submat(new Rect((Math.abs(perspective()) >> 1), 0,
				(sourceFrame.cols() >> 1) - Math.abs(perspective()), sourceFrame.rows()))
				.copyTo(new Mat(newFrame, roi));

		roi = new Rect(val(sourceFrame.cols() >> 1, -perspective(), sourceFrame.cols() >> 1, -1), 0,
				(sourceFrame.cols() >> 1) - Math.abs(perspective()), sourceFrame.rows());
		sourceFrame
				.submat(new Rect((sourceFrame.cols() >> 1) + (Math.abs(perspective()) >> 1), 0,
						(sourceFrame.cols() >> 1) - Math.abs(perspective()), sourceFrame.rows()))
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

	/**
	 * -32 ... -4,-2,0,2,4, ... +32 : Negative values for farer away, positive
	 * for closer
	 */
	private int perspective() {
		return perspectiveMultiplier * ((StereoDistanceConfigController)getConfigController()).getPerspective();
	}

	@Override
	protected void initializeController() {
		((StereoDistanceConfigController)getConfigController()).initialize();
	}

	@Override
	protected DefaultSegmentConfigController instantiateController() {
		return new StereoDistanceConfigController();
	}

}
