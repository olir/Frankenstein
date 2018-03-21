package de.screenflow.frankenstein.vf;

import org.opencv.core.Mat;

import de.screenflow.frankenstein.ProcessingListener;

public interface VideoSource {
	int getFrames();
	double getFps();
	int getWidth();
	int getHeight();

	void open(ProcessingListener l);  // TODO need catched Exception

	void close();

	void reopen(ProcessingListener l);

	Mat getFrame();
	int seek(int pos, ProcessingListener l);
	
//	Mat retrieve(Mat frame);
//	boolean grab();
}
