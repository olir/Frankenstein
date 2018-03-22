package cc0;

public class NativeExample extends NativeFilter {
	public NativeExample() throws UnsatisfiedLinkError {
	}
	
	public native void init();
	public native void process(Object mat, int frameId);
}
