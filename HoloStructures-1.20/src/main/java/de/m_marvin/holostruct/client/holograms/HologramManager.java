package de.m_marvin.holostruct.client.holograms;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import de.m_marvin.blueprints.api.Blueprint;
import de.m_marvin.holostruct.HoloStruct;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;


//@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class HologramManager {
	
	public static final Supplier<ClientLevel> CLIENT_LEVEL = () -> Minecraft.getInstance().level;
	
	protected Thread workerThread;
	protected Map<String, Hologram> holograms = new HashMap<>();
	
//	@SubscribeEvent
//	public static void onChangeBlock(BlockEvent event) {
//		//if ( event.getWorld().isClientSide()) System.out.println("EVENT" + event.getClass() + " " + event.getWorld().isClientSide());
//		
//		ClientHandler.getInstance().getHolograms().updateHoloChunksAt(event.getPos());
//		//		ClientHandler.getInstance().getHolograms().getHolograms().forEach((hologram) -> {
////			BlockPos position = hologram.worldToHoloPosition(event.getPos());
////			Optional<HologramChunk> chunk = hologram.getChunkAt(position);
////			if (chunk.isPresent()) {
////				ChunkPos pos = chunk.get().getPosition();
////				hologram.updateChunkHoloBlockStates(pos);
////			}
////		});
//	}
	
	public Hologram createHologram(@Nullable Blueprint blueprint, BlockPos position, String name) {
		if (holograms.containsKey(name)) return null;
		Hologram hologram = new Hologram(CLIENT_LEVEL.get(), position);
		if (blueprint != null) {
			blueprint.copyTo(hologram);
		} else {
			hologram.setBlock(BlockPos.ZERO, Blocks.STONE.defaultBlockState());
		}
		addHologram(name, hologram);
		return hologram;
	}
	
	public void addHologram(String name, Hologram hologram) {
		this.holograms.put(name, hologram);
		HoloStruct.CLIENT.HOLORENDERER.addHologram(hologram);
		hologram.refreshAllChunks();
	}
	
	public boolean removeHologram(String name) {
		Hologram hologram = this.holograms.remove(name);
		if (hologram != null) {
			HoloStruct.CLIENT.HOLORENDERER.discardHologram(hologram);
			return true;
		}
		return false;
	}
	
	public Hologram getHologram(String name) {
		return this.holograms.get(name);
	}
	
	public Map<String, Hologram> getHolograms() {
		return holograms;
	}
	
//	public void updateHoloChunk(Hologram hologram, ChunkPos chunk) {
//		hologram.updateChunkHoloBlockStates(chunk);
//	}
//	
//	public void updateHoloChunksAt(BlockPos position) {
//		getHolograms().values().forEach(hologram -> {
//			Optional<HologramChunk> chunk = hologram.getChunkAt(position.subtract(hologram.getPosition()));
//			if (chunk.isPresent()) {
//				ChunkPos pos = chunk.get().getPosition();
//				hologram.updateChunkHoloBlockStates(pos);
//			}
//		});
//	}
	
}
