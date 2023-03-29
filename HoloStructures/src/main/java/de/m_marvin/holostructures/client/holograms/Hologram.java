package de.m_marvin.holostructures.client.holograms;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Function;

import de.m_marvin.holostructures.client.blueprints.Blueprint;
import de.m_marvin.holostructures.client.blueprints.Blueprint.EntityData;
import de.m_marvin.holostructures.client.rendering.HolographicRenderer;
import de.m_marvin.holostructures.client.worldaccess.ILevelAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class Hologram implements ILevelAccessor {
	
	public BlockPos position;
	public BlockPos origin;
	public String name;
	public Long2ObjectMap<HologramChunk> chunks;
	public Int2ObjectMap<Entity> entities;
	public Level level;
	
	public Hologram(Level level, BlockPos position, String name) {
		this.level = level;
		this.position = position;
		this.origin = BlockPos.ZERO;
		this.name = name;
		this.chunks = new Long2ObjectArrayMap<HologramChunk>();
		this.entities = new Int2ObjectArrayMap<>();
	}
	
	public void setCornerWorldPosition(Corner corner, BlockPos position) {
		setPosition(position.subtract(corner.map(this)));
	}
	
	public BlockPos getCornerWorldPosition(Corner corner) {
		return getPosition().offset(corner.map(this));
	}
	
	public BlockPos holoToWorldPosition(BlockPos holoPosition) {
		return holoPosition.offset(getPosition());
	}
	
	public BlockPos worldToHoloPosition(BlockPos worldPosition) {
		return worldPosition.subtract(getPosition());
	}
	
	public AABB getBoundingBox() {
		if (this.chunks.isEmpty()) {
			return new AABB(-1, -1, -1, 1, 1, 1); 
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
			
			return new AABB(minBlockX, minBlockY, minBlockZ, maxBlockX + 1, maxBlockY + 1, maxBlockZ + 1);
		}
	}
	
	public BlockPos getOrigin() {
		return origin;
	}
	
	public void setOrigin(BlockPos origin) {
		this.origin = origin;
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
	
	public String getName() {
		return name;
	}
	
	public ObjectCollection<Entity> getEntities() {
		return entities.values();
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
	
	public void setBlock(BlockPos position, BlockState state) {
		Optional<HologramChunk> chunk = getOrCreateChunkAt(position, !state.isAir());
		if (chunk.isEmpty()) return;
		chunk.get().setBlock(position, state);
		if (chunk.get().isEmpty()) discardChunk(chunk.get());
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

	public void refreshChunk(ChunkPos chunk) {
		HolographicRenderer.markDirty(this, chunk);
	}
	
	public void refreshAllChunks() {
		this.chunks.forEach((lp, chunk) -> {
			HolographicRenderer.markDirty(this, new ChunkPos(lp));
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
