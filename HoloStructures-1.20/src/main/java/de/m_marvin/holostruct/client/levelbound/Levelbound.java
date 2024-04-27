package de.m_marvin.holostruct.client.levelbound;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.m_marvin.holostruct.client.levelbound.access.IRemoteLevelAccessor;
import de.m_marvin.holostruct.client.levelbound.access.NoAccessAccessor;

public class Levelbound {
	
	public static final Executor LEVEL_ACCESS_EXECUTOR = Executors.newFixedThreadPool(8);
	
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
		
		public boolean hasRead() {
			return this.hasRead;
		}
		
		public boolean hasCopy() {
			return this.hasCopy;
		}
		
		public boolean hasWrite() {
			return this.hasWrite;
		}
		
		public boolean isServer() {
			return isServer;
		}
	}
	
	private IRemoteLevelAccessor accessor;
	
	public Levelbound() {
		this.accessor = new NoAccessAccessor();
	}
	
	public void setAccess(IRemoteLevelAccessor accessor) {
		this.accessor = accessor;
	}

	public AccessLevel getAccessLevel() {
		return this.accessor.getAccessLevel();
	}
	
	public IRemoteLevelAccessor getAccessor() {
		return this.accessor;
	}
	
}
