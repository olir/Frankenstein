package de.serviceflow.frankenstein.plugin.opencv.jni;

import de.serviceflow.frankenstein.plugin.api.NativeFilter;

public class VideoEqualizer extends NativeFilter {
	public VideoEqualizer() throws UnsatisfiedLinkError {
	}
	
  protected void loadLibrary(String name) {
    System.loadLibrary(name);
  }
  
	public native void init();
	public native void process(Object mat, int frameId, Object context, int brightness, int contrast, int saturation);
}
