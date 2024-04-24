package de.m_marvin.holostruct.client.levelbound.access.clientlevel.commanddispatcher;

public abstract class Command<T> {
	
	public abstract String command();
	public abstract T parseResult(String result);
	
}
