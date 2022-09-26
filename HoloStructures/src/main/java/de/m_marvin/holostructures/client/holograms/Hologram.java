package de.m_marvin.holostructures.client.holograms;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Function;

import de.m_marvin.holostructures.client.blueprints.Blueprint;
import de.m_marvin.holostructures.client.blueprints.Blueprint.EntityData;
import de.m_marvin.holostructures.client.worldaccess.ILevelAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class Hologram implements ILevelAccessor {
	
	public BlockPos position;
	public String name;
	public Long2ObjectMap<HologramChunk> chunks;
	public Int2ObjectMap<Entity> entities;
	public Level level;
	
	public Hologram(Level level, BlockPos position, String name) {
		this.level = level;
		this.position = position;
		this.name = name;
		this.chunks = new Long2ObjectArrayMap<HologramChunk>();
		this.entities = new Int2ObjectArrayMap<>();
	}
	
	public void setCornerWorldPosition(Corner corner, BlockPos position) {
		
	}
	
	public BlockPos getCornerWorldPosition(Corner corner) {
		
	}
	
	public BlockPos holoToWorldPosition(BlockPos holoPosition) {
		
	}
	
	public BlockPos worldToHoloPosition(BlockPos worldPosition) {
		
	}
	
	public BlockPos getBoundingSize() {
		
	}
	
	public BlockPos getPosition() {
		return position;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public String getName() {
		return name;
	}
	
	public ObjectCollection<Entity> getEntities() {
		return entities.values();
	}
	
	public ObjectCollection<HologramChunk> getChunks() {
		return chunks.values();
	}
	
	public HologramChunk getOrCreateChunk(ChunkPos pos) {
		Optional<HologramChunk> chunk = getChunk(pos);
		if (chunk.isPresent()) return chunk.get();
		HologramChunk freshChunk = new HologramChunk(pos, this.level);
		this.chunks.put(pos.toLong(), freshChunk);
		return freshChunk;
	}
	
	public Optional<HologramChunk> getChunk(ChunkPos pos) {
		return Optional.ofNullable(this.chunks.get(pos.toLong()));
	}
	
	public Optional<HologramChunk> getChunkAt(BlockPos position) {
		return getChunk(new ChunkPos(position));
	}
	
	public HologramChunk getOrCreateChunkAt(BlockPos position) {
		ChunkPos pos = new ChunkPos(position);
		return getOrCreateChunk(pos);
	}
	
	public void discardChunk(HologramChunk chunk) {
		this.chunks.remove(chunk.getPosition().toLong());
	}
	
	public void setBlock(BlockPos position, BlockState state) {
		getOrCreateChunkAt(position).setBlock(position, state);
	}
	
	public BlockState getBlock(BlockPos position) {
		Optional<HologramChunk> chunk = getChunkAt(position);
		if (chunk.isPresent()) return chunk.get().getBlock(position);
		return Blocks.AIR.defaultBlockState();
	}
	
	public void setBlockEntity(BlockPos position, BlockEntity blockentity) {
		Optional<HologramChunk> chunk = getChunkAt(position);
		if (chunk.isPresent()) chunk.get().setBlockEntity(position, blockentity);
	}
	
	public Optional<BlockEntity> getBlockEntity(BlockPos position) {
		Optional<HologramChunk> chunk = getChunkAt(position);
		if (chunk.isPresent()) return chunk.get().getBlockEntity(position);
		return Optional.empty();
	}
	
	public List<Entity> getEntitiesInBounds(AABB bounds) {
		return this.entities.values().stream().filter((entity) -> bounds.intersects(entity.getBoundingBox())).toList();
	}
	
	public Entity getEntityById(int id) {
		return this.entities.get(id);
	}
	
	public void addEntity(Entity entity) {
		if (this.entities.containsKey(entity.getId())) {
			this.entities.put(entity.getId(), entity);
		} else {
			throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Entity is already tracked!"));
		}
	}
	
	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public boolean hasWriteAccess() {
		return true;
	}

	@Override
	public boolean isDoneAccessing() {
		return true;
	}
	
	@Override
	public BlockAndTintGetter getLevelGetter() {
		return this.level;
	}

	@Override
	public void abbortAccessing() {}
	
	@Override
	public boolean checkBlock(BlockPos pos, BlockState state) {
		if (!this.getBlock(pos).equals(state)) {
			this.setBlock(pos, state);
		}
		return true;
	}

	@Override
	public Optional<EntityData> getBlockEntityData(BlockPos pos) {
		Optional<BlockEntity> blockentity = getBlockEntity(pos);
		if (blockentity.isPresent()) {
			return Optional.of(
					new Blueprint.EntityData(
							blockentity.get().getType().getRegistryName(), 
							() -> Optional.of(Blueprint.BLOCK_ENTITY_DATA_FILTER.apply(blockentity.get().serializeNBT()))
						)
					);
		}
		return Optional.empty();
	}
	
	@Override
	public void setBlockEntityData(BlockPos pos, EntityData data) {
		BlockEntityType<?> type = ForgeRegistries.BLOCK_ENTITIES.getValue(data.type());
		if (type != null) {
			BlockState state = getBlock(pos);
			if (!type.isValid(state)) throw new IllegalStateException("BlockEntity of type " + type.getRegistryName() + " is invalid for state " + state.toString() + "!");
			BlockEntity blockentity = type.create(pos, state);
			if (data.nbt().get().isPresent()) blockentity.deserializeNBT(data.nbt().get().get());
			setBlockEntity(pos, blockentity);
		}
	}
	
	@Override
	public Map<Vec3, EntityData> getEntitiesData(BlockPos corner1, BlockPos corner2, Function<Vec3, Vec3> positionMapper) {
		AABB aabb = new AABB(corner1, corner2);
		return getEntitiesInBounds(aabb).stream().collect(Collectors.toMap((entity) -> new Vec3(entity.xo, entity.yo, entity.zo), (entity) -> 
				new Blueprint.EntityData(entity.getType().getRegistryName(), () -> Optional.of(Blueprint.BLOCK_ENTITY_DATA_FILTER.apply(entity.serializeNBT())))
		));
	}

	@Override
	public void addEntityData(Vec3 pos, EntityData data) {
		EntityType<?> type = ForgeRegistries.ENTITIES.getValue(data.type());
		if (type != null) {
			Entity entity = type.create(this.level);
			if (data.nbt().get().isPresent()) entity.deserializeNBT(data.nbt().get().get());
			addEntity(entity);
		}
	}
	
}
