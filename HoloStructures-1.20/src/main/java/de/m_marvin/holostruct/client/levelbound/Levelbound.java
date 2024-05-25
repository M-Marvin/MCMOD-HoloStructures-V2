package de.m_marvin.holostruct.client.levelbound;

import java.util.ArrayDeque;
import java.util.Queue;

import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.levelbound.access.IRemoteLevelAccessor;
import de.m_marvin.holostruct.client.levelbound.access.NoAccessAccessor;

/**
 * This class manages the access to the server level from client side using the {@link IRemoteLevelAccessor} implementations.
 * @author Marvin Koehler
 */
public class Levelbound {
	
	/**
	 * The access level determines which actions are possible on the {@link IRemoteLevelAccessor}.
	 * The access level is determined based on if the mod is available on server side, and on an permission config queried from the server.
	 * @author Marvin Koehler
	 */
	public static enum AccessLevel {
		NO_ACCESS(false, false, false, false),
		READ_CLIENT(false, true, false, false),
		COPY_CLIENT(false, true, true, false),
		FULL_CLIENT(false, true, true, true),
		READ_SERVER(true, true, false, false),
		COPY_SERVER(true, true, true, false),
		FULL_SERVER(true, true, true, true);
		
		private boolean isServer;
		private boolean hasRead;
		private boolean hasCopy;
		private boolean hasWrite;
		
		private AccessLevel(boolean server, boolean hasRead, boolean hasCopy, boolean hasWrite) {
			this.isServer = server;
			this.hasRead = hasRead;
			this.hasCopy = hasCopy;
			this.hasWrite = hasWrite;
		}
		
		/**
		 * If read operations are allowed
		 */
		public boolean hasRead() {
			return this.hasRead;
		}
		
		/**
		 * If copy operations are allowed
		 */
		public boolean hasCopy() {
			return this.hasCopy;
		}
		
		/**
		 * If write/modify operations are allowed
		 */
		public boolean hasWrite() {
			return this.hasWrite;
		}
		
		/**
		 * If the mod is available on the server side
		 */
		public boolean isServer() {
			return isServer;
		}
	}
	
	private IRemoteLevelAccessor accessor;
	private Thread levelboundTaskThread;
	private boolean shouldTaskThreadShutdown;
	private Queue<Runnable> tasks = new ArrayDeque<>();
	
	public Levelbound() {
		this.accessor = new NoAccessAccessor();
	}
	
	/**
	 * Start the task thread.
	 * The task thread executes the asynchronous tasks that use the {@link IRemoteLevelAccessor}
	 */
	public void startTaskThread() {
		if (this.levelboundTaskThread != null && this.levelboundTaskThread.isAlive()) return;
		this.shouldTaskThreadShutdown = false;
		this.levelboundTaskThread = new Thread(this::doTasks, "Levelbound Thread");
		this.levelboundTaskThread.setDaemon(true);
		this.levelboundTaskThread.start();
	}
	
	/**
	 * Stop the task thread.
	 * The task thread executes the asynchronous tasks that use the {@link IRemoteLevelAccessor}
	 */
	public void stopTaskThread() {
		if (this.levelboundTaskThread == null || !this.levelboundTaskThread.isAlive()) return;
		try {
			synchronized (this.tasks) {
				this.shouldTaskThreadShutdown = true;
				this.tasks.clear();
				this.tasks.notifyAll();
			}
			this.levelboundTaskThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Executes an task on the task thread.
	 * @param task The task to execute
	 */
	public void safeExecute(Runnable task) {
		if (task == null) return;
		synchronized (this.tasks) {
			this.tasks.add(task);
			this.tasks.notify();
		}
	}
	
	/**
	 * Aborts all pending tasks on the task thread.
	 */
	public void clearTaskQueue() {
		synchronized (this.tasks) {
			this.tasks.clear();
		}
	}
	
	private void doTasks() {
		while (!this.shouldTaskThreadShutdown) {
			try {
				Runnable task;
				synchronized (this.tasks) {
					if (this.tasks.isEmpty()) this.tasks.wait();
					task = this.tasks.poll();
				}
				if (task != null) {
					task.run();
					System.out.println("Run task"); // TODO task problem
				}
			} catch (Throwable e) {
				HoloStruct.LOGGER.warn("Error while executing levelbound task: {}", e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Set the active level accessor implementation, this should only be called internally
	 */
	public void setAccess(IRemoteLevelAccessor accessor) {
		this.accessor = accessor;
		if (this.accessor.getAccessLevel() == AccessLevel.NO_ACCESS) {
			stopTaskThread();
		} else {
			startTaskThread();
		}
	}

	/**
	 * Returns the currently available access level
	 */
	public AccessLevel getAccessLevel() {
		return this.accessor.getAccessLevel();
	}
	
	/**
	 * Returns the currently available {@link IRemoteLevelAccessor} implementation.
	 */
	public IRemoteLevelAccessor getAccessor() {
		return this.accessor;
	}
	
}
