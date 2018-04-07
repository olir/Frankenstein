package de.serviceflow.frankenstein.plugin.api;

import org.opencv.core.Mat;

import javafx.scene.Scene;

/**
 * Filter that can be applied to a video segment.
 */
public interface SegmentVideoFilter {
	/**
	 * create instance of this filter usally called on prototype instance.
	 * 
	 * @return new filter instance
	 */
	SegmentVideoFilter createInstance();

	/**
	 * Returns a FXML scene for filter configuration.
	 * 
	 * @param stylesheet
	 *            reference, external form of a URL, to the CSS stylesheet.
	 * @return Scene
	 */
	Scene createConfigurationScene(String stylesheet);

	/**
	 * Returns controller for the configuration scene.
	 * 
	 * @return SegmentConfigController
	 */
	DefaultSegmentConfigController getConfigController();

	/**
	 * Should process a frame.
	 * 
	 * @param sourceFrame
	 *            OpenCV Mat of the frame
	 * @param frameId
	 *            sequence id of the frame (1, ...).
	 * @param context
	 *            the FilterContext object
	 * @return Mat result.
	 */
	Mat process(Mat sourceFrame, int frameId, FilterContext context);

}
