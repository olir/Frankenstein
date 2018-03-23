package cc0;

public class JniExample {
	public JniExample() throws UnsatisfiedLinkError {
    NativeFilter.loadLibrary();
	}

	public static void main(String[] args) {
		new JniExample().helloNative();
	}

	public native void helloNative();
	
	public void process(Object mat, int frameId) {
		// not used
	}
	
}
