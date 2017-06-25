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
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import de.screenflow.frankenstein.vf.TestImage;
import de.screenflow.frankenstein.vf.VideoFilter;

public class MovieProcessor {

	private final String ffmpegPath;
	private final List<VideoFilter> filters;
	private final Configuration configuration;

	private VideoCapture movie = null;
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

		if (!openInput()) {
			if (l != null)
				l.prematureEnd(1);
			return;
		}
		openOutput(l);

		// TODO: Currently Windows only
		ffmpeg = new File(ffmpegPath, "\\bin\\ffmpeg.exe");

		if (configuration.doInput) {
			if (l != null)
				l.videoStarted(movie.get(Videoio.CAP_PROP_FRAME_COUNT), movie.get(Videoio.CAP_PROP_FPS));
		} else {
			if (l != null)
				l.videoStarted(1000, 20);
		}

		currentPos = 1;

		if (l != null)
			l.nextFrameLoaded(frame, currentPos);
	}

	public boolean process(ProcessingListener l) {
		try {

			// 1. Detach Audio and Metadata from orginal video and store
			// temporarily
			if (configuration.doOutput && configuration.doInput) {
				if (!new Task(ffmpeg.getAbsolutePath() + " -y -i \"" + configuration.inputVideo + "\""
//						+ " -f ffmetadata " + tempMetadataFile.getAbsolutePath()
						+ " -vn -ar 44100 -ac 2 -ab 192k -f mp3 -r 21 " + tempAudioFile.getAbsolutePath(), l,
						"Splitting Audio").run())
					return false;
			}

			// 2. Process Video without audio ()
			System.out.print("Processing video: ");
			Mat newFrame = null;

			int i = 0;
			while (!stopped && (configuration.doInput || i < 1000)) {
				if (i > 0) {
					if (configuration.doInput)
						movie.read(frame);
					else
						frame = Mat.zeros((int) movie_w, (int) movie_h, CvType.CV_8UC3);

				}
				if (!frame.empty()) {
					currentPos = ++i;

					if (l != null)
						l.nextFrameLoaded(frame, i);

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
						l.nextFrameProcessed(newFrame, i);

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
			if (configuration.doInput) {
				movie.release();
			}
			if (stopped) {
				return false;
			}

			if (configuration.doOutput) {
				new File(configuration.outputVideo).delete();
				if (configuration.doInput) {
					if (!new Task(ffmpeg.getAbsolutePath() + " -i " + tempVideoFile.getAbsolutePath() + " -i "
							+ tempAudioFile.getAbsolutePath() + " -c:a aac -c:v libx264  -q 17 \""
							+ configuration.outputVideo + '"', l, "Processing Output").run())
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

	public boolean openInput() {
		if (configuration.doInput) {

			movie = new VideoCapture(configuration.inputVideo);

			if (!movie.isOpened()) {
				System.err.println("Input Movie Opening Error for " + configuration.inputVideo);
				return false;
			} else {
				movie.read(frame);

				// get meta data
				movie_fps = movie.get(Videoio.CAP_PROP_FPS);
				movie_frameCount = movie.get(Videoio.CAP_PROP_FRAME_COUNT);
				movie_h = movie.get(Videoio.CAP_PROP_FRAME_HEIGHT);
				movie_w = movie.get(Videoio.CAP_PROP_FRAME_WIDTH);
				System.out.println("Dimensions: " + movie_w + "x" + movie_h);
				System.out.println("fps: " + movie_fps + "  frameCount: " + movie_frameCount);
			}
		} else {
			movie_fps = 20;
			movie_frameCount = 1000;
			movie_h = ((TestImage) configuration.findFilter(TestImage.class)).getHeight();
			movie_w = ((TestImage) configuration.findFilter(TestImage.class)).getWidth();
			;
			frame = Mat.zeros((int) movie_w, (int) movie_h, CvType.CV_8UC3);
		}
		return true;
	}

	public void closeInput() {
		if (configuration.doInput) {
			movie.release();
			movie = null;
		}
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
		if (l != null)
			l.nextFrameProcessed(newFrame, 1);

		if (configuration.doOutput) {
			outputVideo.open(tempVideoFile.getAbsolutePath(), fourcc, movie_fps,
					new Size(newFrame.cols(), newFrame.rows()), true);
			System.err.println("newsize=" + new Size(newFrame.cols(), newFrame.rows()));

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
	}

	public void seek(final ProcessingListener l, int frameId) {
		if (configuration.doInput && frameId < currentPos) {
			if (l != null)
				l.seeking(0);
			if (movie != null)
				movie.release();
			movie = new VideoCapture(configuration.inputVideo);
			currentPos = 0;
		}
		if (configuration.doInput && !movie.isOpened()) {
			System.err.println("Input Movie Opening Error");
		} else {
			if (configuration.doInput) {
				for (int i = currentPos + 1; i <= frameId; i++) {
					if (l != null)
						l.seeking(i);
					if (!movie.grab()) {
						if (i <= movie_frameCount && l != null) {
							l.prematureEnd(i - 2);
							frameId = i - 1;
							currentPos = i - 1;
						}
					}
				}
				if (l != null)
					l.seeking(frameId);
				movie.retrieve(frame);
				currentPos = frameId;
			}
			if (!frame.empty()) {
				Mat newFrame = frame;
				for (VideoFilter filter : filters) {
					newFrame = filter.process(newFrame, frameId);
				}
				if (l != null)
					l.nextFrameProcessed(newFrame, frameId);
			} else {
				if (frameId <= movie_frameCount && l != null)
					l.prematureEnd(frameId - 1);
			}
		}
		l.seekDone(frameId);
	}

	private class Task {

		private String[] args;
		private String command;
		private boolean shutdown = false;
		private File log;
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
				shutdown = true;
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				shutdown = true;
				return false;
			} catch (InterruptedException e) {
				e.printStackTrace();
				shutdown = true;
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
				String pattern = "time=";
				Pattern p = Pattern.compile(pattern);
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