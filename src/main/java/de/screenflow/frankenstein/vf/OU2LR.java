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
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class OU2LR implements VideoFilter {

	private Mat newFrame = null;
	private Mat upperFrame, lowerFrame;
	private Mat leftFrame, rightFrame;

	private int smallWidth;
	private int smallHeight;

	public static final float ADJUST_SIZE = 2.0f;
	public static final float REDUCE_SIZE = 1.0f;
	private float convertMode;

	public OU2LR(float convertMode) {
		this.convertMode = convertMode;
	}

	@Override
	public Mat configure(Mat sourceFrame) {
		smallWidth = sourceFrame.cols();
		smallHeight = sourceFrame.rows() >> 1;
		if (convertMode == REDUCE_SIZE) {
			smallWidth >>= 1;
		}

		newFrame = sourceFrame.clone();
		Imgproc.resize(sourceFrame, newFrame, new Size((double) (smallWidth << 1), (double) smallHeight));

		upperFrame = Mat.zeros(sourceFrame.rows() >> 1, sourceFrame.cols(), sourceFrame.type());
		lowerFrame = Mat.zeros(sourceFrame.rows() >> 1, sourceFrame.cols(), sourceFrame.type());
		leftFrame = Mat.zeros(smallHeight, smallWidth, sourceFrame.type());
		rightFrame = Mat.zeros(smallHeight, smallWidth, sourceFrame.type());

		return newFrame;
	}

	@Override
	public Mat process(Mat sourceFrame, int frameId) {

		Rect roi = new Rect(0, 0, smallWidth, smallHeight);
		sourceFrame.submat(new Rect(0, 0, sourceFrame.cols(), sourceFrame.rows() >> 1)).copyTo(upperFrame);
		sourceFrame.submat(new Rect(0, sourceFrame.rows() >> 1, sourceFrame.cols(), sourceFrame.rows() >> 1)).copyTo(lowerFrame);
		
		Imgproc.resize(upperFrame, leftFrame, new Size(smallWidth, smallHeight), 0, 0, Imgproc.INTER_AREA);
		Imgproc.resize(lowerFrame, rightFrame, new Size(smallWidth, smallHeight), 0, 0, Imgproc.INTER_AREA);

		newFrame.setTo(new Scalar(255, 0, 0));
		
		roi = new Rect(0, 0, smallWidth, smallHeight);
		leftFrame.copyTo(new Mat(newFrame, roi));

		roi = new Rect(smallWidth, 0, smallWidth, smallHeight);
		rightFrame.copyTo(new Mat(newFrame, roi));

		return newFrame;
	}
}
