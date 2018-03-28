package de.serviceflow.frankenstein.vf.segment;

import de.serviceflow.frankenstein.ProcessingListener;
import de.serviceflow.frankenstein.vf.SegmentVideoFilter;

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
