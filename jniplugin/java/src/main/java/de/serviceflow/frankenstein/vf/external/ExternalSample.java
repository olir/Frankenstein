package de.serviceflow.frankenstein.vf.external;

import de.serviceflow.frankenstein.vf.NativeFilter;

public class ExternalSample extends NativeFilter {
	public ExternalSample() throws UnsatisfiedLinkError {
	}

	public native void init();
	public native void process(Object mat, int frameId, Object context, Object params, Object result);
}
