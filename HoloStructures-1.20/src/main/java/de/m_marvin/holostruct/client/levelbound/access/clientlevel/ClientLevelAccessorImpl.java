package de.m_marvin.holostruct.client.levelbound.access.clientlevel;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.blueprints.TypeConverter;
import de.m_marvin.holostruct.client.levelbound.Levelbound.AccessLevel;
import de.m_marvin.holostruct.client.levelbound.access.AccessDeniedException;
import de.m_marvin.holostruct.client.levelbound.access.IRemoteLevelAccessor;
import de.m_marvin.holostruct.client.levelbound.access.clientlevel.commands.AddEntityCommand;
import de.m_marvin.holostruct.client.levelbound.access.clientlevel.commands.Command;
import de.m_marvin.holostruct.client.levelbound.access.clientlevel.commands.SetBlockEntityCommand;
import de.m_marvin.holostruct.client.levelbound.access.clientlevel.commands.SetBlockStateCommand;
import de.m_marvin.holostruct.client.levelbound.access.serverlevel.ServerLevelAccessorImpl;
import de.m_marvin.univec.impl.Vec3d;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * The client level accessor is used if the mod is not installed on the server side.
 * It communicates with the server using various ways that are available without an server sided mod.
 * This implementation is usually slower than the @link {@link ServerLevelAccessorImpl}
 * @author Marvin Koehler
 *
 */
public class ClientLevelAccessorImpl implements IRemoteLevelAccessor {

	private final Minecraft minecraft;
	private boolean allowCopyOperations;
	private boolean allowModifyOperations;
	
	public ClientLevelAccessorImpl(Minecraft minecraft, boolean allowCopy, boolean allowModify) {
		this.minecraft = minecraft;
		this.allowCopyOperations = allowCopy;
		this.allowModifyOperations = allowModify;
	}
	
	@Override
	public AccessLevel getAccessLevel() {
		boolean isPrivileged = this.minecraft.player.hasPermissions(2);
		return this.allowCopyOperations ? (this.allowModifyOperations && isPrivileged) ? AccessLevel.FULL_CLIENT : AccessLevel.COPY_CLIENT : AccessLevel.READ_CLIENT;
	}

	@Override
	public CompletableFuture<Boolean> setBlock(Vec3i position, BlockStateData state) {
		if (!allowModifyOperations)
			throw new AccessDeniedException("modify operation denied!");
		
		Command<Boolean> command = new SetBlockStateCommand(position, state);
		return HoloStruct.CLIENT.COMMAND_DISPATCHER.startDispatch(command);
	}
	
	@Override
	public CompletableFuture<BlockStateData> getBlock(Vec3i position) {
		BlockState state = this.minecraft.player.level().getBlockState(new BlockPos(position.x, position.y, position.z));
		CompletableFuture<BlockStateData> future = new CompletableFuture<>();
		future.complete(TypeConverter.blockState2data(state));
		return future;
	}

	@Override
	public CompletableFuture<Boolean> setBlockEntity(Vec3i position, BlockEntityData blockEntity) {
		if (!allowModifyOperations)
			throw new AccessDeniedException("modify operation denied!");
		
		Command<Boolean> command = new SetBlockEntityCommand(blockEntity);
		return HoloStruct.CLIENT.COMMAND_DISPATCHER.startDispatch(command);
	}

	@Override
	public CompletableFuture<BlockEntityData> getBlockEntity(Vec3i position) {
		if (!this.minecraft.player.hasPermissions(2)) {
			return CompletableFuture.completedFuture(null);
		}
		
		BlockPos pos = new BlockPos(position.x, position.y, position.z);
		BlockEntity blockEntiy = minecraft.level.getBlockEntity(pos);
		if (blockEntiy == null) return CompletableFuture.completedFuture(null);
		
		ResourceLocation typeName = BlockEntityType.getKey(blockEntiy.getType());
		BlockEntityData data = new BlockEntityData(position, TypeConverter.resLoc2data(typeName));
		
		CompletableFuture<BlockEntityData> querryFuture = new CompletableFuture<>();
		this.minecraft.player.connection.getDebugQueryHandler().queryBlockEntityTag(pos, nbt -> {
			data.setData(TypeConverter.nbt2data(nbt));
			querryFuture.complete(data);
		});
		return querryFuture;
	}
	
	@Override
	public CompletableFuture<Boolean> addEntity(EntityData entity) {
		if (!allowModifyOperations)
			throw new AccessDeniedException("modify operation denied!");
		
		Command<Boolean> command = new AddEntityCommand(entity);
		return HoloStruct.CLIENT.COMMAND_DISPATCHER.startDispatch(command);
	}
	
	@Override
	public CompletableFuture<Boolean> addEntities(Collection<EntityData> entities) {
		if (!allowModifyOperations)
			throw new AccessDeniedException("modify operation denied!");
		
		List<CompletableFuture<Boolean>> futures = entities.stream().map(this::addEntity).toList();
		return CompletableFuture.allOf(futures.toArray(i -> new CompletableFuture[i]))
				.thenApply(v -> futures.stream().map(CompletableFuture::join).reduce((a, b) -> a & b).get());
	}
	
	@Override
	public CompletableFuture<Collection<EntityData>> getEntitiesOnBlock(Vec3i pos) {
		if (!this.minecraft.player.hasPermissions(2)) {
			return CompletableFuture.completedFuture(null);
		}
		
		List<CompletableFuture<EntityData>> queryFutures = this.minecraft.level.getEntities(null, new AABB(new BlockPos(pos.x, pos.y, pos.z))).stream()
				.map(entity -> {
					CompletableFuture<CompoundTag> querryFuture = new CompletableFuture<>();
					this.minecraft.player.connection.getDebugQueryHandler().queryEntityTag(entity.getId(), querryFuture::complete);
					return querryFuture.thenApply(nbt -> {
						ResourceLocation typeName = EntityType.getKey(entity.getType());
						Vec3d position = Vec3d.fromVec(entity.position());
						
						EntityData data = new EntityData(position, TypeConverter.resLoc2data(typeName));
						data.setData(TypeConverter.nbt2data(nbt));
						return data;
					});
				})
				.toList();
		return CompletableFuture.allOf(queryFutures.toArray(i -> new CompletableFuture[i]))
				.thenApply(v -> queryFutures.stream().map(CompletableFuture::join).toList());
	}

	@Override
	public CompletableFuture<Collection<EntityData>> getEntitiesWithin(Vec3i min, Vec3i max) {
		if (!this.minecraft.player.hasPermissions(2)) {
			return CompletableFuture.completedFuture(null);
		}
		
		List<CompletableFuture<EntityData>> queryFutures = this.minecraft.level.getEntities(null, new AABB(min.x, min.y, min.z, max.x, max.y, max.z)).stream()
				.map(entity -> {
					CompletableFuture<CompoundTag> queryFuture = new CompletableFuture<>();
					this.minecraft.player.connection.getDebugQueryHandler().queryEntityTag(entity.getId(), queryFuture::complete);
					return queryFuture.thenApply(nbt -> {
						ResourceLocation typeName = EntityType.getKey(entity.getType());
						Vec3d position = Vec3d.fromVec(entity.position());
						
						EntityData data = new EntityData(position, TypeConverter.resLoc2data(typeName));
						data.setData(TypeConverter.nbt2data(nbt));
						return data;
					});
				})
				.toList();
		return CompletableFuture.allOf(queryFutures.toArray(i -> new CompletableFuture[i]))
				.thenApply(v -> queryFutures.stream().map(CompletableFuture::join).toList());
	}
	
	/* Default fake level methods, only affect client level */
	
	@Override
	public List<Entity> getEntitiesInBounds(AABB bounds) {
		return this.minecraft.level.getEntities(null, bounds);
	}

	@Override
	public void setBlock(BlockPos pos, BlockState sate) {
		this.minecraft.level.setBlock(pos, sate, 0);
	}

	@Override
	public BlockState getBlock(BlockPos pos) {
		return minecraft.level.getBlockState(pos);
	}

	@Override
	public LevelAccessor getLevel() {
		return minecraft.level;
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pPos) {
		return minecraft.level.getBlockEntity(pPos);
	}

}
