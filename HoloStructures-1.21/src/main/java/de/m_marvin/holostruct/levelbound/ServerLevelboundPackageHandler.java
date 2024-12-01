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
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Handles the levelbound packages received from the client
 * @author Marvin Koehler
 */
public class ServerLevelboundPackageHandler {
	
	public void handlerSetBlockstate(SetBlockStatePackage pkg, IPayloadContext context) {

		context.enqueueWork(() -> {
			BlockPos position = new BlockPos(pkg.getPosition().x, pkg.getPosition().y, pkg.getPosition().z);
			BlockState state = TypeConverter.data2blockState(pkg.getData());
			context.player().level().setBlock(position, state, 2);
			context.reply(pkg.makeResponse(true));
		});
		
	}

	public void handlerGetBlockState(GetBlockStatePackage pkg, IPayloadContext context) {
		
		context.enqueueWork(() -> {
			BlockPos position = new BlockPos(pkg.getPosition().x, pkg.getPosition().y, pkg.getPosition().z);
			BlockState state = context.player().level().getBlockState(position);
			context.reply(pkg.makeResponse(TypeConverter.blockState2data(state)));
		});
		
	}
	
	public void handlerSetBlockEntity(SetBlockEntityPackage pkg, IPayloadContext context) {

		context.enqueueWork(() -> {
			BlockPos position = new BlockPos(pkg.getData().getPosition().x, pkg.getData().getPosition().y, pkg.getData().getPosition().z);
			BlockState block = context.player().level().getBlockState(position);
			BlockEntity blockEntity = TypeConverter.data2blockEntity(block, pkg.getData());
			context.player().level().setBlockEntity(blockEntity);
			context.reply(pkg.makeResponse(true));
		});
		
	}

	public void handlerGetBlockEntity(GetBlockEntityPackage pkg, IPayloadContext context) {

		context.enqueueWork(() -> {
			BlockPos position = new BlockPos(pkg.getPosition().x, pkg.getPosition().y, pkg.getPosition().z);
			BlockEntity blockEntity = context.player().level().getBlockEntity(position);
			context.reply(pkg.makeResponse(TypeConverter.blockEntity2data(blockEntity)));
		});
		
	}
	
	public void handlerAddEntity(AddEntityPackage pkg, IPayloadContext context) {

		context.enqueueWork(() -> {
			Entity entity = TypeConverter.data2entity(pkg.getData());
			context.player().level().addFreshEntity(entity);
			context.reply(pkg.makeResponse(true));
		});
		
	}
	
	public void handlerGetEntities(GetEntitiesPackage pkg, IPayloadContext context) {

		context.enqueueWork(() -> {
			AABB aabb = new AABB(pkg.getMin().x, pkg.getMin().y, pkg.getMin().z, pkg.getMax().x, pkg.getMax().y, pkg.getMax().z);
			List<Entity> entities = context.player().level().getEntities(null, aabb);
			context.reply(pkg.makeResponse(entities.stream().map(TypeConverter::entity2data).toList()));
		});
		
	}
	
}
