package de.serviceflow.frankenstein.plugin.opencv;

import de.serviceflow.frankenstein.plugin.api.DefaultSegmentConfigController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

public class ColorWheelConfigController extends DefaultSegmentConfigController {

	@FXML
	CheckBox cbPositive;

	boolean positive = true;
	
	public boolean isPositive() {
		return cbPositive.isSelected();
	}

	public void initialize() {
		cbPositive.setSelected(positive);
		cbPositive.selectedProperty().addListener((observable, oldvalue, newvalue) -> {
			positive = newvalue;
			fireChange();
		});
	}

}
