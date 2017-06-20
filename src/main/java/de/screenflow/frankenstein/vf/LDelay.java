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

public class LDelay implements VideoFilter {

	private Mat newFrame = null, frameBuffer;

	public LDelay() {
	}

	@Override
	public Mat configure(Mat sourceFrame) {
		newFrame = sourceFrame.clone();

		frameBuffer = sourceFrame.clone();

		// Take over right side to left side in buffer
		Rect roi = new Rect(0, 0, sourceFrame.cols() >> 1, sourceFrame.rows());
		sourceFrame.submat(new Rect(sourceFrame.cols() >> 1, 0, sourceFrame.cols() >> 1, sourceFrame.rows()))
				.copyTo(new Mat(frameBuffer, roi));
		
		return newFrame;
	}

	@Override
	public Mat process(Mat sourceFrame, int frameId) {

		// Take over left side from Buffer 
		Rect roi = new Rect(0, 0, sourceFrame.cols() >> 1, sourceFrame.rows());
		frameBuffer.submat(new Rect(0, 0, sourceFrame.cols() >> 1, sourceFrame.rows()))
				.copyTo(new Mat(newFrame, roi));

		// Copy Left Side from Source to Buffer
		roi = new Rect(0, 0, sourceFrame.cols() >> 1, sourceFrame.rows());
		sourceFrame.submat(new Rect(0, 0, sourceFrame.cols() >> 1, sourceFrame.rows()))
				.copyTo(new Mat(frameBuffer, roi));
		
		// Take over right side unchanged
		roi = new Rect(sourceFrame.cols() >> 1, 0, sourceFrame.cols() >> 1, sourceFrame.rows());
		sourceFrame.submat(new Rect(sourceFrame.cols() >> 1, 0, sourceFrame.cols() >> 1, sourceFrame.rows())).copyTo(new Mat(newFrame, roi));

		return newFrame;
	}
}
