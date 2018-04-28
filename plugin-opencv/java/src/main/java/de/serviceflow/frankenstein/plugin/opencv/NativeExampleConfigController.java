package de.serviceflow.frankenstein.plugin.opencv;

import de.serviceflow.frankenstein.plugin.api.DefaultSegmentConfigController;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;

public class NativeExampleConfigController extends DefaultSegmentConfigController {

	@FXML
	Slider slColor;
	@FXML
	Slider slRange;
	@FXML
	Slider slSatMin;
	
	private int farbe = 0;
	private int range = 20;
	private int satmin = 128;
	
	public int getFarbe() {
		return farbe;
	}

	void setFarbe(int farbe) {
		this.farbe = farbe;
	}
	
	public void initialize() {
		slColor.setValue(farbe);
		slColor.valueProperty().addListener((observable, oldvalue, newvalue) -> {
			farbe = newvalue.intValue();
			fireChange();
		});
		slRange.setValue(range);
		slRange.valueProperty().addListener((observable, oldvalue, newvalue) -> {
			range = newvalue.intValue();
			fireChange();
		});
		slSatMin.setValue(satmin);
		slSatMin.valueProperty().addListener((observable, oldvalue, newvalue) -> {
			satmin = newvalue.intValue();
			fireChange();
		});
	}

	public void setRange(int range) {
		this.range = range;
	}

	public int getRange() {
		return range;
	}

	public int getSatMin() {
		return satmin;
	}

	public void setSatMin(int satmin) {
		this.satmin = satmin;
	}
	
}
