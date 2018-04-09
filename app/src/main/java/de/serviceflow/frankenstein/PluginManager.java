package de.serviceflow.frankenstein;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.serviceflow.frankenstein.fxml.FxMain;
import de.serviceflow.frankenstein.plugin.api.SegmentVideoFilter;
import de.serviceflow.frankenstein.vf.segment.BWFilter;
import de.serviceflow.frankenstein.vf.segment.StereoDistanceFilter;

public class PluginManager {

	private final List<SegmentVideoFilter> segmentFilters = new ArrayList<SegmentVideoFilter>();

	public String getImplementationVersion() {
		return this.getClass().getPackage().getImplementationVersion();
	}
	
	public void load(Configuration configuration) {
		loadOpenCV();
		loadSegmentFilters();
	}

	private void loadOpenCV() {
		String opencvlib = "opencv_java320";
		String ffmpeglib = "opencv_ffmpeg320_64";
		String suffix = ".dll";
		String cvarchpath = "/nu/pattern/opencv/windows/x86_64/";
		
		LibraryManager lm = new LibraryManager();
		lm.prepareLoadLibraryWithoutArch(this.getClass(), ffmpeglib, suffix, null);
		System.loadLibrary(ffmpeglib);
		lm.prepareLoadLibraryWithoutArch(this.getClass(), opencvlib, suffix, cvarchpath);
		System.loadLibrary(opencvlib);
	}

	public List<SegmentVideoFilter> getLocalFilters() {
		return segmentFilters;
	}

	public TextSet loadPluginSet() throws IOException {
		File userPluginConfigFile = new File(new File(System.getProperty("user.home")), ".frankenstein-plugin.set");
		Reader reader = null;
		if (userPluginConfigFile.exists()) {
			reader = new FileReader(userPluginConfigFile);
		} else {
			reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("plugin.set"));
		}
		TextSet pluginSet = new TextSet();
		pluginSet.load(reader);
		reader.close();
		if (!userPluginConfigFile.exists()) {
			FileWriter writer = new FileWriter(userPluginConfigFile);
			pluginSet.store(writer);
			writer.close();
		}
		return pluginSet;
	}

	public void loadSegmentFilters() {
		try {
			segmentFilters.add(new BWFilter());
			segmentFilters.add(new StereoDistanceFilter());
		} catch (Throwable t) {
			t.printStackTrace();
		}

		String fs = File.separator;
		try {
			/*
			 * String pluginOpenCVbase; String pluginJogAmpbase;
			 *
			 *
			 * if (version != null && !version.endsWith("-SNAPSHOT")) { fs =
			 * "/"; pluginOpenCVbase =
			 * "https://oss.sonatype.org/service/local/repositories/releases/content/de/serviceflow/frankenstein/plugin/opencv/plugin-opencv/"
			 * + version; pluginJogAmpbase =
			 * "https://oss.sonatype.org/service/local/repositories/releases/content/de/serviceflow/frankenstein/plugin/jogamp/plugin-jogamp/"
			 * + version; } else { // Assume development environment in packaged
			 * state cwd = // app/src/main/resources // @todo Eclipse: //
			 * https://stackoverflow.com/questions/6092200/how-to-fix-an-
			 * unsatisfiedlinkerror-cant-find-dependent-libraries-in-a-jni-pro
			 * version = "0.3.4-SNAPSHOT"; String baseFromAppResources = ".." +
			 * fs + ".." + fs + ".." + fs + ".." + fs; pluginOpenCVbase = new
			 * File(System.getProperty("user.dir") + fs + baseFromAppResources +
			 * "plugin-opencv" + fs + "java" + fs +
			 * "target").toURI().toURL().toExternalForm(); pluginJogAmpbase =
			 * new File(System.getProperty("user.dir") + fs +
			 * baseFromAppResources + "plugin-jogamp" + fs + "java" + fs +
			 * "target").toURI().toURL().toExternalForm(); }
			 *
			 * 
			 *
			 * String pluginOpenCVRef = pluginOpenCVbase + "/" +
			 * "plugin-opencv-" + version + ".jar"; String pluginJogAmpRef =
			 * pluginJogAmpbase + "/" + "plugin-jogamp-" + version + ".jar";
			 */
			
			TextSet pluginSet = loadPluginSet();

			for (Iterator<String> piter = pluginSet.iterator(); piter.hasNext();) {
				String pluginRef = piter.next();
				if (pluginRef.startsWith("http:") || pluginRef.startsWith("https:")) {
					// no op
				} else {
					if (pluginRef.startsWith("."))
						pluginRef = System.getProperty("user.dir") + fs + pluginRef;
					File f = new File(pluginRef);
					if (!f.exists()) {
						System.err.println("Warning: Plugin at path not found: " + pluginRef);
						continue;
					}
					if (f.isDirectory()) {
						File[] list = f.listFiles();
						f = null;
						if (list != null) {
							for (File fil : list) {
								if (!fil.isDirectory()) {
									if (!fil.getName().endsWith("-sources.jar")
											&& !fil.getName().endsWith("-javadoc.jar")) {
										f = fil;
										pluginRef = fil.toURI().toURL().toExternalForm();
										break;
									}
								}
							}
						}
						if (f == null) {
							System.err.println("Warning: Plugin path contains no plugin: " + pluginRef);
							continue;
						}
					}
				}
				Set<String> filterSet = loadFilterSet(pluginRef);
				if (filterSet != null) {
					for (Iterator<String> fiter = filterSet.iterator(); fiter.hasNext();) {
						String filterClassRef = fiter.next();
						segmentFilters.add(loadExternalFilterInstance(filterClassRef, pluginRef));
					}
				}
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

	private Set<String> loadFilterSet(String pluginRef) {
		try {
			URLClassLoader childLoader = getLoader(pluginRef);
			InputStream s = childLoader.getResourceAsStream("filter.set");
			TextSet filterSet = new TextSet();
			if (s != null) {
				filterSet.load(new InputStreamReader(s));
				s.close();
			} else {
				System.err.println("Warning: Plugin contains no filter.set file: " + pluginRef);
			}
			return filterSet;
		} catch (IOException e) {
			System.err.println(e.getLocalizedMessage() + ": Plugin path not found: " + pluginRef);
			return null;
		}
	}

	private SegmentVideoFilter loadExternalFilterInstance(String filterClassName, String ref) {
		try {
			// use dynamic loading and reflection when loading jni proxy class
			// from jar, so app do not depend on it.
			System.out.println("loading " + filterClassName + " from " + ref + " ...");
			URLClassLoader childLoader = getLoader(ref);
			Class<?> filterClass = Class.forName(filterClassName, true, childLoader);
			SegmentVideoFilter filter = (SegmentVideoFilter) filterClass.newInstance();
			return filter;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | MalformedURLException
				| SecurityException
				| IllegalArgumentException /*
											 * | InvocationTargetException |
											 * NoSuchMethodException
											 */ e) {
			throw new RuntimeException("Failed loading filter " + filterClassName + " from " + ref, e);
		}
	}

	static Map<String, URLClassLoader> loaders = new HashMap<String, URLClassLoader>();

	static synchronized URLClassLoader getLoader(String ref) throws MalformedURLException {
		ref = ref.intern();
		URLClassLoader loader = loaders.get(ref);
		if (loader == null) {
			URL[] urls = new URL[] { new URL(ref) };
			loader = new URLClassLoader(urls, FxMain.class.getClassLoader());
			loaders.put(ref, loader);
		}
		return loader;
	}

}
