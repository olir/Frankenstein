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
package de.screenflow.frankenstein.fxml;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Range;

import de.screenflow.frankenstein.Configuration;
import de.screenflow.frankenstein.MovieProcessor;
import de.screenflow.frankenstein.ProcessingListener;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ProcessingSceneController implements ProcessingListener {
	// the FXML area for showing the current frame
	@FXML
	private ImageView currentFrame;

	private boolean processingRunning = false;
	private boolean seeking = false;
	private boolean seekingErrorHandling = false;

	private double taskPosition = -1; // seconds
	private int position = 0;
	private int markPosition = -1;
	private int seekPos;

	private double fps;
	private int frames;

	private FilterElement selectedFilter;

	private MovieProcessor processor;

	private Main main;

	// private Stage stage;

	private Range clipBoardRange = null;

	public final ObservableList<FilterElement> filterListData = FXCollections.observableArrayList();

	@FXML
	BorderPane rootBorder;

	@FXML
	TextField currentTime;

	@FXML
	TextField currentFrameIndex;

	// the FXML video position
	@FXML
	private Slider slider;

	@FXML
	Label maxTime;

	@FXML
	Label maxFrameIndex;

	@FXML
	Button configureButton;

	@FXML
	Button startButton;

	@FXML
	Canvas editCanvas;

	@FXML
	Button btnClear;

	@FXML
	Button btnMark;

	@FXML
	Button btnCopy;

	@FXML
	Button btnPaste;

	@FXML
	ToggleButton btnOneFrame;

	@FXML
	Label l_durationTitle;

	@FXML
	Label l_durationTime;

	@FXML
	Label l_durationFrames;

	@FXML
	ListView<FilterElement> listViewFilter;

	@FXML
	Button btnListAdd;

	@FXML
	Button btnListFilter;

	@FXML
	Button btnListDelete;

	private String taskMessage;

	private String taskErrorMessage;

	/**
	 * Initialize method, automatically called by @{link FXMLLoader}
	 */
	public void initialize() {

		slider.setBlockIncrement(1.0);
		slider.setMajorTickUnit(1.0);
		slider.setMinorTickCount(0);
		slider.setSnapToTicks(true);
		slider.valueProperty().addListener((observable, oldvalue, newvalue) -> {
			System.out.println("Slider " + oldvalue + " -> " + newvalue);
			if (!processingRunning) {
				int p = newvalue.intValue();
				if (p != position && p <= frames) {
					if (markPosition != -1)
						btnOneFrame.setDisable(p != markPosition);
					int oldposition = position;
					position = p;
					updateDuration();
					if (!seeking) {
						seeking = true;
						System.out.println("Seek for #" + position + " from #" + oldposition);
						Runnable r = new Runnable() {
							public void run() {
								seekPos = -1;
								processor.seek(ProcessingSceneController.this, position);
							}
						};
						new Thread(r).start();
					}
				}
			}
		});
		listViewFilter.setItems(filterListData);
		listViewFilter.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FilterElement>() {
			@Override
			public void changed(ObservableValue<? extends FilterElement> observable, FilterElement oldValue,
					FilterElement newValue) {
				selectedFilter = newValue;
				btnListDelete.setDisable(newValue == null);
				btnListFilter.setDisable(newValue == null);
			}
		});

		updateDuration();

		Platform.runLater(() -> {
			// this.currentFrame.fitWidthProperty().bind(currentFrame.getScene().widthProperty());
			this.currentFrame.setPreserveRatio(true);

			startButton.setDisable(true);
			btnMark.setDisable(true);
			btnOneFrame.setDisable(true);
			btnClear.setDisable(true);
			btnCopy.setDisable(true);
			btnPaste.setDisable(true);
			btnListAdd.setDisable(true);
			btnListFilter.setDisable(true);
			btnListDelete.setDisable(true);

		});

	}

	public void drawEditCanvas() {
		GraphicsContext gc = editCanvas.getGraphicsContext2D();

		// Time in seconds at position
		// double t = (position - 1) / fps;
		// Total time in seconds
		double tt = (frames - 1) / fps;

		// Background
		try {
			gc.setFill(rootBorder.getBackground().getFills().get(0).getFill());
		} catch (Exception e) {
			gc.setFill(Color.WHITE);
		}
		gc.fillRect(0, 0, editCanvas.getWidth(), editCanvas.getHeight());

		if (taskErrorMessage != null) {
			gc.setFill(Color.DARKRED);
			gc.fillText(taskErrorMessage, 10, 16, 900);
			return;
		} else if (processingRunning) {
			// Task Progress
			if (taskPosition != -1) {
				int x = (int) ((editCanvas.getWidth() - 1) * taskPosition / ((double) frames - 1) * fps);
				// System.out.println("taskPosition "+taskPosition+" "+x);
				gc.setFill(Color.DARKGREEN);
				gc.fillRect(0, 0, x, 2);
				gc.setFill(Color.BLACK);
				gc.fillText(taskMessage, 10, 16, 900);
			} else {
				// Video Progress
				int x = (int) ((editCanvas.getWidth() - 1) * (position - 1) / (frames - 1));
				gc.setFill(Color.DARKOLIVEGREEN);
				gc.fillRect(0, 0, x, 2);
				gc.setFill(Color.BLACK);
				gc.fillText("Processing Video", 10, 16, 900);
			}
		} else {
			// Cuttings
			gc.setFill(Color.CADETBLUE);
			gc.fillRect(0, 3, editCanvas.getWidth(), 6);
		}
		if (seeking && seekPos > 0) {
			// Seek running
			int x = (int) ((editCanvas.getWidth() - 1) * (seekPos - 1) / (frames - 1));
			gc.setFill(Color.DARKOLIVEGREEN);
			gc.fillRect(0, 0, x, 2);
		}

		// Time Ruler
		double t1off = 1.0;
		double t2off = 0.1;
		int tlevel = 1;
		int ttlevel = 0;
		if (tt >= 10.0) {
			t1off = 10.0;
			t2off = 1.0;
			tlevel++;
		}
		if (tt >= 60.0) {
			t1off = 60.0;
			t2off = 10.0;
			tlevel++;
			ttlevel++;
		}
		if (tt >= 3600.0) {
			t1off = 3600.0;
			t2off = 900.0;
			tlevel++;
			ttlevel++;
		}
		double tm = (((double) editCanvas.getWidth()) - 1.0) / tt;
		gc.setFill(colorByLevel(tlevel));
		for (double tx = 0.0; tx < tt; tx += t1off)
			gc.fillRect(tx * tm - ttlevel, 0, 1 + ttlevel + ttlevel, tlevel * 2);
		gc.setFill(colorByLevel(tlevel - 1));
		for (double tx = 0.0; tx < tt; tx += t2off)
			gc.fillRect(tx * tm, 1, 1, tlevel * 2 - 1);
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 2, editCanvas.getWidth(), 1);
		gc.fillRect(0, 2, 1, 16);
		gc.fillRect(editCanvas.getWidth() - 1, 2, 1, 16);

		// Mark to Position
		Range r = clipBoardRange;
		if (r != null) {
			if (markPosition != -1)
				gc.setFill(Color.rgb(64, 64, 192, 0.5));
			else
				gc.setFill(Color.rgb(128, 128, 64, 0.5));
			gc.fillRect(xForPosition(r.start), 7, xForPosition(r.end) - xForPosition(r.start) + 1, 10);
		}

		// Clipboard
		r = currentRange();
		if (r != null) {
			gc.setFill(Color.rgb(128, 128, 64, 0.5));
			gc.fillRect(xForPosition(r.start), 7, xForPosition(r.end) - xForPosition(r.start), 10);
		}

		// Current Position
		int x = xForPosition(position);
		gc.setFill(Color.RED);
		gc.fillRect(x, 0, 1, editCanvas.getHeight());
	}

	private Range currentRange() {
		if (markPosition != -1) {
			int x1 = 0, x2 = 0;
			if (markPosition > position) {
				x1 = position;
				x2 = markPosition;
			} else if (markPosition < position) {
				x1 = markPosition;
				x2 = position;
			} else if (btnOneFrame.isSelected()) {
				x1 = markPosition;
				x2 = markPosition;
			} else {
				int mx = ((int) frames) >> 1;
				if (position < mx) {
					x1 = 1;
					x2 = position;
				} else {
					x1 = position;
					x2 = (int) frames;
				}
			}
			return new Range(x1, x2);
		} else
			return null;
	}

	private Color colorByLevel(int tlevel) {
		switch (tlevel) {

		case 4:
		case 3:
			return Color.BLACK;
		case 2:
			return Color.DARKSLATEGRAY;
		case 1:
			return Color.DIMGREY;
		case 0:
		default:
			return Color.GREY;
		}
	}

	private int xForPosition(int p) {
		return (int) ((editCanvas.getWidth() - 1) * (p - 1) / (frames - 1));
	}

	public void configure(Main main, Stage stage) {
		this.main = main;
		// this.stage = stage;
	}

	public void initProcessing(ConfigurationSceneController cController, Configuration configuration) {
		markPosition = -1;
		taskPosition = -1;
		selectedFilter = null;
		filterListData.clear();
		this.taskMessage = "";
		this.taskErrorMessage = null;
		processor = new MovieProcessor(configuration);
		try {
			processor.init(ProcessingSceneController.this);
			Platform.runLater(() -> {
				drawEditCanvas();
				startButton.setDisable(false);
				btnMark.setDisable(false);
				btnOneFrame.setDisable(true);
				btnClear.setDisable(true);
				btnCopy.setDisable(true);
				btnPaste.setDisable(true);
				btnListAdd.setDisable(true);
				btnListFilter.setDisable(true);
				btnListDelete.setDisable(true);
			});
		} catch (CvException e) {
			taskError(e.toString());
		}
	}

	public void runProcessing(Configuration configuration) {

		processingRunning = true;
		markPosition = -1;

		Runnable r = new Runnable() {
			public void run() {

				Platform.runLater(() -> {
					btnMark.setDisable(true);
					btnOneFrame.setDisable(true);
					btnClear.setDisable(true);

					slider.setValue(1);
					slider.setDisable(true);
				});

				long seconds = System.currentTimeMillis() / 1000;

				if (processor.process(ProcessingSceneController.this)) {

					seconds = System.currentTimeMillis() / 1000 - seconds;

					System.out.println("Done in " + seconds + "s.");
				}

				Platform.runLater(() -> {
					processingRunning = false;
					taskMessage = "";
					processingDone();
					slider.setDisable(false);
				});
			}
		};
		new Thread(r).start();
	}

	@Override
	public void videoStarted(int frames, double fps) {
		this.fps = fps;
		this.frames = frames;

		adjustVideoLengthDisplay();

		Platform.runLater(() -> {
			this.slider.setMin(1);
			this.slider.setValue(1);
			this.currentFrameIndex.setText("1");
			this.currentTime.setText("" + time(0));
			btnMark.setDisable(false);
		});

	}

	private void adjustVideoLengthDisplay() {
		Platform.runLater(() -> {
			this.slider.setMax(frames);

			this.maxFrameIndex.setText("/ " + (int) frames);
			this.maxTime.setText("/ " + time(((double) (frames - 1)) / fps));
		});
	}

	private String time(double t) {
		LocalTime lt = LocalTime.ofNanoOfDay((long) (t * 1000000000.0));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SS");

		String text = lt.format(formatter);

		return text;
	}

	private double time(String t) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SS");
		LocalTime lt = LocalTime.parse(t, formatter);
		return ((double) lt.toNanoOfDay()) / 1000000000.0;
	}

	@Override
	public void taskUpdate(String timeStamp, String message) {
		this.taskMessage = message;
		if (timeStamp == null)
			taskPosition = -1;
		else
			taskPosition = time(timeStamp);
		Platform.runLater(() -> {
			drawEditCanvas();
		});

	}

	@Override
	public void taskError(String errorMessage) {
		this.taskErrorMessage = errorMessage;
		Platform.runLater(() -> {
			startButton.setDisable(true);
			drawEditCanvas();
			btnMark.setDisable(true);
			btnClear.setDisable(true);
			btnCopy.setDisable(true);
			btnPaste.setDisable(true);
			btnListAdd.setDisable(true);
			btnListFilter.setDisable(true);
			btnListDelete.setDisable(true);
		});

	}

	@Override
	public void nextFrameLoaded(Mat frame, int frameId) {
	}

	@Override
	public void nextFrameProcessed(Mat frame, int frameId) {
		Platform.runLater(() -> {
			// System.out.println("nextFrameProcessed "+frameId);
			this.currentFrameIndex.setText("" + frameId);
			this.currentTime.setText("" + time((frameId - 1) / fps));

			Image imageToShow = SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
			currentFrame.imageProperty().set(imageToShow);
			if (processingRunning) {
				position = frameId;
				drawEditCanvas();
			}

		});
	}

	/**
	 * Support for the {@link mat2image()} method
	 *
	 * @param original
	 *            the {@link Mat} object in BGR or grayscale
	 * @return the corresponding {@link BufferedImage}
	 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
	 * @author <a href="http://max-z.de">Maximilian Zuleger</a>
	 */

	private static BufferedImage matToBufferedImage(Mat original) {
		BufferedImage image = null;
		int width = original.width(), height = original.height(), channels = original.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		original.get(0, 0, sourcePixels);

		if (original.channels() > 1) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		} else {
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}

		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

		return image;
	}

	@Override
	public void seekDone(int frameId) {
		System.out.println("Seeking done for #" + frameId + "(pos=" + position + ")");
		Platform.runLater(() -> {
			drawEditCanvas();
		});

		if (!seekingErrorHandling) {
			if (frameId == position) {
				seeking = false;
			} else {
				System.out.println("Re-Seek for #" + position + " from #" + seekPos);
				Runnable r = new Runnable() {
					public void run() {
						processor.seek(ProcessingSceneController.this, position);
					}
				};
				new Thread(r).start();
			}
		} else {
			seeking = false;
			seekingErrorHandling = false;
		}
	}

	public void processingDone() {
		Platform.runLater(() -> {
			drawEditCanvas();
			startButton.setDisable(false);
			configureButton.setDisable(false);
			btnMark.setDisable(false);
		});
	}

	@FXML
	public void startButtonPressed() {
		Configuration configuration = main.getConfiguration();
		Platform.runLater(() -> {
			startButton.setDisable(true);
			configureButton.setDisable(true);
		});
		runProcessing(configuration);
	}

	@FXML
	public void configureButtonPressed() {
		main.showConfigure();
	}

	@FXML
	public void createMark() {
		markPosition = position;
		Platform.runLater(() -> {
			btnMark.setDisable(false);
			btnOneFrame.setDisable(false);
			btnClear.setDisable(false);
			btnCopy.setDisable(false);
			btnListAdd.setDisable(false);
			drawEditCanvas();
			updateDuration();
		});

	}

	@FXML
	public void clearMark() {
		markPosition = -1;
		clipBoardRange = null;
		Platform.runLater(() -> {
			btnMark.setDisable(false);
			btnOneFrame.setDisable(true);
			btnClear.setDisable(true);
			btnCopy.setDisable(true);
			btnPaste.setDisable(true);
			btnListAdd.setDisable(true);
			drawEditCanvas();
			updateDuration();
		});
	}

	@FXML
	public void copy() {
		clipBoardRange = currentRange();
		markPosition = -1;
		btnClear.setDisable(true);
		btnMark.setDisable(false);
		btnCopy.setDisable(true);
		btnPaste.setDisable(false);
		btnListAdd.setDisable(true);
		Platform.runLater(() -> {
			drawEditCanvas();
			updateDuration();
		});
	}

	@FXML
	public void paste() {
		System.err.println("paste");
	}

	@FXML
	public void oneFrameChanged() {
		Platform.runLater(() -> {
			drawEditCanvas();
			updateDuration();
		});
	}

	@Override
	public void seeking(int i) {
		if (i <= frames) {
			seekPos = i;
			Platform.runLater(() -> {
				drawEditCanvas();
			});
		}
	}

	@Override
	public void prematureEnd(int realFrameCount) {
		frames = realFrameCount;
		position = realFrameCount;
		seekingErrorHandling = true;
		seekPos = -1;
		System.err.println("Warning: Premature end of source at frame " + realFrameCount);
		Platform.runLater(() -> {
			this.slider.setValue(realFrameCount);
			adjustVideoLengthDisplay();
			drawEditCanvas();
		});
	}

	private void updateDuration() {
		Range r = currentRange();
		if (r != null) {
			Platform.runLater(() -> {
				l_durationFrames.setText("" + (r.end - r.start + 1));
				l_durationTime.setText("" + time((r.end - r.start) / fps));

				l_durationTitle.setVisible(true);
				l_durationTime.setVisible(true);
				l_durationFrames.setVisible(true);
			});
		} else {
			Platform.runLater(() -> {
				l_durationTitle.setVisible(false);
				l_durationTime.setVisible(false);
				l_durationFrames.setVisible(false);
			});
		}
	}

	@FXML
	public void filterAdd() {
		FilterElement val = new FilterElement(currentRange());
		filterListData.add(val);
		clearMark();
	}

	@FXML
	public void filterSetup() {
	}

	@FXML
	public void filterDelete() {
		filterListData.remove(selectedFilter);
		listViewFilter.getSelectionModel().clearSelection();
	}

	class FilterElement {
		Range r;

		FilterElement(Range r) {
			FilterElement.this.r = r;
		}

		public String toString() {
			return r.toString();
		}
	}

	@FXML
	public void tfActionTime(ActionEvent event) {
		try {
			String newTime = currentTime.getText();
			int newPosition = (int) (time(newTime) * fps) + 1;
			if (newPosition >= 1 && newPosition <= frames) {
				position = newPosition;
			}
		} catch (DateTimeParseException e) {
			// position unchanged
		}
		currentFrameIndex.setText(String.valueOf(position));
		currentTime.setText(time(((double) position - 1) / fps));
		this.slider.setValue(position);
		drawEditCanvas();
		if (markPosition != -1)
			btnOneFrame.setDisable(position != markPosition);
	}

	@FXML
	public void tfActionFrame(ActionEvent event) {
		try {
			String newFrameIndex = currentFrameIndex.getText();
			int newPosition = Integer.parseInt(newFrameIndex);
			if (newPosition >= 1 && newPosition <= frames)
				position = newPosition;
		} catch (NumberFormatException e) {
			// position unchanged
		}
		currentFrameIndex.setText(String.valueOf(position));
		currentTime.setText(time(((double) position - 1) / fps));
		this.slider.setValue(position);
		drawEditCanvas();
		if (markPosition != -1)
			btnOneFrame.setDisable(position != markPosition);
	}

}