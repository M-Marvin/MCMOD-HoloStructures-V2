package de.m_marvin.holostruct.client.event;

import net.minecraft.locale.Language;
import net.neoforged.bus.api.Event;

public class ClientLanguageInjectEvent extends Event {
	
	private final Language language;
	
	public ClientLanguageInjectEvent(Language language) {
		this.language = language;
	}
	
	public Language getLanguage() {
		return language;
	}
	
}
