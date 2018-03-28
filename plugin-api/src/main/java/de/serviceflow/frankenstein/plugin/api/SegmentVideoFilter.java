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
package de.serviceflow.frankenstein.plugin.api;

import org.opencv.core.Mat;

import javafx.scene.Scene;

public interface SegmentVideoFilter {
	SegmentVideoFilter createInstance();

	Scene createConfigurationScene(String stylesheet);

	SegmentConfigController getConfigController();

	Mat process(Mat sourceFrame, int frameId, FilterContext context);

}