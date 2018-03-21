package de.screenflow.frankenstein.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.screenflow.frankenstein.MovieProcessor;

public class Task {

	private String[] args;
	private String command;
	// private boolean shutdown = false;
	// private File log;
	private TaskHandler l;

	public Task(MovieProcessor movieProcessor, String command, TaskHandler l) {
		this.command = command;
		this.args = command.split(" ");
		Task.this.l = l;
	}

	public boolean run() {
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		processBuilder.directory(new File(System.getProperty("java.io.tmpdir")));

		try {
			// Log to ...\AppData\Local\Temp
			// log = File.createTempFile("frankestein_task_", ".log");
			processBuilder.redirectErrorStream(true);
			// processBuilder.redirectOutput(Redirect.appendTo(log));
			System.out.println("Executing:\n" + command);
			Process p = processBuilder.start();
			InputStream is = p.getInputStream();
			new Thread(new LogHandler(is, l)).start();
			p.waitFor();
			// log.deleteOnExit();
			// shutdown = true;
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			// shutdown = true;
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			// shutdown = true;
			return false;
		}
	}

	private class LogHandler implements Runnable {
		InputStream is;
		TaskHandler l;

		public LogHandler(InputStream is, TaskHandler l) {
			LogHandler.this.is = is;
			LogHandler.this.l = l;
		}

		public void run() {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			try {
				while ((line = br.readLine()) != null) {
					l.handleLine(line);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (Exception ex) {
				// Ignore
				// System.err.println("substring: "+line.substring(s + 5,
				// e));
				System.err.println("line: " + line);
				ex.printStackTrace();
			}
		}

	}
}