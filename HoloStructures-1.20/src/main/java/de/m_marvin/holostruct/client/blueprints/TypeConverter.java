package de.m_marvin.holostruct.client.blueprints;

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
import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.nbtutility.BinaryParser;
import de.m_marvin.nbtutility.nbt.TagCompound;
import de.m_marvin.univec.impl.Vec3d;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

/**
 * This is the main class used to construct actual game instances of their respective blueprint data types and do the reverse.
 * @author Marvin Koehler
 */
public class TypeConverter {
	
	public static final BlockStateData AIR_STATE = new BlockStateData(new RegistryName("minecraft:air"));
	
	/**
	 * Converts an actual blocks state to an data type.
	 * @param state The block state instance
	 * @return The block state data type or null if it was not possible to convert.
	 */
	public static BlockStateData blockState2data(BlockState state) {
		if (state == null) return null;
		RegistryName blockName = new RegistryName(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
		BlockStateData data = new BlockStateData(blockName);
		for (Property<?> prop : state.getProperties()) {
			data.setValue(prop.getName(), getProperty(state, prop));
		}
		return data;
	}
	
	/**
	 * Constructs an actual block state from an data type.
	 * @param data The block state data type
	 * @return The actual block state or null if it was not possible to convert.
	 */
	public static BlockState data2blockState(BlockStateData data) {
		if (data == null) return null;
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
	
	/**
	 * Converts an actual block entity to and data type.
	 * @param blockEntity The block entity instance
	 * @return The block entity data type or null if it was not possible to convert
	 */
	public static BlockEntityData blockEntity2data(BlockEntity blockEntity) {
		if (blockEntity == null) return null;
		RegistryName typeName = resLoc2data(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()));
		Vec3i position = new Vec3i(blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ());
		BlockEntityData data = new BlockEntityData(position, typeName);
		data.setData(nbt2data(blockEntity.serializeNBT()));
		return data;
	}
	
	/**
	 * Constructs an actual block entity from an data type.
	 * @param block The actual block state of the block entity, required for construction
	 * @param data The block entity data type
	 * @return The actual block entity instance or null if it was not possible to convert.
	 */
	public static BlockEntity data2blockEntity(BlockState block, BlockEntityData data) {
		if (data == null) return null;
		BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(data2resLoc(data.getTypeName()));
		if (type == null) return null;
		BlockPos position = new BlockPos(data.getPosition().x, data.getPosition().y, data.getPosition().z);
		BlockEntity blockEntity = type.create(position, block);
		blockEntity.deserializeNBT(data2nbt(data.getData()));
		return blockEntity;
	}
	
	/**
	 * Converts an actual entity to and data type.
	 * @param entity The entity instance
	 * @return The entity data type or null if it was not possible to convert
	 */
	public static EntityData entity2data(Entity entity) {
		if (entity == null) return null;
		RegistryName entityName = resLoc2data(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
		Vec3d position = new Vec3d(entity.position().x, entity.position().y, entity.position().z);
		EntityData data = new EntityData(position, entityName);
		data.setData(nbt2data(entity.serializeNBT()));
		return data;
	}
	
	/**
	 * Constructs an actual entity from an data type.
	 * @param data The entity data type
	 * @return The actual entity instance or null if it was not possible to convert.
	 */
	public static Entity data2entity(EntityData data) {
		if (data == null) return null;
		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(data2resLoc(data.getEntityName()));
		if (type == null) return null;
		Vec3 position = new Vec3(data.getPosition().x, data.getPosition().y, data.getPosition().z);
		@SuppressWarnings("resource")
		Entity entity = type.create(Minecraft.getInstance().level);
		if (data.getData() != null) entity.load(data2nbt(data.getData()));
		entity.setPos(position);
		return entity;
	}
	
	/**
	 * Converts an {@link TagCompound} to an {@link CompoundTag}
	 * @param data The {@link TagCompound}
	 * @return An identical {@link CompoundTag}, null if the parameter was null or an empty compound if an error occurred
	 */
	public static CompoundTag data2nbt(TagCompound data) {
		if (data == null) return null;
		try {
			byte[] nbtBin = BinaryParser.toBytes(data, "", false);
			return NbtIo.read(new DataInputStream(new ByteArrayInputStream(nbtBin)), NbtAccounter.unlimitedHeap());
		} catch (IOException e) {
			HoloStruct.LOGGER.warn("failed to convert nbt data!");
			e.printStackTrace();
			return new CompoundTag();
		}
	}
	
	/**
	 * Converts an {@link CompoundTag} to an {@link TagCompound}
	 * @param data The {@link CompoundTag}
	 * @return An identical {@link TagCompound}, null if the parameter was null or an empty compound if an error occurred
	 */
	public static TagCompound nbt2data(CompoundTag nbt) {
		if (nbt == null) return null;
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			NbtIo.write(nbt, new DataOutputStream(buffer));
			return BinaryParser.fromBytes(buffer.toByteArray(), TagCompound.class, false);
		} catch (IOException e) {
			System.err.println("failed to convert nbt data!");
			e.printStackTrace();
			return new TagCompound();
		}
	}
	
	/**
	 * Converts an {@link RegistryName} to an {@link ResourceLocation}
	 * @param data The {@link RegistryName}
	 * @return An identical {@link ResourceLocation}
	 */
	public static ResourceLocation data2resLoc(RegistryName data) {
		return new ResourceLocation(data.getNamespace(), data.getName());
	}
	
	/**
	 * Converts an {@link ResourceLocation} to an {@link RegistryName}
	 * @param data The {@link ResourceLocation}
	 * @return An identical {@link RegistryName}
	 */
	public static RegistryName resLoc2data(ResourceLocation resLoc) {
		return new RegistryName(resLoc.getNamespace(), resLoc.getPath());
	}
	
}
