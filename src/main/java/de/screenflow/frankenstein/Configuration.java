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
package de.screenflow.frankenstein;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.screenflow.frankenstein.vf.VideoFilter;
import de.screenflow.frankenstein.vf.VideoSource;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.DirectoryChooser;

/**
 * Data class
 */
public class Configuration {
	// Ini-File properties
	Properties iniProperties = new Properties();

	// current global Video Filters
	private final List<VideoFilter> filters = new ArrayList<VideoFilter>();

	private VideoSource source;

	public boolean doInput = true;
	public boolean doOutput = true;

	private String inputVideo = null;
	public String outputVideo = null;

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

	public SectionedProperties metadata = new SectionedProperties();

	public static Configuration configure(String[] args) {
		Configuration configuration = new Configuration(null);

//		addTab(tabTestVideoGenerator);
//		addTab(tabClone);
//		addTab(tabVideoFileOutput);

//		rTestVideoGenerator.setSelected(true);
//		rCloneLR.setSelected(true);
//		rNoAlignment.setSelected(true);
//		rNoVR.setSelected(true);
//		rVideoFileOutput.setSelected(true);

//		tfPropertyInputDir.setText(configuration.getInputDir());

//		tfPropertyTestScreenWidth.setText(String.valueOf(configuration.testScreenWidth));
//		tfPropertyTestScreenHeight.setText(String.valueOf(configuration.testScreenHeight));
//		if (configuration.anaglyphKeepWidth)
//			rPropertyAnaglyphKeepWidth.setSelected(true);
//		else
//			rPropertyAnaglyphDoubleWidth.setSelected(true);

//		if (configuration.ouAdjustSize)
//			rPropertyOUAdjustSize.setSelected(true);
//		else
//			rPropertyOUReduceSize.setSelected(true);
//
//		if (configuration.delayLeft)
//			rDelayLeft.setSelected(true);
//		else
//			rDelayRight.setSelected(true);
//
//		sliderVRShrink.setValue(configuration.vrModeShrinkFactor * 100.0);
//		sliderVRShrink.valueProperty().addListener((observable, oldvalue, newvalue) -> {
//			int newFactor = newvalue.intValue();
//			configuration.vrModeShrinkFactor = ((float) newFactor) / 100.0f;
//			lVRShrinkDisplay.setText(String.valueOf(newFactor) + '%');
//		});
//
//		if (configuration.vrModeShrinkOnly)
//			vrModeFromVR.setSelected(true);
//		else
//			vrModeFromSBS.setSelected(true);
//
//		sliderStereoPerspective.setValue(configuration.perspective);
		return configuration;
	}

	/**
	 * Create empty configuration.
	 *
	 * @param helper ConfigHelper or null (non visual).
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
			if (helper==null)
				throw new RuntimeException("-ffmpegpath not set");
			File dir = null;
			while (dir == null) { // merciless inquisition
				dir = helper.getFFmpegPath();
			}
			iniProperties.setProperty("ffmpegpath", dir.getAbsolutePath());

			savePreferences();
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
		while (tf==null || !tf.canWrite()) { // merciless inquisition
			if (helper==null)
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

	public String getInputVideoPath() {
		return iniProperties.getProperty("inputvideopath");
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

}
