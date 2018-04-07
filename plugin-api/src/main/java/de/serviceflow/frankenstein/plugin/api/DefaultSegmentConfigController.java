package de.serviceflow.frankenstein.plugin.api;

/**
 * Basic controller for FXML Configuration scene of a filter. 
 */
public abstract class DefaultSegmentConfigController {

	private SegmentFilterConfigListener listener = null;
	private SegmentVideoFilter filter = null;

	/**
	 * Bind a SegmentFilterConfigListener and filter to this controller.
	 * @param l SegmentFilterConfigListener for fireChange() calls.
	 * @param filter SegmentVideoFilter
	 */
	public void bind(SegmentFilterConfigListener l, SegmentVideoFilter filter) {
		this.listener = l;
		this.filter = filter;
	}

	/**
	 * Reflect configuration changes to the preview.
	 */
	public void fireChange() {
		if (listener!=null)
			listener.configChanged(this, filter);
	}
}
