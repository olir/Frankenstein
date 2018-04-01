package de.serviceflow.frankenstein;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;

public class ExecutorThread implements Executor {
	final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
	Runnable active;
	boolean isAlive = true;
	final Thread thread = new Thread(new Runnable() {
		@Override
		public void run() {
			try {
				System.out.println("ExecutorThread running");
				while (isAlive) {
					scheduleNext();
					Thread.sleep(10L);
				}
				System.out.println("ExecutorThread shutdown");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	});

	private ExecutorThread() {
		thread.start();
	}

	private void stop() {
		isAlive=false;
	}

	private static ExecutorThread instance = null;
	
	public static synchronized ExecutorThread getInstance() {
		if (instance==null)
			instance = new ExecutorThread();
		return instance;
	}

	public static void shutdown() {
		if (instance!=null)
			instance.stop();
	}
	
	public synchronized void execute(final Runnable r) {
		tasks.offer(r);
	}

	protected synchronized void scheduleNext() {
		if ((active = tasks.poll()) != null) {
			active.run();
		}
	}

}