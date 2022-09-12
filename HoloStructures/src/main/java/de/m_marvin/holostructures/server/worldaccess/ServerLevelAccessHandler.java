package de.m_marvin.holostructures.server.worldaccess;

import java.util.Optional;

import de.m_marvin.holostructures.client.blueprints.Blueprint.EntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.simple.SimpleChannel;

public class ServerLevelAccessHandler {
	
	public static void registerPackages(SimpleChannel network) {
		int id = 0;
		network.registerMessage(id++, WASetBlockState.class, WASetBlockState::encode, WASetBlockState::decode, null);
		network.registerMessage(id++, WASetBlockEntity.class, WASetBlockEntity::encode, WASetBlockEntity::decode, null);
		network.registerMessage(id++, WAGetBlockEntity.class, WAGetBlockEntity::encode, WAGetBlockEntity::decode, null);
		network.registerMessage(id++, WAAddEntity.class, WAAddEntity::encode, WAAddEntity::decode, null);		
	}
	
	public static interface RespondingMSG<T> {
		public Optional<T> getResponse();
	}
	
	public static class WASetBlockState implements RespondingMSG<Boolean> {
		protected BlockPos pos;
		protected BlockState state;
		protected boolean success;
		public WASetBlockState(BlockPos position, BlockState state) {
			this.pos = position;
			this.state = state;
		}
		public static void encode(WASetBlockState msg, FriendlyByteBuf buff) {
			buff.writeBlockPos(msg.pos);
			buff.writeWithCodec(BlockState.CODEC, msg.state);
			buff.writeBoolean(msg.success);
		}
		public static WASetBlockState decode(FriendlyByteBuf buff) {
			WASetBlockState msg = new WASetBlockState(buff.readBlockPos(), buff.readWithCodec(BlockState.CODEC));
			msg.success = buff.readBoolean();
			return msg;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		@Override
		public Optional<Boolean> getResponse() {
			return Optional.of(success);
		}
		@Override
		public int hashCode() {
			return 31 * pos.hashCode() * state.hashCode();
		}
	}

	public static class WASetBlockEntity implements RespondingMSG<Boolean> {
		protected BlockPos pos;
		protected EntityData data;
		protected boolean success;
		public WASetBlockEntity(BlockPos position, EntityData data) {
			this.pos = position;
			this.data = data;
		}
		public static void encode(WASetBlockEntity msg, FriendlyByteBuf buff) {
			buff.writeBlockPos(msg.pos);
			msg.data.writeBytes(buff);
			buff.writeBoolean(msg.success);
		}
		public static WASetBlockEntity decode(FriendlyByteBuf buff) {
			WASetBlockEntity msg = new WASetBlockEntity(buff.readBlockPos(), EntityData.readBytes(buff));
			msg.success = buff.readBoolean();
			return msg;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		@Override
		public Optional<Boolean> getResponse() {
			return Optional.of(success);
		}
		@Override
		public int hashCode() {
			return 31 * pos.hashCode() * data.hashCode();
		}
	}

	public static class WAGetBlockEntity implements RespondingMSG<CompoundTag> {
		protected BlockPos pos;
		protected CompoundTag data; 
		public WAGetBlockEntity(BlockPos position) {
			this.pos = position;
		}
		public static void encode(WAGetBlockEntity msg, FriendlyByteBuf buff) {
			buff.writeBlockPos(msg.pos);
			if (msg.data == null) {
				buff.writeNbt(new CompoundTag());
			} else {
				buff.writeNbt(msg.data);
			}
		}
		public static WAGetBlockEntity decode(FriendlyByteBuf buff) {
			WAGetBlockEntity msg = new WAGetBlockEntity(buff.readBlockPos());
			msg.data = buff.readNbt();
			return msg;
		}
		@Override
		public Optional<CompoundTag> getResponse() {
			return Optional.ofNullable(this.data);
		}
	}
	
	public static class WAAddEntity implements RespondingMSG<Boolean> {
		protected Vec3 pos;
		protected EntityData data;
		protected boolean success;
		public WAAddEntity(Vec3 pos, EntityData data) {
			this.pos = pos;
			this.data = data;
		}
		public static void encode(WAAddEntity msg, FriendlyByteBuf buff) {
			buff.writeDouble(msg.pos.x());
			buff.writeDouble(msg.pos.y());
			buff.writeDouble(msg.pos.z());
			msg.data.writeBytes(buff);
			buff.writeBoolean(msg.success);
		}
		public static WAAddEntity decode(FriendlyByteBuf buff) {
			EntityData entity = EntityData.readBytes(buff);
			Vec3 pos = new Vec3(buff.readDouble(), buff.readDouble(), buff.readDouble());
			WAAddEntity msg = new WAAddEntity(pos, entity);
			msg.success = buff.readBoolean();
			return msg;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		@Override
		public Optional<Boolean> getResponse() {
			return Optional.of(success);
		}
		@Override
		public int hashCode() {
			return 31 * pos.hashCode() * data.hashCode();
		}
	}

	
}
