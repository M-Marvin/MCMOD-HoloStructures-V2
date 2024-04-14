package de.m_marvin.holostructures.client.holograms;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.holostructures.client.levelbound.TypeConverter;
import de.m_marvin.holostructures.client.levelbound.access.ILevelAccessor;
import de.m_marvin.univec.impl.Vec3i;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class Hologram implements ILevelAccessor {
	
	public BlockPos boundsMax;
	public BlockPos boundsMin;
	public BlockPos position;
	public BlockPos origin;
	public Long2ObjectMap<HologramChunk> chunks;
	public Int2ObjectMap<Entity> entities;
	public Level level;
	
	public Hologram(Level level, BlockPos position) {
		this.level = level;
		this.position = position;
		this.boundsMin = BlockPos.ZERO;
		this.boundsMax = BlockPos.ZERO.offset(1, 1, 1);
		this.origin = BlockPos.ZERO;
		this.chunks = new Long2ObjectArrayMap<HologramChunk>();
		this.entities = new Int2ObjectArrayMap<>();
	}
	
	public BlockPos holoToWorldPosition(BlockPos holoPosition) {
		return holoPosition.offset(getPosition().subtract(this.origin));
	}
	
	public BlockPos worldToHoloPosition(BlockPos worldPosition) {
		return worldPosition.subtract(getPosition().subtract(this.origin));
	}

	public BlockPos getOrigin() {
		return origin;
	}
	
	public void setOrigin(BlockPos origin) {
		this.origin = origin;
	}

	@Override
	public void setBounds(Vec3i min, Vec3i max) {
		this.boundsMax = new BlockPos(max.x, max.y, max.z);
		this.boundsMin = new BlockPos(min.x, min.y, min.z);
		ensureMinBounds();
	}
	
	@Override
	public Vec3i getBoundsMax() {
		return new Vec3i(this.boundsMax.getX(), this.boundsMax.getY(), this.boundsMax.getZ());
	}
	
	@Override
	public Vec3i getBoundsMin() {
		return new Vec3i(this.boundsMin.getX(), this.boundsMin.getY(), this.boundsMin.getZ());
	}
	
	public BlockPos getBlockBoundsMax() {
		return this.boundsMax;
	}
	
	public BlockPos getBlockBoundsMin() {
		return this.boundsMin;
	}
	
	protected void ensureMinBounds() {
		if (this.chunks.isEmpty()) {
			this.boundsMax = Vec3i.fromVec(this.boundsMin).max(Vec3i.fromVec(this.boundsMin).add(new Vec3i(1, 1, 1))).writeTo(new BlockPos(0, 0, 0));
		} else {
			int maxChunkX = Stream.of(this.chunks.values().toArray((l) -> new HologramChunk[l])).filter((h) -> !h.isEmpty()).mapToInt((chunk) -> chunk.getPosition().x).max().getAsInt();
			int maxChunkZ = Stream.of(this.chunks.values().toArray((l) -> new HologramChunk[l])).filter((h) -> !h.isEmpty()).mapToInt((chunk) -> chunk.getPosition().z).max().getAsInt();
			int minChunkX = Stream.of(this.chunks.values().toArray((l) -> new HologramChunk[l])).filter((h) -> !h.isEmpty()).mapToInt((chunk) -> chunk.getPosition().x).min().getAsInt();
			int minChunkZ = Stream.of(this.chunks.values().toArray((l) -> new HologramChunk[l])).filter((h) -> !h.isEmpty()).mapToInt((chunk) -> chunk.getPosition().z).min().getAsInt();
			
			int minBlockX = Stream.of(this.chunks.values().toArray((l) -> new HologramChunk[l])).filter((c) -> c.getPosition().x == minChunkX).mapToInt((chunk) -> chunk.getLowestAxis(Axis.X)).min().getAsInt();
			int maxBlockX = Stream.of(this.chunks.values().toArray((l) -> new HologramChunk[l])).filter((c) -> c.getPosition().x == maxChunkX).mapToInt((chunk) -> chunk.getHighestAxis(Axis.X)).max().getAsInt();
			int minBlockZ = Stream.of(this.chunks.values().toArray((l) -> new HologramChunk[l])).filter((c) -> c.getPosition().z == minChunkZ).mapToInt((chunk) -> chunk.getLowestAxis(Axis.Z)).min().getAsInt();
			int maxBlockZ = Stream.of(this.chunks.values().toArray((l) -> new HologramChunk[l])).filter((c) -> c.getPosition().z == maxChunkZ).mapToInt((chunk) -> chunk.getHighestAxis(Axis.Z)).max().getAsInt();
			int minBlockY = Stream.of(this.chunks.values().toArray((l) -> new HologramChunk[l])).mapToInt((chunk) -> chunk.getLowestAxis(Axis.Y)).min().getAsInt();
			int maxBlockY = Stream.of(this.chunks.values().toArray((l) -> new HologramChunk[l])).mapToInt((chunk) -> chunk.getHighestAxis(Axis.Y)).max().getAsInt();
			
			this.boundsMin = Vec3i.fromVec(this.boundsMin).min(new Vec3i(minBlockX, minBlockY, minBlockZ)).writeTo(new BlockPos(0, 0, 0));
			this.boundsMax = Vec3i.fromVec(this.boundsMax).max(new Vec3i(maxBlockX, maxBlockY, maxBlockZ)).writeTo(new BlockPos(0, 0, 0));
		}
	}
	
	@Override
	public void setOffset(Vec3i offset) {
		this.origin = new BlockPos(offset.x, offset.y, offset.z);
	}
	
	@Override
	public Vec3i getOffset() {
		return new Vec3i(this.origin.getX(), this.origin.getY(), this.origin.getZ());
	}
	
	public BlockPos getPosition() {
		return position;
	}
	
	public void setPosition(BlockPos position) {
		this.position = position;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public ObjectCollection<HologramChunk> getChunks() {
		return chunks.values();
	}
	
	public Optional<HologramChunk> getChunk(ChunkPos pos) {
		HologramChunk chunk = this.chunks.get(pos.toLong());
		if (chunk != null && chunk.isEmpty()) {
			this.chunks.remove(pos.toLong());
			return Optional.empty();
		}
		return Optional.ofNullable(chunk);
	}
	
	public Optional<HologramChunk> getChunkAt(BlockPos position) {
		return getChunk(new ChunkPos(position));
	}
	
	public Optional<HologramChunk> getOrCreateChunk(ChunkPos pos, boolean createIfEmpty) {
		HologramChunk chunk = this.chunks.get(pos.toLong());
		if (chunk == null && createIfEmpty) {
			chunk = new HologramChunk(pos);
			this.chunks.put(pos.toLong(), chunk);
		}
		return Optional.ofNullable(chunk);
	}
	
	public Optional<HologramChunk> getOrCreateChunkAt(BlockPos position, boolean createIfEmpty) {
		ChunkPos pos = new ChunkPos(position);
		return getOrCreateChunk(pos, createIfEmpty);
	}
	
	public void discardChunk(HologramChunk chunk) {
		this.chunks.remove(chunk.getPosition().toLong());
	}
	
	public void markChunkDirty(ChunkPos chunk) {
		HoloStructures.CLIENT.HOLORENDERER.markDirty(this, chunk);
	}
	
	public void markDirtyAllChunks() {
		this.chunks.forEach((lp, chunk) -> {
			HoloStructures.CLIENT.HOLORENDERER.markDirty(this, new ChunkPos(lp));
		});
	}
	
	/* standard level accessing methods */
	
	public void setBlock(BlockPos position, BlockState state) {
		Optional<HologramChunk> chunk = getOrCreateChunkAt(position, !state.isAir());
		if (chunk.isEmpty()) return;
		chunk.get().setBlock(position, state);
		if (chunk.get().isEmpty()) discardChunk(chunk.get());
		markChunkDirty(chunk.get().getPosition());
	}
	
	public BlockState getBlock(BlockPos position) {
		Optional<HologramChunk> chunk = getChunkAt(position);
		if (chunk.isPresent()) return chunk.get().getBlock(position);
		return Blocks.AIR.defaultBlockState();
	}
	
	public void setBlockEntity(BlockPos position, BlockEntity blockentity) {
		Optional<HologramChunk> chunk = getChunkAt(position);
		if (chunk.isPresent()) chunk.get().setBlockEntity(position, blockentity);
		markChunkDirty(chunk.get().getPosition());
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
	
	/* struct accessor methods */

	@Override
	public void setBlock(Vec3i position, BlockStateData state) {
		setBlock(new BlockPos(position.x, position.y, position.z), TypeConverter.data2blockState(state));
	}
	
	@Override
	public BlockStateData getBlock(Vec3i position) {
		return TypeConverter.blockState2data(getBlock(new BlockPos(position.x, position.y, position.z)));
	}

	@Override
	public void setBlockEntity(Vec3i position, BlockEntityData blockEntity) {
		if (blockEntity == null) setBlockEntity(new BlockPos(position.x, position.y, position.z), null);
		setBlockEntity(new BlockPos(position.x, position.y, position.z), TypeConverter.data2blockEntity(blockEntity));
	}

	@Override
	public BlockEntityData getBlockEntity(Vec3i position) {
		Optional<BlockEntity> blockEntity = getBlockEntity(new BlockPos(position.x, position.y, position.z));
		if (blockEntity.isEmpty()) return null;
		return TypeConverter.blockEntity2data(blockEntity.get());
	}

	@Override
	public void addEntity(EntityData entity) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void addEntity(Vec3i blockPos, EntityData entity) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void addEntities(Collection<EntityData> entities) {
		// TODO Auto-generated method stub
//		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Collection<EntityData> getEntities() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Collection<EntityData> getEntitiesOnBlock(Vec3i pos) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Collection<EntityData> getEntitiesWithin(Vec3i min, Vec3i max) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

//	public BlockPos getOrigin() {
//		return origin;
//	}
//	
//	public void setOrigin(BlockPos origin) {
//		this.origin = origin;
//	}
	
//	public ObjectCollection<Entity> getEntities() {
//		return entities.values();
//	}
	
//	@Override
//	public BlockAndTintGetter getLevelGetter() {
//		return this.level;
//	}
	
//	@Override
//	public boolean checkBlock(BlockPos pos, BlockState state) {
//		if (!this.getBlock(pos).equals(state)) {
//			this.setBlock(pos, state);
//		}
//		return true;
//	}

//	@Override
//	public Optional<BlockEntityData> getBlockEntityData(BlockPos pos) {
//		Optional<BlockEntity> blockentity = getBlockEntity(pos);
//		if (blockentity.isPresent()) {
//			return Optional.of(TypeConverter.blockEntity2data(blockentity.get()));
//		}
//		return Optional.empty();
//	}
	
//	@Override
//	public void setBlockEntityData(BlockPos pos, BlockEntityData data) {
//		BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(TypeConverter.data2resLoc(data.getTypeName()));
//		if (type != null) {
//			BlockState state = getBlock(pos);
//			if (!type.isValid(state)) throw new IllegalStateException("BlockEntity of type " + data.getTypeName() + " is invalid for state " + state.toString() + "!");
//			BlockEntity blockentity = type.create(pos, state);
//			blockentity.deserializeNBT(TypeConverter.data2nbt(data.getData()));
//			setBlockEntity(pos, blockentity);
//		}
//	}
	
//	@Override
//	public Map<Vec3, EntityData> getEntitiesData(BlockPos corner1, BlockPos corner2, Function<Vec3, Vec3> positionMapper) {
//		AABB aabb = AABB.encapsulatingFullBlocks(corner1, corner2);
//		return getEntitiesInBounds(aabb).stream().collect(Collectors.toMap((entity) -> new Vec3(entity.xo, entity.yo, entity.zo), (entity) -> TypeConverter.entity2data(entity)));
//	}

//	@Override
//	public void addEntityData(Vec3 pos, EntityData data) {
//		addEntity(TypeConverter.data2entity(data));
//	}
	
	/* end of accessors */
	
	public void refreshChunk(ChunkPos chunk) {
//		HolographicRenderer.markDirty(this, chunk);
	}
	
	public void refreshAllChunks() {
		this.chunks.forEach((lp, chunk) -> {
//			HolographicRenderer.markDirty(this, new ChunkPos(lp));
		});
	}

	public void updateAllHoloBlockStates() {
		this.chunks.keySet().forEach((chunkLong) -> {
			updateChunkHoloBlockStates(new ChunkPos(chunkLong));
		});
	}
	
	public void updateChunkHoloBlockStates(ChunkPos chunkPos) {
		Optional<HologramChunk> chunk = getChunk(chunkPos);
		if (chunk.isPresent()) {
			BlockPos chunkPosition = this.position.offset(chunkPos.x << 4, 0, chunkPos.z << 4);
			chunk.get().getSections().forEach((positionIndex, section) -> {
				BlockPos sectionPosition = chunkPosition.offset(0, positionIndex << 4, 0);
				section.getStates().forEach((positionLong, holoBlockState) -> {
					BlockHoloState holoState = null;
					if (!holoBlockState.isAir()) {
						BlockPos worldPosition = sectionPosition.offset(BlockPos.getX(positionLong), BlockPos.getY(positionLong), BlockPos.getZ(positionLong));
						BlockState realBlockState = this.level.getBlockState(worldPosition);
						Optional<BlockEntity> holoBlockEntity = chunk.get().getBlockEntity(worldPosition);
						Optional<BlockEntity> realBlockEntity = Optional.ofNullable(this.level.getBlockEntity(worldPosition));
						holoState = BlockHoloState.getHoloState(holoBlockState, holoBlockEntity, realBlockState, realBlockEntity);
					}
					section.getHoloStates().put((long) positionLong, holoState);
				});
			});
			refreshChunk(chunkPos);
		}
	}
	
}
