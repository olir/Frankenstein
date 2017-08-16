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

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import de.screenflow.frankenstein.ProcessingListener;
import de.screenflow.frankenstein.vf.VideoSource;

public class CameraInput implements VideoSource {
	private final int id;
	
	private VideoCapture movie = null;
	private Mat currentFrame;
	private double fps = 10.0;
	private int width;
	private int height;

	public CameraInput(int id) {
		this.id = id;
	}

	@Override
	public int getFrames() {
		return -1;
	}

	@Override
	public double getFps() {
		return fps;
	}

	@Override
	public void open(ProcessingListener l) {
		movie = new VideoCapture(id);
		if (!movie.isOpened()) {
			throw new RuntimeException("Camera Opening Error for #" + id);
		}
		currentFrame = new Mat();
		grab();
		currentFrame = retrieve(currentFrame, l);
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
		try {
			Thread.sleep((int)(1000.0/fps));
		} catch (InterruptedException e) {
		}
		return currentFrame;
	}

	private boolean grab() {
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
		currentFrame = retrieve(currentFrame, l);
		return pos; 
	}

}