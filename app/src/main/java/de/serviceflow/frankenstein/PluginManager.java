package de.serviceflow.frankenstein;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.serviceflow.frankenstein.fxml.FxMain;
import de.serviceflow.frankenstein.plugin.api.SegmentVideoFilter;
import de.serviceflow.frankenstein.vf.segment.BWFilter;
import de.serviceflow.frankenstein.vf.segment.StereoDistanceFilter;

public class PluginManager {

	private List<SegmentVideoFilter> segmentFilters;

	public void load(Configuration configuration) {
		createSegmentFilters();
	}

	public List<SegmentVideoFilter> getLocalFilters() {
		return segmentFilters;
	}

	public void createSegmentFilters() {
		segmentFilters = new ArrayList<SegmentVideoFilter>();

		try {
			segmentFilters.add(new BWFilter());
			segmentFilters.add(new StereoDistanceFilter());
		} catch (Throwable t) {
			t.printStackTrace();
		}

		try {
			String pluginOpenCVbase;
			String pluginJogAmpbase;

			String version = getClass().getPackage().getImplementationVersion();
			String fs;
			if (version != null && !version.endsWith("-SNAPSHOT")) {
				fs = "/";
				pluginOpenCVbase = "https://oss.sonatype.org/service/local/repositories/releases/content/de/serviceflow/frankenstein/plugin/opencv/plugin-opencv/"
						+ version;
				pluginJogAmpbase = "https://oss.sonatype.org/service/local/repositories/releases/content/de/serviceflow/frankenstein/plugin/jogamp/plugin-jogamp/"
						+ version;
			} else {
				// Assume development environment in packaged state cwd =
				// app/src/main/resources
				// @todo Eclipse:
				// https://stackoverflow.com/questions/6092200/how-to-fix-an-unsatisfiedlinkerror-cant-find-dependent-libraries-in-a-jni-pro
				fs = File.separator;
				version = "0.3.4-SNAPSHOT";
				String baseFromAppResources = ".." + fs + ".." + fs + ".." + fs + ".." + fs;
				pluginOpenCVbase = new File(System.getProperty("user.dir") + fs + baseFromAppResources + "plugin-opencv"
						+ fs + "java" + fs + "target").toURI().toURL().toExternalForm();
				pluginJogAmpbase = new File(System.getProperty("user.dir") + fs + baseFromAppResources + "plugin-jogamp"
						+ fs + "java" + fs + "target").toURI().toURL().toExternalForm();
			}

			System.out.println("version (e.g. 0.3.3) = " + version);

			String pluginOpenCVRef = pluginOpenCVbase + "/" + "plugin-opencv-" + version + ".jar";
			String pluginJogAmpRef = pluginJogAmpbase + "/" + "plugin-jogamp-" + version + ".jar";

			// Filters completly in jar
			segmentFilters.add(loadExternalFilterInstance(
					"de.serviceflow.frankenstein.plugin.opencv.NativeExampleFilter", pluginOpenCVRef));
			segmentFilters.add(loadExternalFilterInstance(
					"de.serviceflow.frankenstein.plugin.opencv.VideoEqualizerFilter", pluginOpenCVRef));
			// segmentFilters.add(loadExternalFilterInstance(
			// "de.serviceflow.frankenstein.plugin.opencv.ExternalSampleFilter",
			// pluginOpenCVRef));
			segmentFilters.add(loadExternalFilterInstance("de.serviceflow.frankenstein.plugin.jogamp.GLExampleFilter",
					pluginJogAmpRef));
		} catch (Throwable t) {
			t.printStackTrace();
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
