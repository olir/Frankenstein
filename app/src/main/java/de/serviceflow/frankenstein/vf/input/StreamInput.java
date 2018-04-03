package de.serviceflow.frankenstein.vf.input;

import java.io.File;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import de.serviceflow.frankenstein.ProcessingListener;
import de.serviceflow.frankenstein.vf.VideoStreamSource;

public class StreamInput implements VideoStreamSource {

	private VideoCapture movie = null;
	private final String videofilename, streamurl;
	private final File videofile;
	private Mat currentFrame;
	private double fps = 10.0;
	private int width;
	private int height;
	private int currentPos;
	private int frames;
	private final String[] commandBuffer = new String[11];
	private Process process = null;

	private Timer timer = null;

	/**
	 *
	 * @param streamurl
	 *
	 * @param videofilename
	 */
	public StreamInput(String streamurl, String videofilename) {
		this.videofilename = videofilename;
		this.streamurl = streamurl;
		this.videofile = videofilename != null ? new File(videofilename) : null;

		if (this.videofile != null) {
			// VLC-Version 2.2.8 Weatherwax (2.2.8-2-gf5224a0)
			commandBuffer[0] = "Cmd";
			commandBuffer[1] = "/C";
			commandBuffer[2] = "Start";
			commandBuffer[3] = "/Wait";
			commandBuffer[4] = "vlc.exe";
			commandBuffer[5] = "-I";
			commandBuffer[6] = "null";
			commandBuffer[7] = "--no-one-instance";
			commandBuffer[8] = streamurl;
			commandBuffer[9] = "--sout=\"#standard{access=file,mux=ps,dst=" + videofile.getAbsolutePath() + "}\"";
			commandBuffer[10] = "vlc://quit";
		}
	}

	@Override
	public int getFrames() {
		return frames;
	}

	@Override
	public double getFps() {
		return fps;
	}

	@Override
	public void open(ProcessingListener l) {
		boolean record = videofilename != null;
		if (record) {
			// Save stream to file
			process = executeCommand(commandBuffer);
			try {
				while (!videofile.exists()) {
					Thread.sleep(250L);
				}
				System.out.println("Streaming to " + videofile.getAbsolutePath());

				// open file
				movie = new VideoCapture(videofile.getAbsolutePath());
				while (!movie.isOpened()) {
					movie.release();
					Thread.sleep(250L);
					movie = new VideoCapture(videofile.getAbsolutePath());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			// open stream capture
			movie = new VideoCapture(streamurl);
			if (!movie.isOpened()) {
				throw new RuntimeException("Error opening stream " + streamurl);
			}
		}

		currentFrame = new Mat();
		currentPos = 0;
		grab();
		currentFrame = retrieve(currentFrame, l);
		frames = 1;
		fps = movie.get(Videoio.CAP_PROP_FPS);
		width = (int) movie.get(Videoio.CAP_PROP_FRAME_WIDTH);
		height = (int) movie.get(Videoio.CAP_PROP_FRAME_HEIGHT);
		System.out.println("open:  " + width + "x" + height);

		// stream from file
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					Thread.sleep((int) (1000.0 / fps));
				} catch (InterruptedException e) {
				}
				if (!grab()) {
					System.out.println("grab failed");
					return;
				}
				currentPos++;
				frames++;
				currentFrame = retrieve(currentFrame, l);
				l.nextFrameLoaded(StreamInput.this);
			}
		}, 2000, (int) (1000.0 / fps));
	}

	public void pause() {
		try {
			Thread.sleep((int) (1000.0 / fps));
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void close() {
		if (process != null) {
			process.destroy();
			;
			process = null;
		}
		if (movie != null)
			movie.release();
		movie = null;
		if (timer != null)
			timer.cancel();
		timer = null;
	}

	@Override
	public void reopen(ProcessingListener l) {
		close();
		open(l);
	}

	private Mat retrieve(Mat frame, ProcessingListener l) {
		// boolean status =
				movie.retrieve(currentFrame); // ignore boolean
		// System.out.println("retrieve: " + status);
		return currentFrame;
	}

	private boolean grab() {
		return movie.grab();
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public Mat getFrame() {
		return currentFrame;
	}

	@Override
	public int seek(int pos, ProcessingListener l) {
		currentFrame = retrieve(currentFrame, l);
		return currentPos;
	}

	public Process executeCommand(String... command) {
		System.out.println("Execute (" + Arrays.toString(command) + ")");

		Process p = null;
		try {
			p = Runtime.getRuntime().exec(command);
			final Process shp = p;
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				shp.destroy();
			}));
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}

		return p;
	}

	public int getCurrentPos() {
		return currentPos;
	}

}