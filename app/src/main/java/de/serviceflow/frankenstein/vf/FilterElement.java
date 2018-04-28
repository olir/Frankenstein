package de.serviceflow.frankenstein.vf;

import org.opencv.core.Range;

import de.serviceflow.frankenstein.fxml.ProcessingSceneController;
import de.serviceflow.frankenstein.plugin.api.SegmentVideoFilter;

public class FilterElement {
	public final Range r;
	final ProcessingSceneController psc;
	final String name;
	
	public SegmentVideoFilter filter = null;

	
	public FilterElement(Range r, ProcessingSceneController psc, String name) {
		this.r = r;
		this.psc = psc;
		this.name = name;
	}

	public FilterElement(Range r, ProcessingSceneController psc) {
		this(r, psc, null);
	}
	
	public void setType(SegmentVideoFilter selectedFilterType) {
		filter = selectedFilterType;
	}

	public String toString() 
	{
		return (name!=null ? name : toStringRange()) + " " + (filter != null ? filter : "<none>");
	}

	public String toStringRange() {
		return "" + psc.time((r.start - 1.0) / psc.fps) + "-" + psc.time((r.end - 1.0) / psc.fps) + " " + r.toString();
	}
}
