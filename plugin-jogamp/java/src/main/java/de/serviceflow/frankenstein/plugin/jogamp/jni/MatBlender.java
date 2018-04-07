package de.serviceflow.frankenstein.plugin.jogamp.jni;

import de.serviceflow.frankenstein.plugin.api.NativeJniProxy;

public class MatBlender extends NativeJniProxy {
	public MatBlender() throws UnsatisfiedLinkError {
	}

  protected void loadLibrary(String name) {
    System.loadLibrary(name);
  }

	public native void init();
	public native void process(Object mat, int frameId, Object context, Object overlay);
}
