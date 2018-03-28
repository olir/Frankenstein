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
package de.serviceflow.frankenstein;

import org.opencv.core.Mat;

import de.serviceflow.frankenstein.plugin.api.SegmentFilterConfigListener;
import de.serviceflow.frankenstein.vf.VideoStreamSource;

public interface ProcessingListener extends SegmentFilterConfigListener {
	void videoStarted(int frames, double fps);

	void nextFrameLoaded(VideoStreamSource s);

	void nextFrameProcessed(Mat frame, int frameId);

	void seekDone(int frameId);

	void seeking(int i);

	void prematureEnd(int realFrameCount);

	void taskUpdate(String timeStamp, String message);

	void taskError(String errorMessage);
}
