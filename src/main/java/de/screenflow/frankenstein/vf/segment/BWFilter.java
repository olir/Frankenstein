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

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class BWFilter extends DefaultSegmentFilter<BWConfigController> {

	Mat grayFrame;

	public BWFilter() {
		super("bw");
	}

	@Override
	public Mat process(Mat sourceFrame, int frameId) {
		if (grayFrame == null || grayFrame.cols() != sourceFrame.cols() || grayFrame.rows() != sourceFrame.rows()) {
			grayFrame = sourceFrame.clone();
		}

		Imgproc.cvtColor(sourceFrame, grayFrame, Imgproc.COLOR_RGB2GRAY);
		Imgproc.cvtColor(grayFrame, sourceFrame, Imgproc.COLOR_GRAY2RGB);
		return sourceFrame;
	}

	@Override
	protected void initializeController() {
		// getConfigController(). ...
	}
}
