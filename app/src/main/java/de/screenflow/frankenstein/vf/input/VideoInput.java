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

import java.io.File;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import de.screenflow.frankenstein.ProcessingListener;
import de.screenflow.frankenstein.vf.VideoSource;

public class VideoInput implements VideoSource {
	private VideoCapture movie = null;
	private final String videofile;
	private Mat currentFrame;
	private int currentPos;
	private int frames;
	private double fps;
	private int width;
	private int height;

	public VideoInput(String videofile) {
		this.videofile = videofile;
	}

	@Override
	public int getFrames() {
		return frames;
	}

	@Override
	public double getFps() {
		return fps;
	}

	@Override
	public void open(ProcessingListener l) {
		movie = new VideoCapture(videofile);
		if (!movie.isOpened()) {
			String path = System.getProperty("OPENH264_LIBRARY_PATH");
			if (path==null)
				System.out.println("Warning: OPENH264_LIBRARY_PATH not set. Input Movie Opening Error for " + videofile);

			throw new RuntimeException("Input Movie Opening Error for " + videofile+". Current path="+new File(".").getAbsolutePath());
		}
		currentFrame = new Mat();
		currentPos = 0;
		grab();
		currentFrame = retrieve(currentFrame, l);
		frames = (int) movie.get(Videoio.CAP_PROP_FRAME_COUNT);
		fps = movie.get(Videoio.CAP_PROP_FPS);
		width = (int) movie.get(Videoio.CAP_PROP_FRAME_WIDTH);
		height = (int) movie.get(Videoio.CAP_PROP_FRAME_HEIGHT);
	}

	@Override
	public void close() {
		if (movie != null)
			movie.release();
		movie = null;
	}

	@Override
	public void reopen(ProcessingListener l) {
		close();
		open(l);
	}

	private Mat retrieve(Mat frame, ProcessingListener l) {
		movie.retrieve(currentFrame); // ignore boolean
		return currentFrame;
	}

	private boolean grab() {
		currentPos++;
		return movie.grab();
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public Mat getFrame() {
		return currentFrame;
	}

	@Override
	public int seek(int pos, ProcessingListener l) {
		if (pos < currentPos) {
			reopen(l);
		}
		if (pos > currentPos) {
			for (int i = currentPos + 1; i <= pos; i++) {
				if (l != null)
					l.seeking(i);
				if (!grab()) {
					if (i <= frames && l != null) {
						l.prematureEnd(i - 2);
						currentPos = i - 1;
					}
				}
			}
			currentFrame = retrieve(currentFrame, l);
		}
		return currentPos;
	}

}
