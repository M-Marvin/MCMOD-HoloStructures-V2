package de.m_marvin.holostruct.client.levelbound.access;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import de.m_marvin.blueprints.api.IStructAccessor;
import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.univec.impl.Vec3i;

/**
 * Like the {@link IStructAccessor} but asynchronous
 * @author Marvin Koehler
 */
public interface IAsyncStructAccessor {
	
	public CompletableFuture<Boolean> setBlock(Vec3i position, BlockStateData state);
	public CompletableFuture<BlockStateData> getBlock(Vec3i position);
	
	public CompletableFuture<Boolean> setBlockEntity(Vec3i position, BlockEntityData blockEntity);
	public CompletableFuture<BlockEntityData> getBlockEntity(Vec3i position);
	
	public CompletableFuture<Boolean> addEntity(EntityData entity);
	public CompletableFuture<Boolean> addEntities(Collection<EntityData> entities);
	public CompletableFuture<Collection<EntityData>> getEntitiesOnBlock(Vec3i pos);
	public CompletableFuture<Collection<EntityData>> getEntitiesWithin(Vec3i min, Vec3i max);
	
}
