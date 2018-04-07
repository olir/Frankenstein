package de.serviceflow.frankenstein.plugin.opencv;

import de.serviceflow.frankenstein.plugin.api.DefaultSegmentConfigController;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;

public class VideoEqualizerConfigController extends DefaultSegmentConfigController {

	@FXML
	Slider slBrightness;
	@FXML
	Slider slSaturation;
	@FXML
	Slider slContrast;

	/**
	 * 0 ... 100
	 */
	private int brightness = 50;
	/**
	 * 0 ... 100
	 */
	private int saturation = 50;
	/**
	 * 0 ... 100
	 */
	private int contrast = 50;

	public void initialize() {
		slBrightness.setValue(brightness);
		slBrightness.valueProperty().addListener((observable, oldvalue, newvalue) -> {
			brightness = newvalue.intValue();
			fireChange();
		});
		slSaturation.setValue(saturation);
		slSaturation.valueProperty().addListener((observable, oldvalue, newvalue) -> {
			saturation = newvalue.intValue();
			fireChange();
		});
		slContrast.setValue(contrast);
		slContrast.valueProperty().addListener((observable, oldvalue, newvalue) -> {
			contrast = newvalue.intValue();
			fireChange();
		});
	}

	int getBrightness() {
		return brightness;
	}

	void setBrightness(int brightness) {
		this.brightness = brightness;
	}

	int getSaturation() {
		return saturation;
	}

	void setSaturation(int saturation) {
		this.saturation = saturation;
	}

	int getContrast() {
		return contrast;
	}

	void setContrast(int contrast) {
		this.contrast = contrast;
	}

}
