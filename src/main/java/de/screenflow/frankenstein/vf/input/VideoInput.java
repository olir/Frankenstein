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
package de.screenflow.frankenstein.vf.input;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import de.screenflow.frankenstein.vf.VideoSource;

public class VideoInput implements VideoSource {
	private VideoCapture movie = null;
	private final String videofile;

	public VideoInput(String videofile) {
		this.videofile = videofile;
	}

	@Override
	public int getFrames() {
		return (int) movie.get(Videoio.CAP_PROP_FRAME_COUNT);
	}

	@Override
	public double getFps() {
		return movie.get(Videoio.CAP_PROP_FPS);
	}

	@Override
	public void open() {
		movie = new VideoCapture(videofile);
		if (!movie.isOpened()) {
			throw new RuntimeException("Input Movie Opening Error for " + videofile);
		}
	}

	@Override
	public void close() {
		movie.release();
		movie = null;
	}

	@Override
	public void reopen() {
		close();
		open();
	}

	@Override
	public Mat retrieve(Mat frame) {
		movie.retrieve(frame); // ignore boolean
		return frame;
	}

	@Override
	public boolean grab() {
		return movie.grab();
	}

	@Override
	public int getWidth() {
		return (int)movie.get(Videoio.CAP_PROP_FRAME_WIDTH);
	}

	@Override
	public int getHeight() {
		return (int)movie.get(Videoio.CAP_PROP_FRAME_HEIGHT);
	}


}
