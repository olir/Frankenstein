package cc0;

import de.screenflow.frankenstein.vf.jni.NativeFilter;

public class NativeExample extends NativeFilter {
	public NativeExample() throws UnsatisfiedLinkError {
	}
	
	public native void init();
	public native void process(Object mat, int frameId);
}
