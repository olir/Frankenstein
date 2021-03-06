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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoWriter;

import de.serviceflow.frankenstein.plugin.api.FilterContext;
import de.serviceflow.frankenstein.plugin.api.SegmentVideoFilter;
import de.serviceflow.frankenstein.task.Task;
import de.serviceflow.frankenstein.task.TaskHandler;
import de.serviceflow.frankenstein.task.TimeTaskHandler;
import de.serviceflow.frankenstein.vf.DefaultFilterContext;
import de.serviceflow.frankenstein.vf.FilterElement;
import de.serviceflow.frankenstein.vf.VideoFilter;
import de.serviceflow.frankenstein.vf.VideoStreamSource;
import de.serviceflow.frankenstein.vf.input.TestImageInput;

public class MovieProcessor {

	private final String ffmpegPath;
	private final File tempPath;
	private final Configuration configuration;
	private final List<VideoFilter> filters;
	private List<FilterElement> localFilters;

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
	private boolean notStream = false;
	int currentPos = 0;

	private File ffmpeg;

	private SegmentVideoFilter previewFilter = null;
	private boolean cliStop = false;

	public MovieProcessor(Configuration configuration) {
		this.ffmpegPath = configuration.getFFmpegPath();
		this.tempPath = new File(configuration.getTempPath());
		this.configuration = configuration;
		filters = configuration.getFilters();
	}

	public void init(ProcessingListener l) {
		currentPos = 1;

		if (!openInput(l)) {
			if (l != null)
				l.prematureEnd(1);
			return;
		}

		frame = configuration.getSource().getFrame();
		if (frame != null && !frame.empty()) {
			Mat newFrame = frame;
			if (!filters.isEmpty()) {
				for (VideoFilter filter : filters) {
					System.out.println("MovieProcessor configure " + filter.getClass().getName());
					newFrame = filter.configure(newFrame);
				}
			}

			FilterContext context = new DefaultFilterContext();
			newFrame = frame;
			for (VideoFilter filter : filters) {
				System.out.println("MovieProcessor process " + filter.getClass().getName());
				newFrame = filter.process(newFrame, 1, context);
			}
			if (l != null)
				l.nextFrameProcessed(newFrame, currentPos);

			movie_w = newFrame.cols();
			movie_h = newFrame.rows();
		} else {
			if (l != null)
				l.prematureEnd(1);
			return;
		}

		openOutput(l);

		// TODO: Currently Windows only
		ffmpeg = new File(ffmpegPath, "\\bin\\ffmpeg.exe");

		if (l != null)
			l.videoStarted(configuration.getSource().getFrames(), configuration.getSource().getFps());

		currentPos = 1;
	}

	public void applyLocalFilters(List<FilterElement> filterListData) {
		localFilters = filterListData;
		if (localFilters != null && !localFilters.isEmpty()) {
			FilterContext context = new DefaultFilterContext();
			for (FilterElement element : localFilters) {
				if (element.filter != null) {
					element.filter.process(frame, 1, context);
				}
			}
		}
	}

	public void processStreamFrame(ProcessingListener l) {
		currentPos = 1;

		frame = configuration.getSource().getFrame();
		if (frame != null && !frame.empty()) {
			FilterContext context = new DefaultFilterContext();
			Mat newFrame = frame;
			for (VideoFilter filter : filters) {
				// System.out.println("MovieProcessor processStreamFrame " +
				// filter.getClass().getName());
				newFrame = filter.process(newFrame, currentPos, context);
			}
			if (localFilters != null && !localFilters.isEmpty()) {
				for (FilterElement element : localFilters) {
					if (element.filter != null) {
						// System.out.println("MovieProcessor
						// processStreamFrame
						// " +
						// element.filter);
						newFrame = element.filter.process(newFrame, currentPos, context);
					}
				}
			}
			if (l != null)
				l.nextFrameProcessed(newFrame, currentPos);

			movie_w = newFrame.cols();
			movie_h = newFrame.rows();
		} else {
			if (l != null)
				l.prematureEnd(1);
		}
	}

	public boolean processVideo(ProcessingListener l) {
		try {
			notStream = !configuration.doInput || !(configuration.getSource() instanceof VideoStreamSource);
			if (!configuration.doInput || !(configuration.getSource() instanceof VideoStreamSource)) {
				System.out.print(
						"doOutput=" + configuration.doOutput + " with source=" + (configuration.getSource() != null
								? configuration.getSource().getClass().getName() : "none"));

				// 1. Detach Audio and Metadata from orginal video and store
				// temporarily
				if (configuration.doOutput && configuration.doInput
						&& !(configuration.getSource() instanceof TestImageInput)) {
					if (!new Task(this,
							ffmpeg.getAbsolutePath() + " -y -i \"" + configuration.getInputVideo() + "\""
									+ " -f ffmetadata " + tempMetadataFile.getAbsolutePath()
									+ " -vn -ar 44100 -ac 2 -ab 192k -f au -r 21 " + tempAudioFile.getAbsolutePath(),
							new TimeTaskHandler(l, "Splitting Audio")).run())
						return false;

					configuration.metadata.clear();
					configuration.metadata.load(tempMetadataFile);
					System.out.print(
							"Meta Data:\n===================\n" + configuration.metadata + "===================\n");

				} else if (configuration.doOutput) {
					// Create silent mp3
					if (!new Task(this,
							ffmpeg.getAbsolutePath() + " -y -f lavfi -i anullsrc=r=44100:cl=mono -t "
									+ (movie_frameCount / movie_fps) + " -q:a 9 -acodec libmp3lame "
									+ tempAudioFile.getAbsolutePath(),
							new TimeTaskHandler(l, "Creating Silent Audio Audio")).run())
						return false;

					configuration.metadata.clear();
					PrintWriter w = new PrintWriter(new FileWriter(tempMetadataFile));
					w.println(";FFMETADATA1");
					w.close();
					configuration.metadata.load(tempMetadataFile);

				}
			}

			// 2. Prepare Audio Processing
			if (configuration.doOutput && tempAudioFile != null && tempAudioFile.exists()) {
				try {
					System.out.println("Prepare audio.");
					Clip clip = AudioSystem.getClip();
					AudioInputStream stream = AudioSystem
							.getAudioInputStream(new BufferedInputStream(new FileInputStream(tempAudioFile)));
					if (stream != null) {
						final AudioFormat baseFormat = stream.getFormat();
						System.out.println("baseFormat: "+baseFormat.toString());
						AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
								baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2,
								baseFormat.getSampleRate(), false);
						System.out.println("decodedFormat: "+decodedFormat.toString());
						stream = AudioSystem.getAudioInputStream(decodedFormat, stream);
						clip.open(stream);
					}
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				} catch (UnsupportedAudioFileException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if (configuration.doOutput)  {
				System.out.println("No tempAudioFile! Missing: "+(tempAudioFile != null?tempAudioFile.getAbsolutePath():"null"));
			}
			
			// 2. Process Video
			System.out.print("Processing video: ");
			Mat newFrame = null;
			if (l != null)
				l.taskUpdate(null, "Processing video");

			int i = 0;
			while (!stopped && (configuration.getSource().getFrames() < 0 || i < configuration.getSource().getFrames()
					|| !notStream)) {
				i++;
				if (notStream) {
					currentPos = configuration.getSource().seek(i, l);
					frame = configuration.getSource().getFrame();
				} else {
					((VideoStreamSource) configuration.getSource()).pause();
					frame = configuration.getSource().getFrame();
				}
				if (frame != null && !frame.empty()) {
					if (!filters.isEmpty()) {
						FilterContext context = new DefaultFilterContext();
						newFrame = frame;
						for (VideoFilter filter : filters) {
							// System.out.println("MovieProcessor
							// process"+filter.getClass().getName());
							newFrame = filter.process(newFrame, i, context);
						}
					} else {
						newFrame = frame;
					}
					if (localFilters != null && !localFilters.isEmpty()) {
						FilterContext context = new DefaultFilterContext();
						for (FilterElement element : localFilters) {
							if (element.filter != null) {
								if (element.r.start <= i && (i < element.r.end || !notStream)) {
									// System.out.println("MovieProcessor
									// processStreamFrame
									// " +
									// element.filter);
									newFrame = element.filter.process(newFrame, i, context);
								}
							}
						}
					}

					if (configuration.doOutput) {
						if (movie_w != newFrame.cols() || movie_h != newFrame.rows())
							System.out.println("Warning: outputVideo.write changed size:"
									+ new Size(newFrame.cols(), newFrame.rows()));
						outputVideo.write(newFrame);
						if ((i % 1000) == 0) {
							System.out.print("+"); // break;
						} else if ((i % 100) == 0)
							System.out.print(".");
					}

					if (l != null)
						l.nextFrameProcessed(newFrame, currentPos);

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
			if (stopped && !cliStop) {
				return false;
			}

			if (configuration.doOutput) {
				File of = findFreeFile(new File(configuration.outputVideo));

				TaskHandler handler = null;

				if (!configuration.doInput || !(configuration.getSource() instanceof VideoStreamSource)) {
					if (configuration.doInput) {
						handler = new TimeTaskHandler(l, "Assembling Output");
						if (!new Task(this,
								ffmpeg.getAbsolutePath() + " -y -i \"" + tempVideoFile.getAbsolutePath() + "\" -i "
										+ tempAudioFile.getAbsolutePath() + " -i " + tempMetadataFile.getAbsolutePath()
										+ " -map_metadata 2" + " -c:a aac -c:v libx264  -q 17 \"" + of.getAbsolutePath()
										+ '"',
								handler).run())
							return false;
					} else {
						handler = new TimeTaskHandler(l, "Processing Output");
						if (!new Task(this,
								ffmpeg.getAbsolutePath() + " -y -i \"" + tempVideoFile.getAbsolutePath() + "\" -i "
										+ tempAudioFile.getAbsolutePath() + " -c:a aac -c:v libx264  -q 17 \""
										+ of.getAbsolutePath() + '"',
								handler).run())
							System.out.println("Warning: Task failed");
					}
				} else {
					System.out.println("Renaming temp  file " + tempVideoFile.getAbsolutePath());
					tempVideoFile.renameTo(of);
				}
				if (!of.exists()) {
					if (handler != null)
						System.err.println(handler.getLogBuffer().toString());
					System.err.println("Missing output " + of.getAbsolutePath());
					return false;
				} else {
					System.out.println("Video created: " + of.getAbsolutePath());
				}
				tempVideoFile.delete();
				tempAudioFile.delete();
				tempMetadataFile.delete();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			// if (!configuration.doInput || !(configuration.getSource()
			// instanceof VideoStreamSource))
			// closeInput();
			closeOutput();
			openOutput(null);
		}
		return true;
	}

	public void stopStream() {
		notStream = true;
	}

	private File findFreeFile(File f) {
		int i = 0;
		while (f.exists()) {
			f = new File(f.getParent(), numberFileName(f.getName(), ++i));
		}
		return f;
	}

	private String numberFileName(String name, int increment) {
		int dotindex = name.lastIndexOf('.');
		int nbrindex = name.indexOf('(');
		if (nbrindex > -1 && name.indexOf(')', nbrindex + 1) < 0) {
			nbrindex = -1;
		}
		if (nbrindex < 0)
			nbrindex = dotindex;

		if (dotindex >= 0)
			return name.substring(0, nbrindex) + "(" + increment + ")" + "." + name.substring(dotindex + 1);
		else
			return name;
	}

	public static void stop() {
		stopped = true;
	}

	public boolean openInput(ProcessingListener l) {
		try {
			configuration.getSource().open(l);

			movie_fps = configuration.getSource().getFps();
			movie_frameCount = configuration.getSource().getFrames();
			movie_w = configuration.getSource().getWidth();
			movie_h = configuration.getSource().getHeight();

			frame = configuration.getSource().getFrame();

			System.out.println("Dimensions: " + (int) movie_w + " x " + (int) movie_h);
			System.out.println("fps: " + movie_fps + "  frameCount: " + (int) movie_frameCount);

			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}

	public void closeInput() {
		configuration.getSource().close();
	}

	public boolean openOutput(ProcessingListener l) {
		if (configuration.doOutput) {
			String tempOutputFormat = "avi"; // format without errors so far
			boolean compress = true;
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
				File tempFile = File.createTempFile("video", "." + tempOutputFormat, tempPath);
				tempFile.deleteOnExit();
				tempVideoFile = new File(new File(configuration.outputVideo).getParentFile(), tempFile.getName());
				tempVideoFile.deleteOnExit();
				tempAudioFile = File.createTempFile("sound", ".au", tempPath);
				tempAudioFile.deleteOnExit();
				tempMetadataFile = File.createTempFile("metadata", ".properties", tempPath);
				tempMetadataFile.deleteOnExit();
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
		if (configuration.doOutput) {
			outputVideo.open(tempVideoFile.getAbsolutePath(), fourcc, movie_fps, new Size(movie_w, movie_h), true);
			System.out.println("ConfigureOutput size=" + new Size(movie_w, movie_h) + " fps=" + movie_fps);

			if (!outputVideo.isOpened()) {
				System.err
						.println("Warning: VideoWriter - Could not open the output video for write. (MovieProcessor)");
				// return false;
			}
		}
		return true;
	}

	public void closeOutput() {
		if (!configuration.doOutput)
			return;

		outputVideo = null;
	}

	public void seek(final ProcessingListener l, int frameId) {

		// System.out.println("MovieProcessor.seek @"+frameId);
		if (configuration.doInput && frameId < currentPos) {
			if (l != null)
				l.seeking(0);

			currentPos = 0;
			configuration.getSource().reopen(l);
		}

		ExecutorThread.getInstance().execute(new Runnable() {
			@Override
			public void run() {

				currentPos = configuration.getSource().seek(frameId, l);
				frame = configuration.getSource().getFrame();
				if (frame != null && !frame.empty()) {
					FilterContext context = new DefaultFilterContext();
					Mat newFrame = frame;
					for (VideoFilter filter : filters) {
						// System.out.println("MovieProcessor process
						// "+filter.getClass().getName());
						newFrame = filter.process(newFrame, frameId, context);
					}
					if (localFilters != null && !localFilters.isEmpty()) {
						for (FilterElement element : localFilters) {
							if (element.filter != null) {
								if (element.r.start <= currentPos && currentPos < element.r.end) {
									// System.out.println("MovieProcessor
									// processStreamFrame " +
									// element.filter);
									newFrame = element.filter.process(newFrame, currentPos, context);
								}
							}
						}
					}
					if (previewFilter != null) {
						// System.out.println("MovieProcessor processStreamFrame
						// " +
						// previewFilter);
						newFrame = previewFilter.process(newFrame, currentPos, context);
					}
					if (l != null)
						l.nextFrameProcessed(newFrame, currentPos);
				} else {
					if (frameId <= movie_frameCount && l != null)
						l.prematureEnd(frameId - 1);
				}

				l.seekDone(frameId);

			}
		});

	}

	public List<String> getVideoDevices() {
		List<String> devices = new ArrayList<String>();

		// case Windows:
		if (!new Task(this, ffmpeg.getAbsolutePath() + " -y -list_devices true -f dshow -i dummy", new TaskHandler() {
			boolean inDirectShowSection = false;

			public void handleLine(String line) {
				if (!inDirectShowSection) {
					int s = line.indexOf("] DirectShow video devices");
					if (s >= 0) {
						inDirectShowSection = true;
					}
				} else {
					int s = line.indexOf("] DirectShow audio devices");
					if (s >= 0) {
						inDirectShowSection = false;
						return;
					}

					s = line.indexOf("]  \"");
					if (s > 0) {
						s++;
						int e = line.indexOf("\"", s);
						if (e > 0) {
							devices.add(line.substring(s, e));
						}
					}
				}
			}
		}).run())
			return devices;
		return devices;
	}

	public List<String> getAudioDevices() {
		List<String> devices = new ArrayList<String>();

		// case Windows:
		if (!new Task(this, ffmpeg.getAbsolutePath() + " -y -list_devices true -f dshow -i dummy", new TaskHandler() {
			boolean inDirectShowSection = false;

			public void handleLine(String line) {
				if (!inDirectShowSection) {
					int s = line.indexOf("] DirectShow audio devices");
					if (s >= 0) {
						inDirectShowSection = true;
					}
				} else {
					int s = line.indexOf("] DirectShow video devices");
					if (s >= 0) {
						inDirectShowSection = false;
						return;
					}

					s = line.indexOf("]  \"");
					if (s > 0) {
						s++;
						int e = line.indexOf("\"", s);
						if (e > 0) {
							devices.add(line.substring(s, e));
						}
					}
				}
			}
		}).run())
			return devices;
		return devices;
	}

	public void setPreviewFilter(SegmentVideoFilter selectedFilter) {
		previewFilter = selectedFilter;
	}

	public void inputReceived(String nextLine) {
		cliStop = true;
		stop();
	}
}