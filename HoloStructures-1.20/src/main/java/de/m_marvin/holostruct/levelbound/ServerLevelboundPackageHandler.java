package de.m_marvin.holostruct.levelbound;

import java.util.List;

import de.m_marvin.holostruct.client.blueprints.TypeConverter;
import de.m_marvin.holostruct.levelbound.network.AddEntityPackage;
import de.m_marvin.holostruct.levelbound.network.GetBlockEntityPackage;
import de.m_marvin.holostruct.levelbound.network.GetBlockStatePackage;
import de.m_marvin.holostruct.levelbound.network.GetEntitiesPackage;
import de.m_marvin.holostruct.levelbound.network.SetBlockEntityPackage;
import de.m_marvin.holostruct.levelbound.network.SetBlockStatePackage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class ServerLevelboundPackageHandler {
	
	public void handlerSetBlockstate(SetBlockStatePackage pkg, PlayPayloadContext context) {
		
		context.workHandler().submitAsync(() -> {
			BlockPos position = new BlockPos(pkg.getPosition().x, pkg.getPosition().y, pkg.getPosition().z);
			BlockState state = TypeConverter.data2blockState(pkg.getData());
			context.level().get().setBlock(position, state, 2);
			context.replyHandler().send(pkg.makeResponse(true));
		})
		.exceptionally(e -> {
			context.replyHandler().send(pkg.makeResponse(false));
			return null;
		});
		
	}

	public void handlerGetBlockState(GetBlockStatePackage pkg, PlayPayloadContext context) {
		
		context.workHandler().submitAsync(() -> {
			BlockPos position = new BlockPos(pkg.getPosition().x, pkg.getPosition().y, pkg.getPosition().z);
			BlockState state = context.level().get().getBlockState(position);
			context.replyHandler().send(pkg.makeResponse(TypeConverter.blockState2data(state)));
		})
		.exceptionally(e -> {
			context.replyHandler().send(pkg.makeResponse(null));
			return null;
		});
		
	}
	
	public void handlerSetBlockEntity(SetBlockEntityPackage pkg, PlayPayloadContext context) {
		
		context.workHandler().submitAsync(() -> {
			BlockPos position = new BlockPos(pkg.getData().getPosition().x, pkg.getData().getPosition().y, pkg.getData().getPosition().z);
			BlockState block = context.level().get().getBlockState(position);
			BlockEntity blockEntity = TypeConverter.data2blockEntity(block, pkg.getData());
			context.level().get().setBlockEntity(blockEntity);
			context.replyHandler().send(pkg.makeResponse(true));
		})
		.exceptionally(e -> {
			context.replyHandler().send(pkg.makeResponse(false));
			return null;
		});
		
	}

	public void handlerGetBlockEntity(GetBlockEntityPackage pkg, PlayPayloadContext context) {
		
		context.workHandler().submitAsync(() -> {
			BlockPos position = new BlockPos(pkg.getPosition().x, pkg.getPosition().y, pkg.getPosition().z);
			BlockEntity blockEntity = context.level().get().getBlockEntity(position);
			context.replyHandler().send(pkg.makeResponse(TypeConverter.blockEntity2data(blockEntity)));
		})
		.exceptionally(e -> {
			context.replyHandler().send(pkg.makeResponse(null));
			return null;
		});
		
	}
	
	public void handlerAddEntity(AddEntityPackage pkg, PlayPayloadContext context) {
		
		context.workHandler().submitAsync(() -> {
			Entity entity = TypeConverter.data2entity(pkg.getData());
			context.level().get().addFreshEntity(entity);
			context.replyHandler().send(pkg.makeResponse(true));
		})
		.exceptionally(e -> {
			context.replyHandler().send(pkg.makeResponse(false));
			return null;
		});
		
	}
	
	public void handlerGetEntities(GetEntitiesPackage pkg, PlayPayloadContext context) {
		
		context.workHandler().submitAsync(() -> {
			AABB aabb = new AABB(pkg.getMin().x, pkg.getMin().y, pkg.getMin().z, pkg.getMax().x, pkg.getMax().y, pkg.getMax().z);
			List<Entity> entities = context.level().get().getEntities(null, aabb);
			context.replyHandler().send(pkg.makeResponse(entities.stream().map(TypeConverter::entity2data).toList()));
		})
		.exceptionally(e -> {
			context.replyHandler().send(pkg.makeResponse(null));
			return null;
		});
		
	}
	
}
