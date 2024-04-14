package de.m_marvin.holostructures.client.holograms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.holostructures.client.ClientHandler;
import de.m_marvin.holostructures.client.blueprints.Blueprint;
import de.m_marvin.holostructures.client.rendering.HolographicRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class HologramManager {
	
	public static final Supplier<ClientLevel> CLIENT_LEVEL = () -> Minecraft.getInstance().level;
	
	protected Thread workerThread;
	protected Map<String, Hologram> holograms = new HashMap<>();
	
	@SubscribeEvent
	public static void onChangeBlock(BlockEvent event) {
		//if ( event.getWorld().isClientSide()) System.out.println("EVENT" + event.getClass() + " " + event.getWorld().isClientSide());
		
		ClientHandler.getInstance().getHolograms().updateHoloChunksAt(event.getPos());
		//		ClientHandler.getInstance().getHolograms().getHolograms().forEach((hologram) -> {
//			BlockPos position = hologram.worldToHoloPosition(event.getPos());
//			Optional<HologramChunk> chunk = hologram.getChunkAt(position);
//			if (chunk.isPresent()) {
//				ChunkPos pos = chunk.get().getPosition();
//				hologram.updateChunkHoloBlockStates(pos);
//			}
//		});
	}
	
	public Hologram createHologram(@Nullable Blueprint blueprint, BlockPos position, String name, boolean includeEntities) {
		if (holograms.containsKey(name)) return null;
		Hologram hologram = new Hologram(CLIENT_LEVEL.get(), position, name);
		if (blueprint != null) {
			blueprint.pasteBlueprint(hologram, BlockPos.ZERO.subtract(blueprint.getOrigin()), includeEntities);
		} else {
			hologram.setBlock(BlockPos.ZERO, Blocks.STONE.defaultBlockState());
		}
		addHologram(name, hologram);
		return hologram;
	}
	
	public void addHologram(String name, Hologram hologram) {
		this.holograms.put(name, hologram);
		HolographicRenderer.addHologram(hologram);
		hologram.refreshAllChunks();
	}
	
	public boolean removeHologram(String name) {
		Hologram hologram = this.holograms.remove(name);
		if (hologram != null) {
			HolographicRenderer.discardHologram(hologram);
			return true;
		}
		return false;
	}
	
	public Hologram getHologram(String name) {
		return this.holograms.get(name);
	}

	public String[] getHologramNames() {
		return this.holograms.keySet().toArray((l) -> new String[l]);
	}
	
	public Collection<Hologram> getHolograms() {
		return holograms.values();
	}
	
	public void updateHoloChunk(Hologram hologram, ChunkPos chunk) {
		hologram.updateChunkHoloBlockStates(chunk);
	}
	
	public void updateHoloChunksAt(BlockPos position) {
		getHolograms().forEach((hologram) -> {
			Optional<HologramChunk> chunk = hologram.getChunkAt(position.subtract(hologram.getPosition()));
			if (chunk.isPresent()) {
				ChunkPos pos = chunk.get().getPosition();
				hologram.updateChunkHoloBlockStates(pos);
			}
		});
	}
	
	public void updateAllHoloChunksOf(Hologram hologram) {
		hologram.updateAllHoloBlockStates();
	}
	
}
