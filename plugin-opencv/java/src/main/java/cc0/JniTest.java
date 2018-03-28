package cc0;

import de.serviceflow.frankenstein.plugin.api.NativeFilter;

public class JniTest {
	public JniTest() throws UnsatisfiedLinkError {
		NativeFilter.loadLibrary();
	}

	public static void main(String[] args) {
		new JniTest().helloNative();
	}

	public native void helloNative();

	public void process(Object mat, int frameId) {
		// not used
	}

}
