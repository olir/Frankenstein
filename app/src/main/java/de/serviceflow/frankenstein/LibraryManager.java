package de.serviceflow.frankenstein;

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

public class LibraryManager {

	private static final String TEMPPATH =  LibraryManager.class.getPackage().getName();
	private static final Set<String> loadedLibraries = Collections.synchronizedSet(new HashSet<String>());

	public String prepareLoadLibrary(Class<?> invokerClass, String libraryBasename) throws UnsatisfiedLinkError {
		System.out.println("sun.arch.data.model=" + System.getProperty("sun.arch.data.model"));
		System.out.println("os.arch=" + System.getProperty("os.arch"));


		String plattformLibraryName = libraryBasename;
		String suffix = ".dll";
		if (System.getProperty("os.arch").contains("64") && System.getProperty("sun.arch.data.model").contains("64")) {
			// load 64-bit lib
			plattformLibraryName = plattformLibraryName + "-win64";
		} else {
			// load 32-bit lib
			plattformLibraryName = plattformLibraryName + "-win32";
		}
		// TODO !!! ...

		prepareLoadLibraryImpl(invokerClass, plattformLibraryName, suffix);

		return plattformLibraryName;
	}

	private static void prepareLoadLibraryImpl(Class<?> invokerClass, String plattformLibraryName, String suffix)
			throws UnsatisfiedLinkError {
		synchronized (loadedLibraries) {

			if (loadedLibraries.contains(plattformLibraryName.intern()))
				return;

			String libFileName = plattformLibraryName + suffix;
			String location = "/" + libFileName;
			InputStream binary = invokerClass.getResourceAsStream("/" + libFileName);
			if (binary == null)
				throw new Error("binary not found: " + "/" + libFileName);

			try {
				Path tmpDir = Files.createTempDirectory(TEMPPATH);
				tmpDir.toFile().deleteOnExit();
				Path destination = tmpDir.resolve("./" + location).normalize();

				try {
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
				} catch (IllegalAccessException x) {
					throw new Error("IllegalAccessException!?", x);
				} catch (NoSuchFieldException x) {
					throw new Error("NoSuchFieldException!?", x);
				}

				loadedLibraries.add(plattformLibraryName.intern());

			} catch (final IOException x) {
				throw new Error("Error writing native library", x);
			}
			// }
		}
	}

}
