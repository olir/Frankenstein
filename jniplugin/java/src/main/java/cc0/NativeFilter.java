package cc0;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class NativeFilter {
	private static boolean loaderCalled = false;
	private static UnsatisfiedLinkError error = null;
	private static final Set<String> loadedLibraries = Collections.synchronizedSet(new HashSet<String>());


	protected NativeFilter() throws UnsatisfiedLinkError {
		if (!loaderCalled) {
			loaderCalled = true;
			System.out.println("Working Directory = " + new File(".").getAbsolutePath());
			try {
				if (System.getProperty("os.arch").contains("64")
						&& System.getProperty("sun.arch.data.model").contains("64")) {
					// load 64-bit lib
					loadLibrary("jniplugin-64");
				} else {
					// load 32-bit lib
					loadLibrary("jniplugin-32");
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

	private void loadLibrary(String libraryName) throws UnsatisfiedLinkError {
		synchronized (loadedLibraries) {

			if (loadedLibraries.contains(libraryName.intern()))
				return;

			try {
				System.loadLibrary(libraryName);
			} catch (final UnsatisfiedLinkError e) {
				if (!String.format("no %s in java.library.path", libraryName).equals(e.getMessage())) {
					System.out.println(
							"*** SOME PROBLEM! --> '" + e.getMessage() + "' --- libraryName='" + libraryName + "'");
					throw e;
				}

				String libFileName = libraryName + ".dll";
				String location = "/cc0/" + libFileName;
				InputStream binary = NativeFilter.class.getResourceAsStream("/" + libFileName);
				if (binary == null)
					throw new Error("binary not found: " + "/" + libFileName);

				try {
					Path tmpDir = Files.createTempDirectory("frankenstein");
					tmpDir.toFile().deleteOnExit();
					Path destination = tmpDir.resolve("./" + location).normalize();
					Files.createDirectories(destination.getParent());
					Files.copy(binary, destination);
					String nPath = destination.getParent().normalize().toString();

					Field field = ClassLoader.class.getDeclaredField("usr_paths");
					field.setAccessible(true);

					Set<String> myPath = new HashSet<String>(Arrays.asList((String[]) field.get(null)));
					myPath.add(nPath);

					field.set(null, myPath.toArray(new String[myPath.size()]));

					System.setProperty("java.library.path",
							System.getProperty("java.library.path") + File.pathSeparator + nPath);
				} catch (final IOException x) {
					throw new Error("Error writing native library", x);
				} catch (IllegalAccessException x) {
					throw new Error("IllegalAccessException!?", x);
				} catch (NoSuchFieldException x) {
					throw new Error("NoSuchFieldException!?", x);
				}

				System.loadLibrary(libraryName);

				loadedLibraries.add(libraryName.intern());
			}
		}
	}

}
