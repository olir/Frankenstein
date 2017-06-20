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

import java.util.ArrayList;
import java.util.List;

import de.screenflow.frankenstein.vf.VideoFilter;

/**
 * Data class 
 */
public class Configuration {
	public final List<VideoFilter> filters = new ArrayList<VideoFilter>();; 
	public boolean doInput = true;
	public boolean doOutput = true;
	public String ffmpegPath = "D:\\Software\\ffmpeg";
	public String inputVideo = "D:\\User\\Videos";
	public String outputVideo = "D:\\User\\Videos\\TestVideo.mp4";
	public int limitOutputWidth = 2880;
	
	public VideoFilter findFilter(Class c) {
		for (VideoFilter f : filters) {
			if (f.getClass()==c)
				return f;
		}
		return null;
	}
}
