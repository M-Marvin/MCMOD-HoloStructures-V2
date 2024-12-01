package de.m_marvin.holostruct.client.event;

import net.minecraft.locale.Language;
import net.neoforged.bus.api.Event;

/**
 * Custom Mixin event for language change detection
 * Fired from the {@link ClientLanguageInjectEvent} mixin.
 * @author Marvin Koehler
 */
public class ClientLanguageInjectEvent extends Event {
	
	private final Language language;
	
	public ClientLanguageInjectEvent(Language language) {
		this.language = language;
	}
	
	public Language getLanguage() {
		return language;
	}
	
}
