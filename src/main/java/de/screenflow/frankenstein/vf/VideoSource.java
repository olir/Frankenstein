package de.screenflow.frankenstein.vf;

import org.opencv.core.Mat;

public interface VideoSource {
	int getFrames();
	double getFps();
	int getWidth();
	int getHeight();

	void open();  // TODO need catched Exception

	void close();

	void reopen();

	Mat retrieve(Mat frame);
	boolean grab();
}
