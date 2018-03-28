package de.serviceflow.frankenstein.vf.jni;

import de.serviceflow.frankenstein.vf.NativeFilter;

public class VideoEqualizer extends NativeFilter {
	public VideoEqualizer() throws UnsatisfiedLinkError {
	}
	
	public native void init();
	public native void process(Object mat, int frameId, Object context, int brightness, int contrast, int saturation);
}
