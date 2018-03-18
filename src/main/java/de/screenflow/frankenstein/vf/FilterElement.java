package de.screenflow.frankenstein.vf;

import org.opencv.core.Range;

import de.screenflow.frankenstein.fxml.ProcessingSceneController;

public class FilterElement {
	public Range r;
	public LocalVideoFilter filter = null;
	ProcessingSceneController psc;
	
	public FilterElement(Range r, ProcessingSceneController psc) {
		FilterElement.this.r = r;
		this.psc = psc;
	}

	public void setType(LocalVideoFilter selectedFilterType) {
		filter = selectedFilterType;
	}

	public String toString() {
		return toStringRange() + " " + (filter != null ? filter : "<none>");
	}

	public String toStringRange() {
		return "" + psc.time((r.start - 1.0) / psc.fps) + "-" + psc.time((r.end - 1.0) / psc.fps) + " " + r.toString();
	}
}
