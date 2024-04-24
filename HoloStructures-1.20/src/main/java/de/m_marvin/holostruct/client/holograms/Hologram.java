package de.m_marvin.holostruct.client.holograms;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.m_marvin.blueprints.api.IStructAccessor;
import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.blueprints.TypeConverter;
import de.m_marvin.holostruct.client.levelbound.Levelbound;
import de.m_marvin.holostruct.client.levelbound.access.IRemoteLevelAccessor;
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

public class Hologram implements IStructAccessor, IFakeLevelAccess {
	
	public BlockPos boundsMax;
	public BlockPos boundsMin;
	public BlockPos position;
	private boolean updateBounds;
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
		if (updateBounds) ensureMinBounds();
		return new Vec3i(this.boundsMax.getX(), this.boundsMax.getY(), this.boundsMax.getZ());
	}
	
	@Override
	public Vec3i getBoundsMin() {
		if (updateBounds) ensureMinBounds();
		return new Vec3i(this.boundsMin.getX(), this.boundsMin.getY(), this.boundsMin.getZ());
	}
	
	public BlockPos getBlockBoundsMax() {
		if (updateBounds) ensureMinBounds();
		return this.boundsMax;
	}
	
	public BlockPos getBlockBoundsMin() {
		if (updateBounds) ensureMinBounds();
		return this.boundsMin;
	}
	
	protected void ensureMinBounds() {
		if (this.chunks.isEmpty()) {
			this.boundsMax = Vec3i.fromVec(this.boundsMax).max(Vec3i.fromVec(this.boundsMin).add(new Vec3i(1, 1, 1))).writeTo(new BlockPos(0, 0, 0));
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
		this.updateBounds = false;
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
	
	public void markSectionDirty(ChunkPos pos, int section) {
		HoloStruct.CLIENT.HOLORENDERER.markDirty(this, pos, section);
	}
	
	public void markChunkDirty(ChunkPos pos) {
		Optional<HologramChunk> chunk = getChunk(pos);
		if (chunk.isPresent()) {
			chunk.get().getSections().keySet().forEach(section -> 
				HoloStruct.CLIENT.HOLORENDERER.markDirty(this, pos, section)
			);
		}
	}
	
	public void markDirtyAllChunks() {
		this.chunks.keySet().forEach(lp -> markChunkDirty(new ChunkPos(lp)));
	}
	
	public void setHoloState(BlockPos position, BlockHoloState state) {
		Optional<HologramChunk> chunk = getChunkAt(position);
		if (chunk.isPresent()) chunk.get().setHoloState(position, state);
	}

	public void setHoloState(Vec3i position, BlockHoloState state) {
		setHoloState(new BlockPos(position.x, position.y, position.z), state);
	}
	
	public BlockHoloState getHoloState(Vec3i position) {
		return getHoloState(new BlockPos(position.x, position.y, position.z));
	}

	public BlockHoloState getHoloState(BlockPos position) {
		Optional<HologramChunk> chunk = getChunkAt(position);
		if (chunk.isPresent()) return chunk.get().getHoloState(position);
		return BlockHoloState.CORRECT_BLOCK;
	}
	
	public void updateHoloStateAt(IRemoteLevelAccessor target, Vec3i holoPos) {
		if (!target.getAccessLevel().hasRead()) {
			setHoloState(holoPos, BlockHoloState.NO_BLOCK);
			return;
		}
		Vec3i targetPos = holoPos.add(Vec3i.fromVec(getPosition()));
		Levelbound.LEVEL_ACCESS_EXECUTOR.execute(() -> {
			BlockStateData targetState = target.getBlock(targetPos).join();
			BlockStateData holoState = getBlock(holoPos);
			BlockEntityData holoBE = getBlockEntity(holoPos);
			BlockEntityData targetBE = target.getBlockEntity(targetPos).join();
			BlockHoloState state = BlockHoloState.getHoloState(targetState, holoState, targetBE, holoBE);
			setHoloState(holoPos, state);
			markSectionDirty(new ChunkPos(new BlockPos(holoPos.x, holoPos.y, holoPos.z)), holoPos.y >> 4);
		});
	}
	
	public void updateHoloStates(IRemoteLevelAccessor target) {
		for (int y = this.boundsMin.getY(); y < this.boundsMax.getY(); y++) {
			for (int z = this.boundsMin.getZ(); z < this.boundsMax.getZ(); z++) {
				for (int x = this.boundsMin.getX(); x < this.boundsMax.getX(); x++) {
					Vec3i holoPos = new Vec3i(x, y, z);
					updateHoloStateAt(target, holoPos);
				}
			}
		}
	}
	
	/* standard level accessing methods */
	
	public void setBlock(BlockPos position, BlockState state) {
		Optional<HologramChunk> chunk = getOrCreateChunkAt(position, !state.isAir());
		if (chunk.isEmpty()) return;
		chunk.get().setBlock(position, state);
		if (chunk.get().isEmpty()) discardChunk(chunk.get());
		markSectionDirty(chunk.get().getPosition(), position.getY() >> 4);
		this.updateBounds = true;
	}
	
	public BlockState getBlock(BlockPos position) {
		Optional<HologramChunk> chunk = getChunkAt(position);
		if (chunk.isPresent()) return chunk.get().getBlock(position);
		return Blocks.AIR.defaultBlockState();
	}
	
	public void setBlockEntity(BlockPos position, BlockEntity blockentity) {
		Optional<HologramChunk> chunk = getChunkAt(position);
		if (chunk.isPresent()) {
			chunk.get().setBlockEntity(position, blockentity);
			blockentity.setLevel(this.level);
			markSectionDirty(chunk.get().getPosition(), position.getY() >> 4);
			this.updateBounds = true;
		}
	}
	
	public BlockEntity getBlockEntity(BlockPos position) {
		Optional<HologramChunk> chunk = getChunkAt(position);
		if (chunk.isPresent()) return chunk.get().getBlockEntity(position).orElseGet(() -> null);
		return null;
	}
	
	public List<Entity> getEntitiesInBounds(AABB bounds) {
		return this.entities.values().stream().filter(entity -> bounds.intersects(entity.getBoundingBox())).toList();
	}
	
	public List<Entity> getEntitiesInChunk(ChunkPos chunk) {
		return getEntitiesInBounds(AABB.encapsulatingFullBlocks(new BlockPos(chunk.getMinBlockX(), getBoundsMin().y, chunk.getMinBlockZ()), new BlockPos(chunk.getMaxBlockX(), getBoundsMax().y, chunk.getMaxBlockZ())));
	}

	public List<Entity> getEntitiesInSection(ChunkPos chunk, int section) {
		return getEntitiesInBounds(AABB.encapsulatingFullBlocks(new BlockPos(chunk.getMinBlockX(), section << 4, chunk.getMinBlockZ()), new BlockPos(chunk.getMaxBlockX(), (section + 1) << 4, chunk.getMaxBlockZ())));
	}
	
	public Entity getEntityById(int id) {
		return this.entities.get(id);
	}
	
	public void addEntity(Entity entity) {
		if (!this.entities.containsKey(entity.getId())) {
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
		BlockState state = getBlock(new BlockPos(position.x, position.y, position.z));
		setBlockEntity(new BlockPos(position.x, position.y, position.z), TypeConverter.data2blockEntity(state, blockEntity));
	}

	@Override
	public BlockEntityData getBlockEntity(Vec3i position) {
		BlockEntity blockEntity = getBlockEntity(new BlockPos(position.x, position.y, position.z));
		if (blockEntity == null) return null;
		return TypeConverter.blockEntity2data(blockEntity);
	}

	@Override
	public void addEntity(EntityData entity) {
		addEntity(TypeConverter.data2entity(entity));
	}

	@Override
	public void addEntity(Vec3i blockPos, EntityData entity) {
		addEntity(TypeConverter.data2entity(entity));
	}

	@Override
	public void addEntities(Collection<EntityData> entities) {
		entities.stream().map(TypeConverter::data2entity).forEach(this::addEntity);
	}

	@Override
	public Collection<EntityData> getEntities() {
		return this.entities.values().stream().map(TypeConverter::entity2data).toList();
	}

	@Override
	public Collection<EntityData> getEntitiesOnBlock(Vec3i pos) {
		return this.getEntitiesInBounds(new AABB(pos.x - 1, pos.y - 1, pos.z - 1, pos.x, pos.y, pos.z)).stream().map(TypeConverter::entity2data).toList();
	}

	@Override
	public Collection<EntityData> getEntitiesWithin(Vec3i min, Vec3i max) {
		return this.getEntitiesInBounds(new AABB(min.x, min.y, min.z, max.x, max.y, max.z)).stream().map(TypeConverter::entity2data).toList();
	}

	@Override
	public void copyTo(IStructAccessor target) {
		target.setBounds(getBoundsMin(), getBoundsMax());
		target.setOffset(getOffset());
		this.chunks.forEach((lp, chunk) -> {
			ChunkPos chunkpos = new ChunkPos(lp);
			chunk.sections.forEach((yi, section) -> {
				Vec3i sectionpos = new Vec3i(chunkpos.getMinBlockX(), yi << 4, chunkpos.getMinBlockZ());
				section.states.forEach((blp, state) -> {
					Vec3i pos = sectionpos.add(Vec3i.fromVec(BlockPos.of(blp)));
					target.setBlock(pos, TypeConverter.blockState2data(state));
				});
			});
			chunk.blockentities.forEach((p, blockEntity) -> {
				Vec3i pos = new Vec3i(chunkpos.getMinBlockX(), 0, chunkpos.getMinBlockZ()).add(Vec3i.fromVec(p));
				target.setBlockEntity(pos, TypeConverter.blockEntity2data(blockEntity));
			});
		});
		target.addEntities(this.getEntities());
	}
	
	@Override
	public void clearParseLogs() {}

	@Override
	public void logParseWarn(String errorMessage) {}
	
	/* end of accessors */
	
}
