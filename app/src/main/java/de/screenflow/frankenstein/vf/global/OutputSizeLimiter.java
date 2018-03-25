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
package de.screenflow.frankenstein.vf.global;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import de.screenflow.frankenstein.vf.FilterContext;
import de.screenflow.frankenstein.vf.VideoFilter;

public class OutputSizeLimiter implements VideoFilter {

	private Mat newFrame = null;

	private int newWidth;
	private int newHeight;

	int maximumWidth;

	public OutputSizeLimiter(int maximumWidth) {
		this.maximumWidth = maximumWidth;
	}

	@Override
	public Mat configure(Mat sourceFrame) {
		if (sourceFrame.cols() <= maximumWidth || maximumWidth < 1)
			return sourceFrame;

		newWidth = maximumWidth;
		newHeight = (int) ((float) maximumWidth * (((float) sourceFrame.rows()) / (float) sourceFrame.cols()));

		newFrame = sourceFrame.clone();
		Imgproc.resize(sourceFrame, newFrame, new Size(newWidth, newHeight));

		return newFrame;
	}

	@Override
	public Mat process(Mat sourceFrame, int frameId, FilterContext context) {

		if (newFrame == null)
			return sourceFrame;

		Imgproc.resize(sourceFrame, newFrame, new Size(newWidth, newHeight), 0, 0, Imgproc.INTER_AREA);

		return newFrame;
	}
}
