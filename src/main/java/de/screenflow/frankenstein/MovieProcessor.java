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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import de.screenflow.frankenstein.vf.VideoFilter;
import de.screenflow.frankenstein.vf.input.TestImageInput;

public class MovieProcessor {

	private final String ffmpegPath;
	private final List<VideoFilter> filters;
	private final Configuration configuration;

	// private final VideoCapture movie = null;
	private VideoWriter outputVideo;
	private File tempVideoFile = null;
	private File tempAudioFile = null;
	private File tempMetadataFile = null;
	private int fourcc;

	private Mat frame = new Mat();

	private double movie_fps;
	private double movie_frameCount;
	private double movie_h;
	private double movie_w;

	private static boolean stopped = false;
	int currentPos = 0;

	private File ffmpeg;

	public MovieProcessor(Configuration configuration) {
		this.ffmpegPath = configuration.getFFmpegPath();
		this.configuration = configuration;
		filters = configuration.filters;
	}

	public void init(ProcessingListener l) {
		currentPos = 1;

		if (!openInput(l)) {
			if (l != null)
				l.prematureEnd(1);
			return;
		}
		openOutput(l);

		// TODO: Currently Windows only
		ffmpeg = new File(ffmpegPath, "\\bin\\ffmpeg.exe");

		if (l != null)
			l.videoStarted(configuration.source.getFrames(), configuration.source.getFps());

		currentPos = 1;
	}

	public boolean process(ProcessingListener l) {
		try {

			// 1. Detach Audio and Metadata from orginal video and store
			// temporarily
			if (configuration.doOutput && configuration.doInput) {
				if (!new Task(ffmpeg.getAbsolutePath() + " -y -i \"" + configuration.inputVideo + "\""
						+ " -f ffmetadata " + tempMetadataFile.getAbsolutePath()
						+ " -vn -ar 44100 -ac 2 -ab 192k -f mp3 -r 21 " + tempAudioFile.getAbsolutePath(), l,
						"Splitting Audio").run())
					return false;
			}

			// 2. Process Video without audio ()
			System.out.print("Processing video: ");
			Mat newFrame = null;

			int i = 0;
			while (!stopped && i < configuration.source.getFrames()) {
				i++;
				currentPos = configuration.source.seek(i, l);
				frame = configuration.source.getFrame();
				if (frame != null && !frame.empty()) {
					if (!filters.isEmpty()) {
						newFrame = frame;
						for (VideoFilter filter : filters) {
							newFrame = filter.process(newFrame, i);
						}
					} else {
						newFrame = frame;
					}

					if (configuration.doOutput) {
						outputVideo.write(newFrame);
					}

					if (l != null)
						l.nextFrameProcessed(newFrame, currentPos);

					if ((i % 1000) == 0) {
						System.out.print("+"); // break;
					} else if ((i % 100) == 0)
						System.out.print(".");
				} else {
					if (currentPos < movie_frameCount && l != null)
						l.prematureEnd(currentPos);
					break;
				}
			}
			System.out.println("ok\nFrames proccessed: " + i);
			if (configuration.doOutput) {
				outputVideo.release();
			}
			if (stopped) {
				return false;
			}

			if (configuration.doOutput) {
				new File(configuration.outputVideo).delete();
				if (configuration.doInput) {
					if (!new Task(ffmpeg.getAbsolutePath() + " -i " + tempVideoFile.getAbsolutePath() + " -i "
							+ tempAudioFile.getAbsolutePath() + " -i " + tempMetadataFile.getAbsolutePath()
							+ " -map_metadata 2" + " -c:a aac -c:v libx264  -q 17 \"" + configuration.outputVideo + '"',
							l, "Processing Output").run())
						return false;
				} else {
					if (!new Task(ffmpeg.getAbsolutePath() + " -i " + tempVideoFile.getAbsolutePath()
							+ " -c:v libx264  -q 17 " + configuration.outputVideo, l, "Processing Output").run())
						return false;
				}
			}
		} finally {
			closeInput();
			closeOutput();
		}
		return true;
	}

	public static void stop() {
		stopped = true;
	}

	public boolean openInput(ProcessingListener l) {
		configuration.source.open(l);

		movie_fps = configuration.source.getFps();
		movie_frameCount = configuration.source.getFrames();
		movie_w = configuration.source.getWidth();
		movie_h = configuration.source.getHeight();

		frame = configuration.source.getFrame();

		System.out.println("Dimensions: " + movie_w + "x" + movie_h);
		System.out.println("fps: " + movie_fps + "  frameCount: " + movie_frameCount);

		return true;
	}

	public void closeInput() {
		configuration.source.close();
	}

	public boolean openOutput(ProcessingListener l) {
		if (configuration.doOutput) {
			String tempOutputFormat = "mkv"; // format without errors so far
			boolean compress = false;
			if (compress)
				fourcc = VideoWriter.fourcc('M', 'J', 'P', 'G'); // -- Motion
																	// JPEG
			else
				fourcc = 0; // uncompressed.

			// if (fourcc == 0) {
			// output = output.substring(0, output.lastIndexOf('.')) + "_uc."
			// + output.substring(output.lastIndexOf('.') + 1);
			// }

			outputVideo = new VideoWriter();
			try {
				// Store temp Video next to output to avoid SSD must be used for
				// large files.
				File tempFile = File.createTempFile("video", "." + tempOutputFormat);
				tempVideoFile = new File(new File(configuration.outputVideo).getParentFile(), tempFile.getName());
				tempFile.deleteOnExit();
				tempAudioFile = File.createTempFile("sound", ".mp3");
				tempMetadataFile = File.createTempFile("metadata", ".properties");
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		return configureOutput(l);
	}

	// fourcc = 0x00000021; // openh264 - problem with ffmpeg
	// fourcc = VideoWriter.fourcc('X','2','6','4'); // -- MP4
	// fourcc = VideoWriter.fourcc('M','R','L','E'); // -- Motion JPEG
	// (nativ)
	// fourcc = VideoWriter.fourcc('P','I','M','1'); // - MPEG-1 based
	// codec - bad audio
	// fourcc = VideoWriter.fourcc('M','S','V','C'); // + Video For
	// Windows - bad Video quality

	public boolean configureOutput(ProcessingListener l) {
		Mat newFrame = frame;
		if (!filters.isEmpty()) {
			for (VideoFilter filter : filters) {
				newFrame = filter.configure(newFrame);
			}
		}

		if (configuration.doOutput) {
			outputVideo.open(tempVideoFile.getAbsolutePath(), fourcc, movie_fps,
					new Size(movie_w, movie_h), true);
			System.err.println("newsize=" + new Size(movie_w, movie_h));

			if (!outputVideo.isOpened()) {
				System.err.println("Could not open the output video for write.");
				return false;
			}
		}
		return true;
	}

	public void closeOutput() {
		if (!configuration.doOutput)
			return;

		outputVideo = null;

		tempVideoFile.delete();
		tempAudioFile.delete();
		tempMetadataFile.delete();
	}

	public void seek(final ProcessingListener l, int frameId) {
		if (configuration.doInput && frameId < currentPos) {
			if (l != null)
				l.seeking(0);

			currentPos = 0;
			configuration.source.reopen(l);
		}
		currentPos = configuration.source.seek(frameId, l);
		frame = configuration.source.getFrame();
		if (frame != null && !frame.empty()) {
			Mat newFrame = frame;
			for (VideoFilter filter : filters) {
				newFrame = filter.process(newFrame, frameId);
			}			
			if (l != null)
				l.nextFrameProcessed(newFrame, currentPos);
		} else {
			if (frameId <= movie_frameCount && l != null)
				l.prematureEnd(frameId - 1);
		}

		l.seekDone(frameId);
	}

	private class Task {

		private String[] args;
		private String command;
		// private boolean shutdown = false;
		// private File log;
		private ProcessingListener l;
		private final String taskMessage;

		Task(String command, ProcessingListener l, String taskMessage) {
			this.taskMessage = taskMessage;
			this.command = command;
			this.args = command.split(" ");
			Task.this.l = l;
		}

		private boolean run() {
			ProcessBuilder processBuilder = new ProcessBuilder(args);
			processBuilder.directory(new File(System.getProperty("java.io.tmpdir")));

			try {
				// Log to ...\AppData\Local\Temp
				// log = File.createTempFile("frankestein_task_", ".log");
				processBuilder.redirectErrorStream(true);
				// processBuilder.redirectOutput(Redirect.appendTo(log));
				System.out.println("Executing:\n" + command);
				Process p = processBuilder.start();
				InputStream is = p.getInputStream();
				new Thread(new LogHandler(is, l)).start();
				p.waitFor();
				// log.deleteOnExit();
				// shutdown = true;
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				// shutdown = true;
				return false;
			} catch (InterruptedException e) {
				e.printStackTrace();
				// shutdown = true;
				return false;
			}
		}

		private class LogHandler implements Runnable {
			InputStream is;
			ProcessingListener l;

			LogHandler(InputStream is, ProcessingListener l) {
				LogHandler.this.is = is;
				LogHandler.this.l = l;
			}

			public void run() {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;
				try {
					while ((line = br.readLine()) != null) {
						int s = line.indexOf("time=");
						if (s >= 0) {
							int e = line.indexOf(' ', s);
							progress(line.substring(s + 5, e));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				progress(null);
			}

			private void progress(String time) {
				if (l != null)
					l.taskUpdate(time, taskMessage);
			}
		}

	}
}