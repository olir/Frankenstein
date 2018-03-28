package de.serviceflow.frankenstein.plugin.api;

public class SegmentConfigController {

	private SegmentFilterConfigListener listener = null;
	private SegmentVideoFilter filter = null;

	public void bind(SegmentFilterConfigListener l, SegmentVideoFilter filter) {
		this.listener = l;
		this.filter = filter;
	}

	public void fireChange() {
		listener.configChanged(this, filter);
	}
}
