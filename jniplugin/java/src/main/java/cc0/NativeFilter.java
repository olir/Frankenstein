package cc0;

import java.io.File;

public abstract class NativeFilter {
	private static boolean loaderCalled = false;
	private static UnsatisfiedLinkError error = null;

	protected NativeFilter() throws UnsatisfiedLinkError {
		if (!loaderCalled) {
			loaderCalled = true;
			System.out.println("Working Directory = " + new File(".").getAbsolutePath());
			try {
				if (System.getProperty("os.arch").contains("64")
						&& System.getProperty("sun.arch.data.model").contains("64")) {
					// load 64-bit lib
					System.loadLibrary("jniplugin-64");
				} else {
					// load 32-bit lib
					System.loadLibrary("jniplugin-32");
				}
			} catch (UnsatisfiedLinkError t) {
				System.out.println("sun.arch.data.model=" + System.getProperty("sun.arch.data.model"));
				System.out.println("os.arch=" + System.getProperty("os.arch"));

				error = t;
				throw t;
			}
			if (error != null)
				throw error; // throw again
		}

	}

	public abstract void process(Object mat, int frameId);
}
