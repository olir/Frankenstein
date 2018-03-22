package cc0;

public class JniExample extends NativeFilter {
	public JniExample() throws UnsatisfiedLinkError {
	}

	public static void main(String[] args) {
		new JniExample().helloNative();
	}

	public native void helloNative();
	
	public void process(Object mat, int frameId) {
		// not used
	}
	
}
