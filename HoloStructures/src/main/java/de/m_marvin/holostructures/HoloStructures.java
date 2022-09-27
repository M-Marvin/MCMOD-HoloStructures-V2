package de.m_marvin.holostructures;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.common.Mod;

@Mod("holostructures")
public class HoloStructures {
	
	public static final String MODID = "holostructures";
	public static final Logger LOGGER = LogUtils.getLogger();
	private static HoloStructures INSTANCE;
	
	public HoloStructures() {
		INSTANCE = this;
	}
	
	public static HoloStructures getInstance() {
		return INSTANCE;
	}
	
	/* TODO */
	// Schem format mit Mod-BlÃ¶cken die fehlen
	// Fehlende-Mods liste beim laden einer Blaupause
	// Blaupausen rotieren/spiegeln
	// In-Game editieren von Hologrammen
	// Layer-Ansicht der Hologramme
	// Entities und BlockEntities in Hologrammen
	// GUI-Version der Befehle
	// Mod-API für mod-specifische Dinge (z.B. Industria Conduits, Create Klebstoff)
	
}
