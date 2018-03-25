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
package de.screenflow.frankenstein.vf.segment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.opencv.core.Mat;

import de.screenflow.frankenstein.vf.FilterContext;

public class NativeExampleFilter extends NativeSegmentFilter<NativeExampleConfigController> {

	private final static String JNI_FILTER_CLASS = "cc0.NativeExample";

	private final Method jniProxyProcessMethod;

	@SuppressWarnings("unchecked")
	public NativeExampleFilter() throws UnsatisfiedLinkError {
		super("native", JNI_FILTER_CLASS);
		try {
			jniProxyProcessMethod = getJniProxyClass().getMethod("process", Object.class, int.class, Object.class);
		} catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
			throw new RuntimeException("jni wrapper creation failed", e);
		}
	}

	@Override
	public Mat process(Mat sourceFrame, int frameId, FilterContext context) {
		try {
			jniProxyProcessMethod.invoke(getJniProxy(), sourceFrame, frameId, context);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return sourceFrame;
	}

	@Override
	protected void initializeController() {
		// getConfigController(). ...
	}
}
