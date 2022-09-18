package de.m_marvin.holostructures.client.blueprints;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import de.m_marvin.holostructures.UtilHelper;
import de.m_marvin.holostructures.client.blueprints.Blueprint.EntityData;
import de.m_marvin.holostructures.client.blueprints.BlueprintLoader.IFormatLoader;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LoaderSchem implements IFormatLoader {
	
	public static final int DATA_VERSION = 2120;
	public static final int DATA_VERSION_SUPPORTED = 0; // TODO
	
	@Override
	public boolean loadFromStream(Blueprint blueprint, InputStream inputStream) throws CommandSyntaxException {
		
		try {
			CompoundTag nbt = NbtIo.readCompressed(inputStream);
			
			if (!nbt.contains("DataVersion") || nbt.getInt("DataVersion") < DATA_VERSION_SUPPORTED) throw new SimpleCommandExceptionType(new TranslatableComponent("loader.error.dataversion",  + nbt.getInt("DataVersion"))).create();
			
			blueprint.size = new BlockPos(nbt.getInt("Width") - 1, nbt.getInt("Height") - 1, nbt.getInt("Length") - 1);
			CompoundTag weoffset = nbt.getCompound("Metadata");
			blueprint.origin = new BlockPos(-weoffset.getInt("WEOffsetX"), -weoffset.getInt("WEOffsetY"), -weoffset.getInt("WEOffsetZ"));
			
			CompoundTag palette = nbt.getCompound("Palette");
			Map<Integer, BlockState> stateCache = new HashMap<>();
			for (String stateString : palette.getAllKeys()) {
				int id = palette.getInt(stateString);
				BlockStateParser parser = new BlockStateParser(new StringReader(stateString), false);
				try {
					parser.parse(false);
				} catch (CommandSyntaxException e) {
					throw new SimpleCommandExceptionType(new TranslatableComponent("loader.error.stateparser", stateString)).create();
				}
				stateCache.put(id, parser.getState());
			}
			for (int i = 0; i < nbt.getInt("PaletteMax"); i++) {
				BlockState state = stateCache.get(i);
				blueprint.states.add(state);
			}
			
			byte[] blocks = nbt.getByteArray("BlockData");
			int counter = 0;
			for (int y = 0; y <= blueprint.size.getY(); y++) {
				for (int z = 0; z <= blueprint.size.getZ(); z++) {
					for (int x = 0; x <= blueprint.size.getX(); x++) {
						BlockPos pos = new BlockPos(x, y, z);
						int stateId = blocks[counter++];
						blueprint.statemap.put(pos, stateId);
					}
				}
			}
			
			ListTag blockentities = nbt.getList("BlockEntities", 10);
			for (int i = 0; i < blockentities.size(); i++) {
				CompoundTag benbt = blockentities.getCompound(i);
				int[] posArr = benbt.getIntArray("Pos");
				BlockPos pos = new BlockPos(posArr[0], posArr[1], posArr[2]);
				ResourceLocation id = new ResourceLocation(benbt.getString("Id"));
				benbt.remove("Pos");
				blueprint.blockentities.put(pos, new EntityData(id, () -> Optional.of(benbt)));
			}
			
			ListTag entities = nbt.getList("Entities", 10);
			int[] offset = nbt.getIntArray("Offset");
			for (int i = 0; i < entities.size(); i++) {
				CompoundTag enbt = entities.getCompound(i);
				Vec3 pos = UtilHelper.loadVecPos(enbt, "Pos").subtract(offset[0], offset[1], offset[2]);
				enbt.remove("Pos");
				ResourceLocation id = new ResourceLocation(enbt.getString("Id"));
				System.out.println("DDD " + pos + " " + enbt);
				blueprint.addEntity(pos, new Blueprint.EntityData(id, () -> Optional.of(enbt)));
			}
			
			return true;
		} catch (IOException e) {
			throw new SimpleCommandExceptionType(new TranslatableComponent("loader.error.ioexception", e.getMessage())).create();
		}
	}

	@Override
	public boolean saveToStream(Blueprint blueprint, OutputStream outputStream) throws CommandSyntaxException {
		
		CompoundTag nbt = new CompoundTag();
		
		CompoundTag palette = new CompoundTag();
		for (int id = 0; id < blueprint.states.size(); id++) {
			palette.putInt(BlockStateParser.serialize(blueprint.states.get(id)), id);
		}
		nbt.put("Palette", palette);
		nbt.putInt("PaletteMax", blueprint.states.size());
		
		byte[] blocks = new byte[(blueprint.size.getX() + 1) * (blueprint.size.getY() + 1) * (blueprint.size.getZ() + 1)];
		int counter = 0;
		for (int y = 0; y <= blueprint.size.getY(); y++) {
			for (int z = 0; z <= blueprint.size.getZ(); z++) {
				for (int x = 0; x <= blueprint.size.getX(); x++) {
					BlockPos pos = new BlockPos(x, y, z);
					int stateId = blueprint.statemap.get(pos);
					blocks[counter++] = (byte) stateId;
				}
			}
		}
		nbt.putByteArray("BlockData", blocks);
		
		ListTag blockentities = new ListTag();
		blueprint.blockentities.forEach((pos, blockentitie) -> {
			if (blockentitie.nbt().get().isPresent()) {
				CompoundTag tag = blockentitie.nbt().get().get();
				tag.putIntArray("Pos", new int[] {pos.getX(), pos.getY(), pos.getZ()});
				tag.putString("Id", blockentitie.type().toString());
				blockentities.add(tag);
			}
		});
		nbt.put("BlockEntities", blockentities);
		
		ListTag entities = new ListTag();
		blueprint.entities.asMap().forEach((pos, entitielist) -> {
			entitielist.forEach((entity) -> {
				if (entity.nbt().get().isPresent()) {
					CompoundTag tag = entity.nbt().get().get();
					tag.putString("Id", entity.type().toString());
					ListTag posTag = new ListTag();
					posTag.add(DoubleTag.valueOf(pos.x()));
					posTag.add(DoubleTag.valueOf(pos.y()));
					posTag.add(DoubleTag.valueOf(pos.z()));
					tag.put("Pos", posTag);
					entities.add(tag);
				}
			});
		});
		nbt.put("Entities", entities);
		nbt.putIntArray("Offset", new int[] {0, 0, 0});
		
		CompoundTag weoffset = new CompoundTag();
		weoffset.putInt("WEOffsetX", -blueprint.origin.getX());
		weoffset.putInt("WEOffsetY", -blueprint.origin.getY());
		weoffset.putInt("WEOffsetZ", -blueprint.origin.getZ());
		nbt.put("Metadata", weoffset);
		
		nbt.putShort("Width", (short) (blueprint.size.getX() + 1));
		nbt.putShort("Height", (short) (blueprint.size.getY() + 1));
		nbt.putShort("Length", (short) (blueprint.size.getZ() + 1));
		
		nbt.putInt("Version", 2);
		nbt.putInt("DataVersion", DATA_VERSION);
		
		try {
			NbtIo.writeCompressed(nbt, outputStream);
		} catch (IOException e) {
			throw new SimpleCommandExceptionType(new TranslatableComponent("loader.error.ioexception", e.getMessage())).create();
		}
		
		return true;
		
	}
	
}
