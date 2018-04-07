package de.serviceflow.frankenstein.plugin.api;

/**
 * Callback interface to reflect configuration changes to the preview.
 */
public interface SegmentFilterConfigListener {
	/**
	 * notifies the listener about a configuration change event in the filter.
	 * 
	 * @param segmentConfigController
	 *            DefaultSegmentConfigController
	 * @param selectedFilter
	 *            SegmentVideoFilter
	 */
	void configChanged(DefaultSegmentConfigController segmentConfigController, SegmentVideoFilter selectedFilter);
}
