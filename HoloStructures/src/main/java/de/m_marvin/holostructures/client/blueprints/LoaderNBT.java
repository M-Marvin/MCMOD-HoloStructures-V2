package de.m_marvin.holostructures.client.blueprints;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import de.m_marvin.holostructures.UtilHelper;
import de.m_marvin.holostructures.client.blueprints.BlueprintLoader.IFormatLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LoaderNBT implements IFormatLoader {
	
	public static final int DATA_VERSION = 2975;
	public static final int DATA_VERSION_SUPPORTED = 2000;
	
	@Override
	public boolean loadFromStream(Blueprint blueprint, InputStream inputStream) throws CommandSyntaxException {
		
		try {
			CompoundTag nbt = NbtIo.readCompressed(inputStream);

			if (!nbt.contains("DataVersion") || nbt.getInt("DataVersion") < DATA_VERSION_SUPPORTED) throw new SimpleCommandExceptionType(new TranslatableComponent("loader.error.dataversion",  + nbt.getInt("DataVersion"))).create();
			
			blueprint.size = UtilHelper.loadBlockPos(nbt, "size").offset(-1, -1, -1);
			blueprint.origin = UtilHelper.loadBlockPos(nbt, "origin");
			
			ListTag palette = nbt.getList("palette", 10);
			for (int i = 0; i < palette.size(); i++) {
				BlockState state = NbtUtils.readBlockState(palette.getCompound(i));
				blueprint.states.add(state);
			}
			
			ListTag blocks = nbt.getList("blocks", 10);
			for (int i = 0; i < blocks.size(); i++) {
				CompoundTag tag = blocks.getCompound(i);
				int stateid = tag.getInt("state");
				BlockPos pos = UtilHelper.loadBlockPos(tag, "pos");
				CompoundTag benbt = tag.getCompound("nbt");
				blueprint.statemap.put(pos, stateid);
				if (benbt != null) {
					ResourceLocation beid = new ResourceLocation(benbt.getString("id"));
					blueprint.addBlockEntity(pos, new Blueprint.EntityData(beid, () -> Optional.of(benbt)));
				}
			}
			
			ListTag entities = nbt.getList("entities", 10);
			for (int i = 0; i < entities.size(); i++) {
				CompoundTag tag = entities.getCompound(i);
				CompoundTag enbt = tag.getCompound("nbt");
				Vec3 pos = UtilHelper.loadVecPos(tag, "pos");
				ResourceLocation eid = new ResourceLocation(enbt.getString("id"));
				blueprint.addEntity(pos, new Blueprint.EntityData(eid, () -> Optional.of(enbt)));
			}
			
			return true;
		} catch (IOException e) {
			throw new SimpleCommandExceptionType(new TranslatableComponent("loader.error.ioexception", e.getMessage())).create();
		}
		
	}

	@Override
	public boolean saveToStream(Blueprint blueprint, OutputStream outputStream) throws CommandSyntaxException {
		
		CompoundTag nbt = new CompoundTag();
		
		ListTag palette = new ListTag();
		blueprint.states.forEach((state) -> {
			CompoundTag tag = NbtUtils.writeBlockState(state);
			palette.add(tag);
		});
		nbt.put("palette", palette);
		
		ListTag blocks = new ListTag();
		blueprint.statemap.forEach((pos, stateid) -> {
			CompoundTag tag = new CompoundTag();
			Blueprint.EntityData blockentity = blueprint.blockentities.get(pos);
			tag.putInt("state", stateid);
			tag.put("pos", UtilHelper.saveBlockPos(pos));
			if (blockentity != null && blockentity.nbt().get().isPresent()) {
				tag.put("nbt", blockentity.nbt().get().get());
			}
			blocks.add(tag);
		});
		nbt.put("blocks", blocks);
		
		ListTag entities = new ListTag();
		blueprint.entities.asMap().forEach((pos, elist) -> {
			elist.forEach((entity) -> {
				if (entity.nbt().get().isPresent()) {
					CompoundTag tag = new CompoundTag();
					CompoundTag enbt = entity.nbt().get().get();
					enbt.putString("id", entity.type().toString());
					tag.put("pos", UtilHelper.saveVecPos(pos));
					tag.put("nbt", enbt);
					entities.add(tag);
				}
			});
		});
		nbt.put("entities", entities);
		
		nbt.put("size", UtilHelper.saveBlockPos(blueprint.size.offset(1, 1, 1)));
		nbt.putInt("DataVersion", DATA_VERSION);
		
		nbt.put("origin", UtilHelper.saveBlockPos(blueprint.origin));
		
		try {
			NbtIo.writeCompressed(nbt, outputStream);
		} catch (IOException e) {
			throw new SimpleCommandExceptionType(new TranslatableComponent("loader.error.ioexception", e.getMessage())).create();
		}
		return true;
		
	}
	
}
