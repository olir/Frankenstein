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
import de.serviceflow.frankenstein.plugin.opencv.jni.ExternalSample;

public class ExternalSampleFilter extends DefaultSegmentFilter {

	private final ExternalSample proxy;

	private Mat mHsvMat = new Mat();

	public ExternalSampleFilter() throws UnsatisfiedLinkError {
		super("externalsample");

		proxy = new ExternalSample();
		proxy.init();
	}

	@Override
	protected DefaultSegmentConfigController instantiateController() {
		return new ExternalSampleConfigController();
	}

	@Override
	public Mat process(Mat rgbaImage, int frameId, FilterContext context) {
		Imgproc.cvtColor(rgbaImage, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

		// ExternalSampleConfigController c = ((ExternalSampleConfigController)
		// getConfigController());
		// pass c data to proxy...

		proxy.process(mHsvMat, frameId, context);

		Imgproc.cvtColor(mHsvMat, rgbaImage, Imgproc.COLOR_HSV2RGB_FULL);

		return rgbaImage;
	}

	@Override
	protected void initializeController() {
		// ExternalSampleConfigController c = ((ExternalSampleConfigController)
		// getConfigController());
		// c.set...(...);
		// c.initialize();
	}
}
