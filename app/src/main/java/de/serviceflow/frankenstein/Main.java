package de.serviceflow.frankenstein;

import org.opencv.core.CvException;
import org.opencv.core.Mat;

import de.serviceflow.frankenstein.fxml.FxMain;
import de.serviceflow.frankenstein.fxml.ProcessingSceneController;
import de.serviceflow.frankenstein.plugin.api.DefaultSegmentConfigController;
import de.serviceflow.frankenstein.plugin.api.SegmentVideoFilter;
import de.serviceflow.frankenstein.vf.VideoStreamSource;
import javafx.application.Platform;

public class Main {
	public static void main(String[] args) {

		Configuration c = Configuration.cliCreateConfiguration(args);
		if (c == null) {
			System.out.println(Configuration.getUsage());
			System.exit(0);
		}

		c.init();

		if (c.isVisual()) {
			FxMain.fxmain(c);
		} else {
			new Main().runcli(c);
		}
	}

	private void runcli(Configuration configuration) {
		CliProcessingListener l = new CliProcessingListener();
		MovieProcessor processor = new MovieProcessor(configuration);
		try {
			processor.init(l);
			runProcessing(processor, l);
		} catch (CvException e) {
			// taskError(e.toString());
		}
	}

	private void runProcessing(MovieProcessor processor, CliProcessingListener l) {
		Runnable r = new Runnable() {
			public void run() {

				long seconds = System.currentTimeMillis() / 1000;

				if (processor.processVideo(l)) {

					seconds = System.currentTimeMillis() / 1000 - seconds;

					System.out.println("Done in " + seconds + "s.");
				} else {
					// taskError("Task failed");
				}
			}
		};
		ExecutorThread.getInstance().execute(r, new Runnable() {
			@Override
			public void run() {
				ExecutorThread.shutdown();
				System.exit(0);
			}
		});
		
	}
	
	private class CliProcessingListener implements ProcessingListener {

		@Override
		public void configChanged(DefaultSegmentConfigController segmentConfigController,
				SegmentVideoFilter selectedFilter) {
			// TODO Auto-generated method stub

		}

		@Override
		public void videoStarted(int frames, double fps) {
			// TODO Auto-generated method stub

		}

		@Override
		public void nextFrameLoaded(VideoStreamSource s) {
			// TODO Auto-generated method stub

		}

		@Override
		public void nextFrameProcessed(Mat frame, int frameId) {
			// TODO Auto-generated method stub

		}

		@Override
		public void seekDone(int frameId) {
			// TODO Auto-generated method stub

		}

		@Override
		public void seeking(int i) {
			// TODO Auto-generated method stub

		}

		@Override
		public void prematureEnd(int realFrameCount) {
			// TODO Auto-generated method stub

		}

		@Override
		public void taskUpdate(String timeStamp, String message) {
			// TODO Auto-generated method stub

		}

		@Override
		public void taskError(String errorMessage) {
			// TODO Auto-generated method stub

		}

	}
}
