package samplefilters;

import org.opencv.core.Mat;

import de.serviceflow.frankenstein.plugin.api.DefaultSegmentFilter;
import de.serviceflow.frankenstein.plugin.api.FilterContext;

public class SampleFilter extends DefaultSegmentFilter {

	public SampleFilter() {
		super("sample");
	}
	
	@Override
	public Mat process(Mat sourceFrame, int frameId, FilterContext context) {
		// TODO: Your open OpenCV code here ...
		return sourceFrame;
	}

	@Override
	protected void initializeController() {
		// optional TODO ...
		
	}
}
