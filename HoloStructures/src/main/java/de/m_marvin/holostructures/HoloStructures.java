package de.m_marvin.holostructures;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

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
	// Schem format mit Mod-Blöcken die fehlen
	// Fehlende-Mods liste beim laden einer Blaupause
	// Blaupausen rotieren/spiegeln
	// In-Game editieren von Hologrammen
	// Layer-Ansicht der Hologramme
	// Entities und BlockEntities in Hologrammen
	// GUI-Version der Befehle
	// Mod-API f�r mod-specifische Dinge (z.B. Industria Conduits, Create Klebstoff)
	// Hologram shader: https://gist.github.com/gigaherz/b8756ff463541f07a644ef8f14cb10f5
	// https://github.com/XFactHD/FramedBlocks/blob/1.19.x/src/main/java/xfacthd/framedblocks/api/util/client/OutlineRender.java#L74-L87
	
}
