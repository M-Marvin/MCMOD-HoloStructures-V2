package de.m_marvin.holostruct.client.levelbound.access.clientlevel.commands;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import de.m_marvin.holostruct.client.ClientConfig;

/**
 * An command send to the server and waiting for response.
 * @author Marvin Koehler
 *
 * @param <T> Type of the result of this command
 */
public abstract class Command<T> {
	
	public static class Response {
		
		private String responseKey;
		private List<String> responseValues;
		
		public Response(String key, List<String> values) {
			this.responseKey = key;
			this.responseValues = values;
		}
		
		public String getResponseKey() {
			return responseKey;
		}
		
		public List<String> getResponseValues() {
			return responseValues;
		}
		
	}
	
	private long sendTime;
	private CompletableFuture<T> future;
	
	public abstract String command();
	public abstract Optional<T> tryParseResponse(Response response);
	
	public boolean tryAccept(Response response) {
		Optional<T> result = tryParseResponse(response);
		if (result.isEmpty()) return false;
		this.future.complete(result.get());
		return true;
	}
	
	public CompletableFuture<T> startDispatch(long timestamp) {
		this.sendTime = timestamp;
		this.future = new CompletableFuture<>();
		return this.future;
	}
	
	public boolean isOutdated(long current) {
		boolean outdated = current - this.sendTime > ClientConfig.COMMAND_TIMEOUT.get();
		if (outdated) this.future.completeExceptionally(new Throwable("timed out"));
		return outdated;
	}
	
}
