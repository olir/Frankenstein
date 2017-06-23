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

/**
 * Data class
 */
public class Configuration {
	// Ini-File properties
	Properties iniProperties = new Properties();

	public final List<VideoFilter> filters = new ArrayList<VideoFilter>();

	public boolean doInput = true;
	public boolean doOutput = true;

	public String inputVideo = null;
	public String outputVideo = null;
	public int limitOutputWidth = 2880;

	public Configuration(ConfigHelper helper) {
		String homeDir = System.getProperty("user.home");
		File configFile = new File(homeDir, "frankenstein.ini");
		if (configFile.canRead()) {
			try {

				// load a properties file
				iniProperties.load(new FileInputStream(configFile));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		if (getFFmpegPath() == null) {
			File dir = null;
			while (dir==null) { // merciless inquisition
				dir = helper.getFFmpegPath();
			}
			iniProperties.setProperty("ffmpegpath", dir.getAbsolutePath());

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
	}

	public VideoFilter findFilter(Class c) {
		for (VideoFilter f : filters) {
			if (f.getClass() == c)
				return f;
		}
		return null;
	}

	public String getFFmpegPath() {
		return iniProperties.getProperty("ffmpegpath");
	}

	public interface ConfigHelper {
		public File getFFmpegPath();
	}
}
