package de.screenflow.frankenstein.vf.jni;

public class MatBlender extends NativeFilter {
	public MatBlender() throws UnsatisfiedLinkError {
	}

	public native void init();
	public native void process(Object mat, int frameId, Object context, Object overlay);
}
