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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import de.serviceflow.frankenstein.plugin.api.FilterContext;
import de.serviceflow.frankenstein.plugin.api.NativeSegmentFilter;
import de.serviceflow.frankenstein.plugin.api.SegmentConfigController;

public class VideoEqualizerFilter extends NativeSegmentFilter<VideoEqualizerConfigController> {

	private final static String JNI_FILTER_CLASS = "de.serviceflow.frankenstein.plugin.opencv.jni.VideoEqualizer";

	private final Method jniProxyProcessMethod;

	private Mat mHsvMat = new Mat();
	
	@SuppressWarnings("unchecked")
	public VideoEqualizerFilter() throws UnsatisfiedLinkError {
		super("videoequalizer", JNI_FILTER_CLASS);
		try {
			jniProxyProcessMethod = getJniProxyClass().getMethod("process", Object.class, int.class, Object.class, int.class, int.class, int.class);
		} catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
			throw new RuntimeException("jni wrapper creation failed", e);
		}
	}

	@Override
	protected SegmentConfigController instantiateController() {
		return new VideoEqualizerConfigController();
	}

	@Override
	public Mat process(Mat rgbaImage, int frameId, FilterContext context) {
        Imgproc.cvtColor(rgbaImage, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

		VideoEqualizerConfigController c = ((VideoEqualizerConfigController)getConfigController());
		try {
			jniProxyProcessMethod.invoke(getJniProxy(), mHsvMat, frameId, context, c.getBrightness(), c.getContrast(), c.getSaturation());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		Imgproc.cvtColor(mHsvMat, rgbaImage, Imgproc.COLOR_HSV2RGB_FULL);
		
		return rgbaImage;
	}

	@Override
	protected void initializeController() {
		VideoEqualizerConfigController c = ((VideoEqualizerConfigController)getConfigController());
		c.setBrightness(50);
		c.setContrast(50);
		c.setSaturation(50);
		c.initialize();
	}
}
