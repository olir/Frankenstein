package de.serviceflow.frankenstein.plugin.opencv.jni;

import de.serviceflow.frankenstein.plugin.api.NativeJniProxy;

public class ExternalSample extends NativeJniProxy {
	public ExternalSample() throws UnsatisfiedLinkError {
	}
	
  protected void loadLibrary(String name) {
    System.loadLibrary(name);
  }
  
	public native void init();
	public native void process(Object mat, int frameId, Object context);
}
