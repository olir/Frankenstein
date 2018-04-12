package de.serviceflow.frankenstein.task;

public abstract class TaskHandler {
	private final StringBuffer logBuffer = new StringBuffer();
	
	public void handleLine(String line) {

	}

	public void done() {

	}
	
	public StringBuffer getLogBuffer() {
		return logBuffer;
	}
	
	
}