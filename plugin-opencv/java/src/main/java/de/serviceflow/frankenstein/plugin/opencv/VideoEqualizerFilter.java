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
package de.serviceflow.frankenstein.plugin.opencv;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import de.serviceflow.frankenstein.plugin.api.DefaultSegmentFilter;
import de.serviceflow.frankenstein.plugin.api.FilterContext;
import de.serviceflow.frankenstein.plugin.api.DefaultSegmentConfigController;
import de.serviceflow.frankenstein.plugin.opencv.jni.VideoEqualizer;

public class VideoEqualizerFilter extends DefaultSegmentFilter {

	private final VideoEqualizer proxy;

	private Mat mHsvMat = new Mat();

	public VideoEqualizerFilter() throws UnsatisfiedLinkError {
		super("videoequalizer");

		proxy = new VideoEqualizer();
		proxy.init();
	}

	@Override
	protected DefaultSegmentConfigController instantiateController() {
		return new VideoEqualizerConfigController();
	}

	@Override
	public Mat process(Mat rgbaImage, int frameId, FilterContext context) {
		Imgproc.cvtColor(rgbaImage, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

		VideoEqualizerConfigController c = ((VideoEqualizerConfigController) getConfigController());

		proxy.process(mHsvMat, frameId, context, c.getBrightness(), c.getContrast(), c.getSaturation());

		Imgproc.cvtColor(mHsvMat, rgbaImage, Imgproc.COLOR_HSV2RGB_FULL);

		return rgbaImage;
	}

	@Override
	protected void initializeController() {
		VideoEqualizerConfigController c = ((VideoEqualizerConfigController) getConfigController());
		c.setBrightness(50);
		c.setContrast(50);
		c.setSaturation(50);
		c.initialize();
	}
}
