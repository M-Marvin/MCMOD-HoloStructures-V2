package de.m_marvin.holostruct.client.levelbound.access;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.holostruct.client.levelbound.Levelbound;
import de.m_marvin.holostruct.client.levelbound.Levelbound.AccessLevel;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Special case implementation of {@link IRemoteLevelAccessor}, returned from {@link Levelbound} if no level is available at all.
 * This is only active if the player has not yet yoined any level or server or the server denies all access to the level.
 * <b>NOTE</b>: This implementation just always denies every request with an {@link AccessDeniedException}
 * @author Marvin Koehler
 */
public class NoAccessAccessor implements IRemoteLevelAccessor {

	@Override
	public AccessLevel getAccessLevel() {
		return AccessLevel.NO_ACCESS;
	}

	@Override
	public CompletableFuture<Boolean> setBlock(Vec3i position, BlockStateData state) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public CompletableFuture<BlockStateData> getBlock(Vec3i position) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public CompletableFuture<Boolean> setBlockEntity(Vec3i position, BlockEntityData blockEntity) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public CompletableFuture<BlockEntityData> getBlockEntity(Vec3i position) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public CompletableFuture<Boolean> addEntity(EntityData entity) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public CompletableFuture<Boolean> addEntities(Collection<EntityData> entities) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public CompletableFuture<Collection<EntityData>> getEntitiesOnBlock(Vec3i pos) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public CompletableFuture<Collection<EntityData>> getEntitiesWithin(Vec3i min, Vec3i max) {
		throw new AccessDeniedException("no access to game world!");
	}
	
	@Override
	public List<Entity> getEntitiesInBounds(AABB bounds) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public void setBlock(BlockPos pos, BlockState sate) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public BlockState getBlock(BlockPos pos) {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public LevelAccessor getLevel() {
		throw new AccessDeniedException("no access to game world!");
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pPos) {
		throw new AccessDeniedException("no access to game world!");
	}
	
}
