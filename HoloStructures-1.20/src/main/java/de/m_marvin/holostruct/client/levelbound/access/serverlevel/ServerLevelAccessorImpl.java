package de.m_marvin.holostruct.client.levelbound.access.serverlevel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.holostruct.client.levelbound.Levelbound.AccessLevel;
import de.m_marvin.holostruct.client.levelbound.access.AccessDeniedException;
import de.m_marvin.holostruct.client.levelbound.access.IRemoteLevelAccessor;
import de.m_marvin.holostruct.levelbound.network.AddEntityPackage;
import de.m_marvin.holostruct.levelbound.network.GetBlockEntityPackage;
import de.m_marvin.holostruct.levelbound.network.GetBlockStatePackage;
import de.m_marvin.holostruct.levelbound.network.GetEntitiesPackage;
import de.m_marvin.holostruct.levelbound.network.ILevelboundPackage;
import de.m_marvin.holostruct.levelbound.network.SetBlockEntityPackage;
import de.m_marvin.holostruct.levelbound.network.SetBlockStatePackage;
import de.m_marvin.univec.impl.Vec3d;
import de.m_marvin.univec.impl.Vec3i;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ServerLevelAccessorImpl implements IRemoteLevelAccessor {

	private final Minecraft minecraft;
	private boolean allowCopyOperations;
	private boolean allowModifyOperations;
	private Int2ObjectMap<PendingPackage<?, ?>> pending = new Int2ObjectArrayMap<>();
	private int packageIdCounter = 0;
	
	public ServerLevelAccessorImpl(Minecraft minecraft, boolean allowCopy, boolean allowModify) {
		this.minecraft = minecraft;
		this.allowCopyOperations = allowCopy;
		this.allowModifyOperations = allowModify;
	}
	
	protected int nextPackageId() {
		return this.packageIdCounter++;
	}
	
	@SuppressWarnings("unchecked")
	public <P extends ILevelboundPackage<T>, T> void handleResponse(P pkg) {
		int taskId = pkg.getTaskId();
		PendingPackage<?, ?> pending = this.pending.get(taskId);
		if (pending != null && pending.isRightPackageType(pkg)) {
			if (((PendingPackage<P, T>) pending).accept(pkg)) {
				this.pending.remove(taskId);
			}
		}
		
		long now = System.currentTimeMillis();
		List<Integer> outdated = new ArrayList<>();
		for (Int2ObjectMap.Entry<PendingPackage<?, ?>> entry : this.pending.int2ObjectEntrySet()) {
			if (entry.getValue().isOutdated(now)) outdated.add(entry.getIntKey());
		}
		outdated.forEach(id -> this.pending.remove((int) id));
	}

	@Override
	public AccessLevel getAccessLevel() {
		return this.allowCopyOperations ? this.allowModifyOperations ? AccessLevel.FULL_SERVER : AccessLevel.COPY_SERVER : AccessLevel.READ_SERVER;
	}

	@Override
	public CompletableFuture<Boolean> setBlock(Vec3i position, BlockStateData state) {
		if (!allowModifyOperations)
			throw new AccessDeniedException("modify operation denied!");
		
		int taskId = nextPackageId();
		PendingPackage<SetBlockStatePackage, Boolean> pending = new PendingPackage<SetBlockStatePackage, Boolean>(new SetBlockStatePackage(taskId, position, state));
		this.pending.put(taskId, pending);
		return pending.startDispatch(System.currentTimeMillis(), this.minecraft.player.connection);
	}

	@Override
	public CompletableFuture<BlockStateData> getBlock(Vec3i position) {
		int taskId = nextPackageId();
		PendingPackage<GetBlockStatePackage, BlockStateData> pending = new PendingPackage<GetBlockStatePackage, BlockStateData>(new GetBlockStatePackage(taskId, position));
		this.pending.put(taskId, pending);
		return pending.startDispatch(System.currentTimeMillis(), this.minecraft.player.connection);
	}

	@Override
	public CompletableFuture<Boolean> setBlockEntity(Vec3i position, BlockEntityData blockEntity) {
		if (!allowModifyOperations)
			throw new AccessDeniedException("modify operation denied!");
		
		int taskId = nextPackageId();
		PendingPackage<SetBlockEntityPackage, Boolean> pending = new PendingPackage<SetBlockEntityPackage, Boolean>(new SetBlockEntityPackage(taskId, blockEntity));
		this.pending.put(taskId, pending);
		return pending.startDispatch(System.currentTimeMillis(), this.minecraft.player.connection);
	}

	@Override
	public CompletableFuture<BlockEntityData> getBlockEntity(Vec3i position) {
		int taskId = nextPackageId();
		PendingPackage<GetBlockEntityPackage, BlockEntityData> pending = new PendingPackage<GetBlockEntityPackage, BlockEntityData>(new GetBlockEntityPackage(taskId, position));
		this.pending.put(taskId, pending);
		return pending.startDispatch(System.currentTimeMillis(), this.minecraft.player.connection);
	}

	@Override
	public CompletableFuture<Boolean> addEntity(EntityData entity) {
		if (!allowModifyOperations)
			throw new AccessDeniedException("modify operation denied!");
		
		int taskId = nextPackageId();
		PendingPackage<AddEntityPackage, Boolean> pending = new PendingPackage<AddEntityPackage, Boolean>(new AddEntityPackage(taskId, entity));
		this.pending.put(taskId, pending);
		return pending.startDispatch(System.currentTimeMillis(), this.minecraft.player.connection);
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
		int taskId = nextPackageId();
		PendingPackage<GetEntitiesPackage, Collection<EntityData>> pending = new PendingPackage<GetEntitiesPackage, Collection<EntityData>>(new GetEntitiesPackage(taskId, new Vec3d(pos), new Vec3d(1, 1, 1).add(pos)));
		this.pending.put(taskId, pending);
		return pending.startDispatch(System.currentTimeMillis(), this.minecraft.player.connection);
	}

	@Override
	public CompletableFuture<Collection<EntityData>> getEntitiesWithin(Vec3i min, Vec3i max) {
		int taskId = nextPackageId();
		PendingPackage<GetEntitiesPackage, Collection<EntityData>> pending = new PendingPackage<GetEntitiesPackage, Collection<EntityData>>(new GetEntitiesPackage(taskId, new Vec3d(min), new Vec3d(max)));
		this.pending.put(taskId, pending);
		return pending.startDispatch(System.currentTimeMillis(), this.minecraft.player.connection);
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
