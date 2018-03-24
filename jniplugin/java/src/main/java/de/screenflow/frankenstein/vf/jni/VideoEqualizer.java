package de.screenflow.frankenstein.vf.jni;

public class VideoEqualizer extends NativeFilter {
	public VideoEqualizer() throws UnsatisfiedLinkError {
	}
	
	public native void init();
	public native void process(Object mat, int frameId, int brightness, int contrast, int saturation);
}
