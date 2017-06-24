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

import java.io.File;
import java.util.List;

import de.screenflow.frankenstein.Configuration;
import de.screenflow.frankenstein.vf.Anaglyph2LR;
import de.screenflow.frankenstein.vf.CloneLR;
import de.screenflow.frankenstein.vf.LDelay;
import de.screenflow.frankenstein.vf.LR2VR180;
import de.screenflow.frankenstein.vf.OU2LR;
import de.screenflow.frankenstein.vf.OutputSizeLimiter;
import de.screenflow.frankenstein.vf.RL2LR;
import de.screenflow.frankenstein.vf.StereoEffect;
import de.screenflow.frankenstein.vf.TestImage;
import de.screenflow.frankenstein.vf.VideoFilter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ConfigurationSceneController {

	// the FXML video position
	@FXML
	private Button doneButton;

	private Main main;

	private Stage stage;

	private Configuration configuration;
	private FileChooser.ExtensionFilter inputFileFilter = new FileChooser.ExtensionFilter("Video Files", "*.mp4",
			"*.avi", "*.wmv", "*.mks", "*.mpg", "*.mov");

	@FXML
	RadioButton rVideoFileInput;

	@FXML
	RadioButton rTestVideoGenerator;

	@FXML
	RadioButton rNoNormalization;

	@FXML
	RadioButton rAnaglyph;

	@FXML
	RadioButton rOverUnder;

	@FXML
	RadioButton rCloneLR;

	@FXML
	RadioButton rDelayLeft;

	@FXML
	RadioButton r3DConverter;

	@FXML
	RadioButton rNoAlignment;

	@FXML
	RadioButton rSwapLR;

	@FXML
	RadioButton rNoVR;

	@FXML
	RadioButton rVRConverter;

	@FXML
	RadioButton rNoOutput;

	@FXML
	RadioButton rVideoFileOutput;

	@FXML
	CheckBox cPostProcessing;

	@FXML
	TabPane tabPane;

	@FXML
	Tab tabVideoFileInput;

	@FXML
	Tab tabTestVideoGenerator;

	@FXML
	Tab tabAnaglyph;

	@FXML
	Tab tabOverUnder;

	@FXML
	Tab tabClone;

	@FXML
	Tab tabDelay;

	@FXML
	Tab tabSwap;

	@FXML
	Tab tabVRConverter;

	@FXML
	Tab tabVideoFileOutput;

	@FXML
	Tab tabPostProcessing;

	/**
	 * Initialize method, automatically called by @{link FXMLLoader}
	 */
	public void initialize() {
		configuration = new Configuration(new Configuration.ConfigHelper() {
			public File getFFmpegPath() {
				DirectoryChooser dirChooser = new DirectoryChooser();
				dirChooser.setTitle("Select FFmpeg Home Path");
				return dirChooser.showDialog(stage);
			}
		});

		removeTab(tabVideoFileInput);
		addTab(tabTestVideoGenerator);
		removeTab(tabAnaglyph);
		removeTab(tabDelay);
		removeTab(tabOverUnder);
		addTab(tabClone);
		removeTab(tabSwap);
		removeTab(tabVRConverter);
		addTab(tabVideoFileOutput);
		removeTab(tabPostProcessing);

		rTestVideoGenerator.setSelected(true);
		rCloneLR.setSelected(true);
		rNoAlignment.setSelected(true);
		rNoVR.setSelected(true);
		rVideoFileOutput.setSelected(true);

	}

	public void configure(Main main, Stage stage) {
		this.main = main;
		this.stage = stage;
	}

	@FXML
	public void doneButtonPressed(ActionEvent event) {
		List<VideoFilter> filters = configuration.filters;

		filters.clear();

		configuration.doInput = rVideoFileInput.isSelected();

		if (rTestVideoGenerator.isSelected()) {
			//filters.add(new TestImage(640, 480));
			// filters.add(new TestImage(1080, 1920));
			// filters.add(new TestImage(1024, 768));
			// filters.add(new LRTestImage(1152, 648));
			 filters.add(new TestImage(1280,720));
		}

		if (rAnaglyph.isSelected())
			filters.add(new Anaglyph2LR(Anaglyph2LR.KEEP_WIDTH));

		if (rOverUnder.isSelected())
			filters.add(new OU2LR(OU2LR.REDUCE_SIZE));
		// filters.add(new OU2LR(OU2LR.ADJUST_SIZE));

		if (rDelayLeft.isSelected())
			filters.add(new LDelay());

		if (rCloneLR.isSelected())
			filters.add(new CloneLR());

		if (rSwapLR.isSelected())
			filters.add(new RL2LR());

		if (rVRConverter.isSelected())
			filters.add(new LR2VR180(LR2VR180.PAD_3D_TO_VR));
		// filters.add(new LR2VR(LR2VR.SHRINK_VR_ONLY));

		if (cPostProcessing.isSelected())
			filters.add(new StereoEffect(32, 0));

		configuration.doOutput = rVideoFileOutput.isSelected();

		if (configuration.doOutput) {
			filters.add(new OutputSizeLimiter(2880));
		}

		main.showProcessing(configuration);
	}

	private void addTab(Tab tab) {
		if (!tabPane.getTabs().contains(tab)) {
			tabPane.getTabs().add(tab);
			tabPane.getSelectionModel().select(tab);
		}
	}

	private void removeTab(Tab tab) {
		if (tabPane.getTabs().contains(tab))
			tabPane.getTabs().remove(tab);
	}

	@FXML
	public void rActionVideoFileInput() {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Video Input File");
		fileChooser.getExtensionFilters().add(inputFileFilter);
		if (configuration.inputVideo != null) {
			File f = new File(configuration.inputVideo);
			if (!f.isDirectory())
				f = f.getParentFile();
			if (f == null || !f.isDirectory())
				f = new File(".");
			fileChooser.setInitialDirectory(f);
		} else
			fileChooser.setInitialDirectory(new File("."));
		File file = fileChooser.showOpenDialog(stage);
		if (file != null && file.exists()) {
			configuration.inputVideo = file.getAbsolutePath();
			configuration.outputVideo = configuration.inputVideo.substring(0, configuration.inputVideo.lastIndexOf('.'))
					+ "_edit" + ".mp4";
			main.setDocumentInTitle(file.getName());
			if (rCloneLR.isSelected())
				rNoNormalization.setSelected(true);
			addTab(tabVideoFileInput);
			removeTab(tabTestVideoGenerator);
		} else {
			main.setDocumentInTitle(null);
			rTestVideoGenerator.setSelected(true);
			if (!rCloneLR.isSelected())
				rCloneLR.setSelected(true);
		}
	}

	@FXML
	public void rActionTestVideoGenerator() {
		removeTab(tabVideoFileInput);
		addTab(tabTestVideoGenerator);
		configuration.outputVideo = new File (new File(System.getProperty("user.home")), "TestVideo.mp4").getAbsolutePath();
	}

	@FXML
	public void rActionNoNormalization() {
		removeTab(tabAnaglyph);
		removeTab(tabOverUnder);
		removeTab(tabClone);
		removeTab(tabDelay);
	}

	@FXML
	public void rActionAnaglyph() {
		addTab(tabAnaglyph);
		removeTab(tabOverUnder);
		removeTab(tabClone);
		removeTab(tabDelay);
	}

	@FXML
	public void rActionOverUnder() {
		removeTab(tabAnaglyph);
		addTab(tabOverUnder);
		removeTab(tabClone);
		removeTab(tabDelay);
	}

	@FXML
	public void rActionCloneLR() {
		removeTab(tabAnaglyph);
		removeTab(tabOverUnder);
		addTab(tabClone);
		removeTab(tabDelay);
	}

	@FXML
	public void rActionDelayLeft() {
		removeTab(tabAnaglyph);
		removeTab(tabOverUnder);
		removeTab(tabClone);
		addTab(tabDelay);
	}

	@FXML
	public void cbActionPostProcessing() {
		if (cPostProcessing.isSelected())
			addTab(tabPostProcessing);
		else
			removeTab(tabPostProcessing);
	}

	@FXML
	public void rActionNoAlignment() {
		removeTab(tabSwap);
	}

	@FXML
	public void rActionSwapLR() {
		addTab(tabSwap);
	}

	@FXML
	public void rActionNoVR() {
		removeTab(tabVRConverter);
	}

	@FXML
	public void rActionVRConverter() {
		addTab(tabVRConverter);
	}

	@FXML
	public void rActionNoOutput() {
		removeTab(tabVideoFileOutput);
	}

	@FXML
	public void rActionVideoFileOutput() {
		addTab(tabVideoFileOutput);
	}

}