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

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class TestImage implements VideoFilter {

	private Mat newFrame = null;
	private Mat testFrame = null;

	private int smallWidth;
	private int smallHeight;

	final Scalar white = new Scalar(255, 255, 255);
	final Scalar red = new Scalar(0, 0, 255);

	public TestImage(int width, int height) {
		this.smallWidth = width;
		this.smallHeight = height;
	}

	@Override
	public Mat configure(Mat sourceFrame) {
		testFrame = sourceFrame.clone();
		newFrame = sourceFrame.clone();
		Imgproc.resize(sourceFrame, testFrame, new Size((double) smallWidth, (double) smallHeight));
		Imgproc.resize(sourceFrame, newFrame, new Size((double) smallWidth, (double) smallHeight));

		testFrame.setTo(new Scalar(0, 0, 0));
		drawImage(0, 0, smallWidth, smallHeight);

		return testFrame;
	}

	@Override
	public Mat process(Mat sourceFrame, int frameId) {
		testFrame.copyTo(newFrame);
		Imgproc.putText(newFrame, "Frame #" + frameId, new Point(10, smallHeight - 10), Core.FONT_HERSHEY_PLAIN, 3.0,
				red, 2);
		return newFrame;
	}

	private void drawImage(int xoffset, int yoffset, int width, int height) {
		int count = 10;
		int gridSize = ((height / count) >> 1) << 1;
		while (gridSize >= 8)
			gridSize >>= 1;

		int xmid = xoffset + (width >> 1);
		Imgproc.line(testFrame, new Point(xmid, yoffset), new Point(xmid, yoffset + height - 1), white, 3);
		for (int x = xmid + gridSize; x < xoffset + width; x += gridSize) {
			Imgproc.line(testFrame, new Point(x, yoffset), new Point(x, yoffset + height - 1), white, 1);
		}
		for (int x = xmid - gridSize; x > xoffset; x -= gridSize) {
			Imgproc.line(testFrame, new Point(x, yoffset), new Point(x, yoffset + height - 1), white, 1);
		}

		int ymid = yoffset + (height >> 1);
		Imgproc.line(testFrame, new Point(xoffset, ymid), new Point(xoffset + width - 1, ymid), white, 3);
		for (int y = ymid + gridSize; y < yoffset + height; y += gridSize) {
			Imgproc.line(testFrame, new Point(xoffset, y), new Point(xoffset + width - 1, y), white, 1);
		}
		for (int y = ymid - gridSize; y > yoffset; y -= gridSize) {
			Imgproc.line(testFrame, new Point(xoffset, y), new Point(xoffset + width - 1, y), white, 1);
		}

		Imgproc.putText(testFrame, "" + width + " x " + height,
				new Point(xmid - gridSize * 1.33, ymid - gridSize * 0.25), Core.FONT_HERSHEY_PLAIN, 4.0, red, 3);
	}

	public int getWidth() {
		return smallWidth;
	}

	public void setWidth(int smallWidth) {
		this.smallWidth = smallWidth;
	}

	public int getHeight() {
		return smallHeight;
	}

	public void setHeight(int smallHeight) {
		this.smallHeight = smallHeight;
	}
}
