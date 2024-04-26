package de.m_marvin.holostruct.client.levelbound.access.serverlevel;

import java.util.concurrent.CompletableFuture;

import de.m_marvin.holostruct.client.Config;
import de.m_marvin.holostruct.levelbound.network.ILevelboundPackage;
import net.minecraft.client.multiplayer.ClientPacketListener;

public class PendingPackage<P extends ILevelboundPackage<T>, T> {
	
	private long sendTime;
	private final P networPackage;
	private CompletableFuture<T> future;
	
	public PendingPackage(P networkPackage) {
		this.networPackage = networkPackage;
	}
	
	public boolean isRightPackageType(Object pkg) {
		return this.networPackage.getClass().isInstance(pkg);
	}
	
	public CompletableFuture<T> startDispatch(long timestamp, ClientPacketListener network) {
		this.future = new CompletableFuture<>();
		this.sendTime = timestamp;
		network.send(this.networPackage);
		return this.future;
	}
	
	public boolean accept(P responsePackage) {
		if (responsePackage.getTaskId() != this.networPackage.getTaskId()) return false;
		this.future.complete(responsePackage.getResponse());
		return true;
	}
	
	public boolean isOutdated(long current) {
		boolean outdated = current - this.sendTime > Config.PACKAGE_TIMEOUT.get();
		if (outdated) this.future.completeExceptionally(new Throwable("timed out"));
		return outdated;
	}
	
}
