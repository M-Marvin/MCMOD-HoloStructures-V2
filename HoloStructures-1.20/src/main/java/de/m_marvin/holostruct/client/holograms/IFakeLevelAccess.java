package de.m_marvin.holostruct.client.holograms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;

/**
 * An interface used to access data required for rendering.
 * Implemented by holograms to be able to be passed to vanilla render functions.
 * @author Marvin Koehler
 *
 */
public interface IFakeLevelAccess extends LevelAccessor {
	
	/**
	 * Passed to render methods to redirect their requests for block and fluid states to other level instances
	 * @author Marvin Koehler
	 */
	public static class FakeLevelRedirected implements IFakeLevelAccess {
		
		protected IFakeLevelAccess level;
		protected Function<BlockPos, BlockPos> redirector;
		
		public FakeLevelRedirected(IFakeLevelAccess redirectedAccess, Function<BlockPos, BlockPos> posRedirector) {
			this.level = redirectedAccess;
			this.redirector = posRedirector;
		}
		
		@Override
		public BlockEntity getBlockEntity(BlockPos pPos) {
			return this.level.getBlockEntity(this.redirector.apply(pPos));
		}

		@Override
		public List<Entity> getEntitiesInBounds(AABB bounds) {
			return new ArrayList<>();
		}

		@Override
		public void setBlock(BlockPos pos, BlockState sate) {
			this.level.setBlock(this.redirector.apply(pos), sate);
		}

		@Override
		public BlockState getBlock(BlockPos pos) {
			return this.level.getBlock(this.redirector.apply(pos));
		}

		@Override
		public LevelAccessor getLevel() {
			return this.level;
		}
		
	}
	
	public List<Entity> getEntitiesInBounds(AABB bounds);
	public void setBlock(BlockPos pos, BlockState sate);
	public BlockState getBlock(BlockPos pos);
	public LevelAccessor getLevel();
	
	@Override
	public default List<Entity> getEntities(Entity pEntity, AABB pArea, Predicate<? super Entity> pPredicate) {
		return getEntitiesInBounds(pArea).stream().filter(pPredicate).toList();
	}

	@Override
	public default <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> pEntityTypeTest, AABB pBounds,
			Predicate<? super T> pPredicate) {
		return getEntitiesInBounds(pBounds).stream()
				.map(entity -> pEntityTypeTest.tryCast(entity))
				.filter(e -> e != null)
				.filter(pPredicate)
				.toList();
	}

	@Override
	public default List<? extends Player> players() {
		return getLevel().players();
	}

	@Override
	public default ChunkAccess getChunk(int pX, int pZ, ChunkStatus pRequiredStatus, boolean pNonnull) {
		return getLevel().getChunk(pX, pZ, pRequiredStatus, pNonnull);
	}

	@Override
	public default int getHeight(Types pHeightmapType, int pX, int pZ) {
		return getLevel().getHeight(pHeightmapType, pX, pZ);
	}

	@Override
	public default int getSkyDarken() {
		return getLevel().getSkyDarken();
	}

	@Override
	public default BiomeManager getBiomeManager() {
		return getLevel().getBiomeManager();
	}

	@Override
	public default Holder<Biome> getUncachedNoiseBiome(int pX, int pY, int pZ) {
		return getLevel().getUncachedNoiseBiome(pX, pY, pZ);
	}

	@Override
	public default boolean isClientSide() {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public default int getSeaLevel() {
		return getLevel().getSeaLevel();
	}

	@Override
	public default DimensionType dimensionType() {
		return getLevel().dimensionType();
	}

	@Override
	public default RegistryAccess registryAccess() {
		return getLevel().registryAccess();
	}

	@Override
	public default FeatureFlagSet enabledFeatures() {
		return getLevel().enabledFeatures();
	}

	@Override
	public default float getShade(Direction pDirection, boolean pShade) {
		return getLevel().getShade(pDirection, pShade);
	}

	@Override
	public default LevelLightEngine getLightEngine() {
		return getLevel().getLightEngine();
	}
	
	@Override
	public default BlockState getBlockState(BlockPos p_45571_) {
		return getBlock(p_45571_);
	}

	@Override
	public default FluidState getFluidState(BlockPos pPos) {
		return getBlockState(pPos).getFluidState();
	}

	@Override
	public default WorldBorder getWorldBorder() {
		return getLevel().getWorldBorder();
	}

	@Override
	public default boolean isStateAtPosition(BlockPos pPos, Predicate<BlockState> pState) {
		return pState.test(getBlockState(pPos));
	}

	@Override
	public default boolean isFluidAtPosition(BlockPos pPos, Predicate<FluidState> pPredicate) {
		return pPredicate.test(getFluidState(pPos));
	}

	@Override
	public default boolean setBlock(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft) {
		setBlock(pPos, pState);
		return true;
	}

	@Override
	public default boolean removeBlock(BlockPos pPos, boolean pIsMoving) {
		setBlock(pPos, Blocks.AIR.defaultBlockState());
		return true;
	}

	@Override
	public default boolean destroyBlock(BlockPos pPos, boolean pDropBlock, Entity pEntity, int pRecursionLeft) {
		setBlock(pPos, Blocks.AIR.defaultBlockState());
		return true;
	}

	@Override
	public default long nextSubTickCount() {
		return getLevel().nextSubTickCount();
	}

	@Override
	public default LevelTickAccess<Block> getBlockTicks() {
		return getLevel().getBlockTicks();
	}

	@Override
	public default LevelTickAccess<Fluid> getFluidTicks() {
		return getLevel().getFluidTicks();
	}

	@Override
	public default LevelData getLevelData() {
		return getLevel().getLevelData();
	}

	@Override
	public default DifficultyInstance getCurrentDifficultyAt(BlockPos pPos) {
		return getLevel().getCurrentDifficultyAt(pPos);
	}

	@Override
	public default MinecraftServer getServer() {
		return getLevel().getServer();
	}

	@Override
	public default ChunkSource getChunkSource() {
		return getLevel().getChunkSource();
	}

	@Override
	public default RandomSource getRandom() {
		return getLevel().getRandom();
	}

	@Override
	public default void playSound(Player pPlayer, BlockPos pPos, SoundEvent pSound, SoundSource pSource, float pVolume, float pPitch) {}

	@Override
	public default void addParticle(ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {}

	@Override
	public default void levelEvent(Player pPlayer, int pType, BlockPos pPos, int pData) {}

	@Override
	public default void gameEvent(GameEvent pEvent, Vec3 pPosition, Context pContext) {}

}
