package de.serviceflow.frankenstein.vf;

public interface VideoStreamSource extends VideoSource {
	void pause();
	int getCurrentPos();
}
