package de.m_marvin.holostructures.client.levelbound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Optional;

import de.m_marvin.blueprints.api.RegistryName;
import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.nbtutility.BinaryParser;
import de.m_marvin.nbtutility.nbt.TagCompound;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class TypeConverter {
	
	// TODO try catchs conversion
	
	public static BlockStateData blockState2data(BlockState state) {
		RegistryName blockName = new RegistryName(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
		BlockStateData data = new BlockStateData(blockName);
		for (Property<?> prop : state.getProperties()) {
			data.setValue(prop.getName(), getProperty(state, prop));
		}
		return data;
	}
	
	public static BlockState data2blockState(BlockStateData data) {
		Block block = BuiltInRegistries.BLOCK.get(data2resLoc(data.getBlockName()));
		BlockState state = block.defaultBlockState();
		for (Property<?> prop : state.getProperties()) {
			state = setProperty(state, prop, data.getValue(prop.getName()));
		}
		return state;
	}
	
	protected static <T extends Comparable<T>> String getProperty(BlockState state, Property<T> prop) {
		return prop.getName(state.getValue(prop));
	}
	
	protected static <T extends Comparable<T>> BlockState setProperty(BlockState state, Property<T> prop, String value) {
		Optional<T> val = prop.getValue(value);
		if (val.isPresent()) return state.setValue(prop, val.get());
		return state;
	}
	
	public static BlockEntityData blockEntity2data(BlockEntity blockEntity) {
		RegistryName typeName = resLoc2data(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()));
		Vec3i position = new Vec3i(blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ());
		BlockEntityData data = new BlockEntityData(position, typeName);
		data.setData(nbt2data(blockEntity.serializeNBT()));
		return data;
	}
	
	public static BlockEntity data2blockEntity(BlockEntityData data) {
		BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(data2resLoc(data.getTypeName()));
		if (type == null) return null;
		BlockPos position = new BlockPos(data.getPosition().x, data.getPosition().y, data.getPosition().z);
		BlockEntity blockEntity = type.create(position, Blocks.AIR.defaultBlockState()); // TODO is air valid ?
		blockEntity.deserializeNBT(data2nbt(data.getData()));
		return blockEntity;
	}
	
	public static EntityData entity2data(Entity entity) {
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	public static Entity data2entity(EntityData data) {
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	public static CompoundTag data2nbt(TagCompound data) {
		try {
			byte[] nbtBin = BinaryParser.toBytes(data, false);
			return NbtIo.read(new DataInputStream(new ByteArrayInputStream(nbtBin)), NbtAccounter.unlimitedHeap());
		} catch (IOException e) {
			HoloStructures.LOGGER.warn("failed to convert nbt data!");
			e.printStackTrace();
			return new CompoundTag();
		}
	}
	
	public static TagCompound nbt2data(CompoundTag nbt) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			nbt.write(new DataOutputStream(buffer));
			return BinaryParser.fromBytes(buffer.toByteArray(), TagCompound.class, false);
		} catch (IOException e) {
			System.err.println("failed to convert nbt data!");
			e.printStackTrace();
			return null;
		}
	}
	
	public static ResourceLocation data2resLoc(RegistryName data) {
		return new ResourceLocation(data.getNamespace(), data.getName());
	}
	
	public static RegistryName resLoc2data(ResourceLocation resLoc) {
		return new RegistryName(resLoc.getNamespace(), resLoc.getPath());
	}
	
}
