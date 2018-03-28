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
package de.serviceflow.frankenstein.vf.global;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import de.serviceflow.frankenstein.plugin.api.FilterContext;
import de.serviceflow.frankenstein.vf.VideoFilter;

public class LDelay implements VideoFilter {

	private Mat newFrame = null, frameBuffer;
	private final boolean delayLeft;

	public LDelay(boolean delayLeft) {
		this.delayLeft = delayLeft;
	}

	@Override
	public Mat configure(Mat sourceFrame) {
		newFrame = sourceFrame.clone();

		frameBuffer = sourceFrame.clone();

		if (delayLeft) {
			// Take over right side to left side in buffer
			Rect roi = new Rect(0, 0, sourceFrame.cols() >> 1, sourceFrame.rows());
			sourceFrame.submat(new Rect(sourceFrame.cols() >> 1, 0, sourceFrame.cols() >> 1, sourceFrame.rows()))
					.copyTo(new Mat(frameBuffer, roi));
		} else {
			// Take over left side to right side in buffer
			Rect roi = new Rect(sourceFrame.cols() >> 1, 0, sourceFrame.cols() >> 1, sourceFrame.rows());
			sourceFrame.submat(new Rect(0, 0, sourceFrame.cols() >> 1, sourceFrame.rows()))
					.copyTo(new Mat(frameBuffer, roi));
		}
		return newFrame;
	}

	@Override
	public Mat process(Mat sourceFrame, int frameId, FilterContext context) {

		int x1 = 0;
		int x2 = sourceFrame.cols() >> 1;
		if (!delayLeft) {
			x1 = x2;
			x2 = 0;
		}
			
		// Take over left side from Buffer
		Rect roi = new Rect(x1, 0, sourceFrame.cols() >> 1, sourceFrame.rows());
		frameBuffer.submat(new Rect(x1, 0, sourceFrame.cols() >> 1, sourceFrame.rows())).copyTo(new Mat(newFrame, roi));

		// Copy Left Side from Source to Buffer
		     roi = new Rect(x1, 0, sourceFrame.cols() >> 1, sourceFrame.rows());
		sourceFrame.submat(new Rect(x1, 0, sourceFrame.cols() >> 1, sourceFrame.rows()))
				.copyTo(new Mat(frameBuffer, roi));

		// Take over right side unchanged
		roi = new Rect(x2, 0, sourceFrame.cols() >> 1, sourceFrame.rows());
		sourceFrame.submat(new Rect(x2, 0, sourceFrame.cols() >> 1, sourceFrame.rows()))
				.copyTo(new Mat(newFrame, roi));

		return newFrame;
	}
}
