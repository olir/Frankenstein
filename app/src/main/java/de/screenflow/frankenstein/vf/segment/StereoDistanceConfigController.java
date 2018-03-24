package de.screenflow.frankenstein.vf.segment;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;

public class StereoDistanceConfigController extends SegmentConfigController {

	@FXML
	Slider sliderStereoPerspective;

	public void initialize() {
		sliderStereoPerspective.setValue(perspective);
		sliderStereoPerspective.valueProperty().addListener((observable, oldvalue, newvalue) -> {
			perspective = newvalue.intValue();
			fireChange();
		});
	}

	/**
	 * -2n ... -4,-2,0,2,4, ... +2n : Negative values for farer away, positive
	 * for closer
	 */
	private int perspective = 0;

	public int getPerspective() {
		return perspective;
	}

}
