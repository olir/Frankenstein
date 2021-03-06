/*
 * Copyright 2017 Oliver Rode, https://github.com/olir/Frankenstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.serviceflow.frankenstein;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import de.serviceflow.frankenstein.plugin.api.ConfigManager;
import de.serviceflow.frankenstein.vf.VideoFilter;
import de.serviceflow.frankenstein.vf.VideoSource;
import de.serviceflow.frankenstein.vf.input.CameraInput;
import de.serviceflow.frankenstein.vf.input.SlideShowInput;
import de.serviceflow.frankenstein.vf.input.StreamInput;
import de.serviceflow.frankenstein.vf.input.TestImageInput;
import de.serviceflow.frankenstein.vf.input.VideoInput;

/**
 * Handles information about the configuration of the 'Project' and the tool.
 */
public class Configuration implements ConfigManager {
	// Ini-File properties
	Properties iniProperties = new Properties();

	// current global Video Filters
	private final List<VideoFilter> filters = new ArrayList<VideoFilter>();

	private VideoSource source;

	public boolean doInput = true;
	public boolean doOutput = true;

	private boolean visual = true;

	private String inputVideo = null;
	public String outputVideo = null;
	private String recordingVideo = null;
	private String inputVideoStreamURL = null;


	public int limitOutputWidth = 2880;

	public int testScreenWidth = 1280;
	public int testScreenHeight = 768;

	public boolean anaglyphKeepWidth = true;

	public boolean delayLeft = true;

	public boolean ouAdjustSize = false;

	public boolean vrModeShrinkOnly;

	public float vrModeShrinkFactor = 0.8f;

	public int perspective = 0;

	private final File configFile;

	private final PluginManager pluginManager = new PluginManager();

	public SectionedProperties metadata = new SectionedProperties();

	public static StringBuffer usage = new StringBuffer();

	private static final Configuration configuration = new Configuration(null);

	public void init() {
		getPluginManager().load(configuration);

		// todo ...

		
	}
	
	public static ConfigManager getInstance() {
		return configuration;
	}

	public static Configuration cliCreateConfiguration(String[] args) {

		int optionKeyIndex;
		String optionValue;
		Properties optionProperties;

		usage.setLength(0);
		usage.append("Usage:\n\nfrankenstein");

		optionKeyIndex = getOptionIndex(args, "cli", false, false);
		configuration.setVisual(optionKeyIndex<0);

		optionKeyIndex = getOptionIndex(args, "source", true, false);
		optionValue = getOptionValue(args, "source", "test", optionKeyIndex, false, "file", "test", "slides", "camera", "stream");
		optionProperties = getOptionProperties(args, optionKeyIndex);
		cliConfigureSource(configuration, optionValue, optionProperties);

		usage.append("\n\nfrankenstein");
		if (getOptionIndex(args, "?", false, true)>=0) {
			return null;
		}

		return configuration;
	}

	
	
	private static void cliConfigureSource(Configuration configuration, String optionValue,
			Properties optionProperties) {
		switch (optionValue) {
		case "file":
			configuration.source = new VideoInput(optionProperties.getProperty("file", null));
			break;
		case "slides":
			configuration.source = new SlideShowInput(optionProperties.getProperty("dir", "."));
			break;
		case "camera":
			configuration.source = new CameraInput(0);
			break;
		case "stream":
			configuration.source = new StreamInput(optionProperties.getProperty("stream", null), optionProperties.getProperty("file", null));
			break;
		default:
		case "test":
			configuration.source = new TestImageInput(Integer.valueOf(optionProperties.getProperty("width", "1024")),
					Integer.valueOf(optionProperties.getProperty("height", "768")));
		}
	}

	private static int getOptionIndex(String[] args, String optionKey, boolean hasValues, boolean mandatory) {
		usage.append(' ');
		if (!mandatory)
			usage.append('[');
		usage.append('-');
		usage.append(optionKey);
		if (!mandatory && !hasValues)
			usage.append(']');

		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-" + optionKey + "=") || args[i].equals("-" + optionKey)) {
				return i;
			}
		}
		return -1;
	}

	private static String getOptionValue(String[] args, String optionKey, String defaultValue, int optionKeyIndex,
			boolean mandatory, 
			String... optionValues) {
		usage.append("=");
		for (String opt : optionValues) {
			usage.append(opt);
			usage.append('|');
		}
		usage.setLength(usage.length()-1);
		if (!mandatory)
			usage.append(']');

		if (optionKeyIndex < 0)
			return defaultValue;

		String value = args[optionKeyIndex].substring(optionKey.length() + 2);
		for (String v : optionValues) {
			if (v.equals(value))
				return v;
		}
		System.err.println("Warning: Invalid option for -"+optionKey+": '"+value+"'. Using default instead: "+defaultValue);
		return defaultValue;
	}

	private static Properties getOptionProperties(String[] args, int optionKeyIndex) {
		Properties p = new Properties();

		if (optionKeyIndex < 0)
			return p;

		for (int i = optionKeyIndex + 1; i < args.length; i++) {
			if (args[i].startsWith("-")) {
				break;
			}
			int sep = args[i].indexOf('=');
			if (sep > 0) {
				String key = args[i].substring(0, sep);
				String value = args[i].substring(sep + 1);
				p.setProperty(key, value);
			}
		}
		return p;
	}

	/**
	 * Create empty configuration.
	 *
	 * @param helper
	 *            ConfigHelper or null (non visual).
	 */
	public Configuration(ConfigHelper helper) {
		String homeDir = System.getProperty("user.home");
		outputVideo = new File(new File(System.getProperty("user.home")), "TestVideo.mp4").getAbsolutePath();
		configFile = new File(homeDir, "frankenstein.ini");

		if (configFile.canRead()) {
			try {

				// load a properties file
				iniProperties.load(new FileInputStream(configFile));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

    if (getFFmpegPath() == null) {
      if (helper == null) {
        System.err.println("Error: -ffmpegpath not set");
      }
      if (helper != null) {
        File dir = null;
        while (dir == null) { // merciless inquisition
          dir = helper.getFFmpegPath();
        }
        iniProperties.setProperty("ffmpegpath", dir.getAbsolutePath());

        savePreferences();
      }
    }

		String tdp = getTempPath();
		File tdf = tdp != null ? new File(tdp) : null;
		File tf = null;
		try {
			tf = File.createTempFile("Frankenstein", "tmp", tdf);
			tdf = tf.getParentFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (tf == null || !tf.canWrite()) { // merciless inquisition
			if (helper == null)
				throw new RuntimeException("-temppath not set");
			tdf = helper.getTempPath();
			try {
				tf = File.createTempFile("Frankenstein", "tmp", tdf);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		iniProperties.setProperty("temppath", tdf.getAbsolutePath());

		savePreferences();
	}

	public void savePreferences() {
		// save properties to project root folder
		try {
			iniProperties.store(new FileOutputStream(configFile), null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public VideoFilter findFilter(Class<?> c) {
		for (VideoFilter f : filters) {
			if (f.getClass() == c)
				return f;
		}
		return null;
	}

	public String getFFmpegPath() {
		return iniProperties.getProperty("ffmpegpath");
	}

	public String getTempPath() {
		return iniProperties.getProperty("temppath");
	}

	public String getInputDir() {
		return iniProperties.getProperty("inputdirslides");
	}

	public void setInputDir(String inputdir) {
		iniProperties.setProperty("inputdirslides", inputdir);
		savePreferences();
	}

	public String getInputVideo() {
		return inputVideo;
	}

	public void setInputVideo(String inputVideo) {
		this.inputVideo = inputVideo;
		iniProperties.setProperty("inputvideopath", new File(inputVideo).getParent());
		savePreferences();
	}

	public String getRecordingVideo() {
		return recordingVideo;
	}

	public void setRecordingVideo(String recordingVideo) {
		this.recordingVideo = recordingVideo;
		iniProperties.setProperty("recordingvideopath", new File(recordingVideo).getParent());
		savePreferences();
	}

	public String getInputVideoPath() {
		return iniProperties.getProperty("inputvideopath");
	}

	public String getInputStreamURL() {
		return inputVideoStreamURL;
	}

	public void setInputStreamURL(String inputVideoStreamURL) {
		this.inputVideoStreamURL = inputVideoStreamURL;
	}

	public String getRecordingVideoPath() {
		return iniProperties.getProperty("recordingvideopath", iniProperties.getProperty("inputvideopath"));
	}

	public interface ConfigHelper {
		public File getFFmpegPath();

		public File getTempPath();
	}

	public VideoSource getSource() {
		return source;
	}

	public void setSource(VideoSource source) {
		this.source = source;
	}

	public List<VideoFilter> getFilters() {
		return filters;
	}

	public boolean isVisual() {
		return visual;
	}

	public void setVisual(boolean visual) {
		this.visual = visual;
	}

	public static String getUsage() {
		return usage.toString();
	}

	public PluginManager getPluginManager() {
		return pluginManager;
	}

	private static Locale locale = Locale.getDefault();


	public Locale getLocale() {
		return locale;
	}


}
