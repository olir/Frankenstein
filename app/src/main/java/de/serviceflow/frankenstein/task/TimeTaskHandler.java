package de.serviceflow.frankenstein.task;

import de.serviceflow.frankenstein.ProcessingListener;

public class TimeTaskHandler extends TaskHandler {
	private final ProcessingListener l;
	private final String taskMessage;

	public TimeTaskHandler(ProcessingListener l, String taskMessage) {
		this.taskMessage = taskMessage;
		this.l = l;
	}

	@Override
	public void handleLine(String line) {
		// System.out.println("> "+line);
		int s = line.indexOf("time=");
		if (s >= 0) {
			int e = line.indexOf(' ', s);
			if (e>0)
				progress(line.substring(s + 5, e));
		}
	}

	@Override
	public void done() {
		progress(null);
	}

	private void progress(String time) {
		if (l != null)
			l.taskUpdate(time, taskMessage);
		// System.out.println("progress "+time);
	}

}