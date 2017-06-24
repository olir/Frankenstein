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

public class LR2VR180 implements VideoFilter {

	private Mat newFrame = null;

	private float factor = 0.80f;
	private float vrVerticalSpan;

	private int borderW;
	private int borderH;
	private int smallWidth;
	private int smallHeight;

	public static final float PAD_3D_TO_VR = 1.0f;
	public static final float SHRINK_VR_ONLY = 0.0f;
	private float convert3DMode;

	private static int ALIGMENT_POT = 3;
	private static int STEPS_PER_DIRECTION = 2 << (ALIGMENT_POT - 1);

	int[] srcStepOffset = new int[STEPS_PER_DIRECTION];
	int[] srcStepHeight = new int[STEPS_PER_DIRECTION];
	int[] destStepOffset = new int[STEPS_PER_DIRECTION];
	int[] destStepHeight = new int[STEPS_PER_DIRECTION];
	Mat[] bufferFrame = new Mat[STEPS_PER_DIRECTION];

	boolean coneCorrection = true;

	public LR2VR180(float convert3DMode) {
		this.convert3DMode = convert3DMode;
	}

	@Override
	public Mat configure(Mat sourceFrame) {

		float aspect = ((float) sourceFrame.cols()) / (float) sourceFrame.rows();
		int vcut = 0;

		if (aspect > 2f)
			aspect = aspect / 2f; // not a half sbs
		if (aspect < 1.3f) {
			aspect *= 1.3f;
			vcut = 1;
		}

		vrVerticalSpan = 0.7f - (aspect - 1.33333f) / 1.7f; // aspect heuristic
		if (vrVerticalSpan < 0.4f)
			vrVerticalSpan = 0.4f;

		System.out.println("VerticalSpan: " + vrVerticalSpan + "  vcut: " + vcut);

		borderW = (int) ((1.0f - factor) * (float) (sourceFrame.cols() >> 1) * 0.25f)
				+ vcut * (sourceFrame.cols() >> 2);

		borderH = (int) (((1.0f - factor) + (1.0f / vrVerticalSpan - 1.0) * convert3DMode) * (float) sourceFrame.rows()
				* 0.5f);
		borderH = ((borderH + (2 << (ALIGMENT_POT - 1)) - 1) >> ALIGMENT_POT) << ALIGMENT_POT;

		newFrame = sourceFrame.clone();
		Imgproc.resize(sourceFrame, newFrame,
				new Size((double) sourceFrame.cols() + 4 * borderW, (double) sourceFrame.rows() + 2 * borderH));
		newFrame.setTo(new Scalar(0, 0, 0));

		smallWidth = sourceFrame.cols() >> 1;
		smallHeight = sourceFrame.rows();

		System.out.println("aspect: " + (((float) sourceFrame.cols()) / 2f / (float) sourceFrame.rows()) + " ==> "
				+ (((float) newFrame.cols()) / 2f / (float) newFrame.rows()));

		if (coneCorrection) {
			for (int i = 0; i < STEPS_PER_DIRECTION; i++) {
				bufferFrame[i] = sourceFrame.clone();
				Imgproc.resize(sourceFrame, bufferFrame[i],
						new Size((double) sourceFrame.cols() + 4 * borderW, (double) sourceFrame.rows() + 2 * borderH));
				bufferFrame[i].setTo(new Scalar(0, 0, 0));
			}

			double[] weight = new double[STEPS_PER_DIRECTION];
			double sum = 0.0;

			for (int i = 0; i < STEPS_PER_DIRECTION; i++) {
				weight[i] = 1.0 + ((double) i) / (double) (STEPS_PER_DIRECTION - 1);
				sum += weight[i];
			}
			int hsumSrc = 0;
			int hsumDest = 0;
			for (int i = 0; i < STEPS_PER_DIRECTION; i++) {
				weight[i] = sum / ((double) STEPS_PER_DIRECTION) / weight[i];
				srcStepOffset[i] = hsumSrc;
				srcStepHeight[i] = (int) ((((double) (smallHeight >> 1)) / (double) STEPS_PER_DIRECTION));
				hsumSrc += srcStepHeight[i];
				destStepOffset[i] = hsumDest;
				destStepHeight[i] = (int) (weight[i] * (((double) (smallHeight >> 1)) / (double) STEPS_PER_DIRECTION));
				hsumDest += destStepHeight[i];
			}
//			srcStepHeight[STEPS_PER_DIRECTION - 1] += (smallHeight >> 1) - hsumSrc;
//			destStepHeight[STEPS_PER_DIRECTION - 1] += (smallHeight >> 1) - hsumDest;
		}

		return newFrame;
	}

	@Override
	public Mat process(Mat sourceFrame, int frameId) {

		Rect roiDest, roiSource;

		if (!coneCorrection) {
			roiDest = new Rect(borderW, borderH, smallWidth, smallHeight);
			sourceFrame.submat(new Rect(0, 0, smallWidth, smallHeight)).copyTo(new Mat(newFrame, roiDest));

			roiDest = new Rect(smallWidth + 3 * borderW, borderH, smallWidth, smallHeight);
			sourceFrame.submat(new Rect(smallWidth, 0, smallWidth, smallHeight)).copyTo(new Mat(newFrame, roiDest));
		} else {
			int ymid = smallHeight >> 1;
			for (int i = 0; i < STEPS_PER_DIRECTION; i++) {
				roiSource = new Rect(0, ymid + srcStepOffset[i], sourceFrame.cols(), srcStepHeight[i]);
				Imgproc.resize(sourceFrame.submat(roiSource), bufferFrame[i],
						new Size(sourceFrame.cols(), destStepHeight[i]), 0, 0, Imgproc.INTER_AREA);
				// bufferFrame.setTo(new Scalar((i % 2) * 255, 0, 255));

				roiDest = new Rect(borderW, borderH + ymid + destStepOffset[i], smallWidth, destStepHeight[i]);
				bufferFrame[i].submat(new Rect(0, 0, smallWidth, destStepHeight[i])).copyTo(new Mat(newFrame, roiDest));

				roiDest = new Rect(smallWidth + 3 * borderW, borderH + ymid + destStepOffset[i], smallWidth,
						destStepHeight[i]);
				bufferFrame[i].submat(new Rect(smallWidth, 0, smallWidth, destStepHeight[i]))
						.copyTo(new Mat(newFrame, roiDest));

				roiSource = new Rect(0, ymid - srcStepOffset[i] - srcStepHeight[i], sourceFrame.cols(),
						srcStepHeight[i]);
				Imgproc.resize(sourceFrame.submat(roiSource), bufferFrame[i],
						new Size(sourceFrame.cols(), destStepHeight[i]), 0, 0, Imgproc.INTER_AREA);
				// bufferFrame.setTo(new Scalar((i % 2) * 255, 255, 0));

				roiDest = new Rect(borderW, borderH + ymid - destStepHeight[i] - destStepOffset[i], smallWidth,
						destStepHeight[i]);
				bufferFrame[i].submat(new Rect(0, 0, smallWidth, destStepHeight[i])).copyTo(new Mat(newFrame, roiDest));

				roiDest = new Rect(smallWidth + 3 * borderW, borderH + ymid - destStepHeight[i] - destStepOffset[i],
						smallWidth, destStepHeight[i]);
				bufferFrame[i].submat(new Rect(smallWidth, 0, smallWidth, destStepHeight[i]))
						.copyTo(new Mat(newFrame, roiDest));

			}
		}

		return newFrame;
	}
}
