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

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import de.serviceflow.frankenstein.vf.FilterContext;
import de.serviceflow.frankenstein.vf.VideoFilter;

public class Anaglyph2LR implements VideoFilter {

	private Mat newFrame = null;
	private Mat leftFrame, rightFrame;
	private Mat halfFrame1, halfFrame2;

	private int smallWidth;
	private int smallHeight;

	public static final float DOUBLE_WIDTH = 2.0f;
	public static final float KEEP_WIDTH = 1.0f;
	private float convertMode;

	public Anaglyph2LR(float convertMode) {
		this.convertMode = convertMode;
	}

	@Override
	public Mat configure(Mat sourceFrame) {
		smallWidth = sourceFrame.cols();
		if (convertMode == KEEP_WIDTH)
			smallWidth >>= 1;
		smallHeight = sourceFrame.rows();

		newFrame = sourceFrame.clone();
		Imgproc.resize(sourceFrame, newFrame, new Size((double) smallWidth * 2, (double) smallHeight));
		// newFrame.setTo(new Scalar(0, 0, 0));

		leftFrame = Mat.zeros(sourceFrame.rows(), sourceFrame.cols(), sourceFrame.type());
		rightFrame = Mat.zeros(sourceFrame.rows(), sourceFrame.cols(), sourceFrame.type());
		halfFrame1 = Mat.zeros(smallHeight, smallWidth, sourceFrame.type());
		halfFrame2 = Mat.zeros(smallHeight, smallWidth, sourceFrame.type());

		return newFrame;
	}

	@Override
	public Mat process(Mat sourceFrame, int frameId, FilterContext context) {

		List<Mat> sourcePlanes = new ArrayList<Mat>();
		Core.split(sourceFrame, sourcePlanes); // planes[2] is the red channel
		List<Mat> leftPlanes = new ArrayList<Mat>();
		List<Mat> rightPlanes = new ArrayList<Mat>();

		leftPlanes.add(sourcePlanes.get(2));
		leftPlanes.add(sourcePlanes.get(2));
		leftPlanes.add(sourcePlanes.get(2));
		Core.merge(leftPlanes, leftFrame);

		rightPlanes.add(sourcePlanes.get(1));
		rightPlanes.add(sourcePlanes.get(1));
		rightPlanes.add(sourcePlanes.get(1));
		Core.merge(rightPlanes, rightFrame);

		Imgproc.resize(leftFrame, halfFrame1, new Size(smallWidth, smallHeight), 0, 0, Imgproc.INTER_AREA);
		Imgproc.resize(rightFrame, halfFrame2, new Size(smallWidth, smallHeight), 0, 0, Imgproc.INTER_AREA);

		Rect roi = new Rect(0, 0, smallWidth, smallHeight);
		halfFrame1.copyTo(new Mat(newFrame, roi));

		roi = new Rect(smallWidth, 0, smallWidth, smallHeight);
		halfFrame2.copyTo(new Mat(newFrame, roi));

		return newFrame;
	}
}
