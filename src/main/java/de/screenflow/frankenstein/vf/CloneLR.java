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
package de.screenflow.frankenstein.vf;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class CloneLR implements VideoFilter {

	private Mat newFrame = null;
	private int smallWidth;
	private int smallHeight;

	public CloneLR() {
	}

	@Override
	public Mat configure(Mat sourceFrame) {
		smallWidth = sourceFrame.cols();
		smallHeight = sourceFrame.rows();
		newFrame = sourceFrame.clone();
		Imgproc.resize(sourceFrame, newFrame, new Size((double) (smallWidth << 1), (double) smallHeight));

		return newFrame;
	}

	@Override
	public Mat process(Mat sourceFrame, int frameId) {

		Rect roi = new Rect(0, 0, sourceFrame.cols(), sourceFrame.rows());
		sourceFrame.copyTo(new Mat(newFrame, roi));

		roi = new Rect(sourceFrame.cols(), 0, sourceFrame.cols(), sourceFrame.rows());
		sourceFrame.copyTo(new Mat(newFrame, roi));

		return newFrame;
	}
}
