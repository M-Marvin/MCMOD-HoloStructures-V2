package de.m_marvin.holostruct.client.levelbound.access.serverlevel;

import java.util.concurrent.CompletableFuture;

import de.m_marvin.holostruct.client.ClientConfig;
import de.m_marvin.holostruct.levelbound.network.ILevelboundPackage;
import net.minecraft.client.multiplayer.ClientPacketListener;

/**
 * An levelbound package that was sent to server and is waiting for response.
 * @author Marvin Koehler
 *
 * @param <P> The package type
 * @param <T> The packages response type
 */
public class PendingPackage<P extends ILevelboundPackage<T>, T> {
	
	private long sendTime;
	private final P networPackage;
	private CompletableFuture<T> future;
	
	public PendingPackage(P networkPackage) {
		this.networPackage = networkPackage;
	}
	
	/**
	 * Returns true if the passed package type matches this classes type
	 */
	public boolean isRightPackageType(Object pkg) {
		return this.networPackage.getClass().isInstance(pkg);
	}
	
	/**
	 * Starts execution of the package by sending it to the server.
	 * Returns an {@link CompletableFuture} of the packages return type which is completed as soon as an response is apssed to this class using {@link PendingPackage#accept(ILevelboundPackage)}
	 * @param timestamp
	 * @param network
	 * @return
	 */
	public CompletableFuture<T> startDispatch(long timestamp, ClientPacketListener network) {
		this.future = new CompletableFuture<>();
		this.sendTime = timestamp;
		network.send(this.networPackage);
		return this.future;
	}
	
	/**
	 * Completes this package with the passed response if it matches this package.
	 * @param responsePackage The response from the server
	 * @return true if the package could be completed with the response-
	 */
	public boolean accept(P responsePackage) {
		if (responsePackage.getTaskId() != this.networPackage.getTaskId()) return false;
		this.future.complete(responsePackage.getResponse());
		return true;
	}
	
	/**
	 * Checks if this package has reached the timeout limit and if so, completes this package exceptionally and returns true.
	 * @param current The current time in ms
	 * @return true if this packages timeout has been reached
	 */
	public boolean isOutdated(long current) {
		boolean outdated = current - this.sendTime > ClientConfig.PACKAGE_TIMEOUT.get();
		if (outdated && this.future != null) this.future.completeExceptionally(new Throwable("timed out"));
		return outdated;
	}
	
}
