package de.screenflow.frankenstein.vf.segment;

import de.screenflow.frankenstein.ProcessingListener;
import de.screenflow.frankenstein.vf.SegmentVideoFilter;

public class SegmentConfigController {

	private ProcessingListener processingListener = null;
	private SegmentVideoFilter filter = null;

	public void bind(ProcessingListener l, SegmentVideoFilter filter) {
		this.processingListener = l;
		this.filter = filter;
	}
	
	public void fireChange() {
		processingListener.configChanged(this, filter);
	}
}
