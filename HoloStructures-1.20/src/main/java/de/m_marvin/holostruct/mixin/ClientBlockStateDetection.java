package de.m_marvin.holostruct.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.m_marvin.holostruct.client.event.ClientBlockEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;

@Mixin(ClientLevel.class)
public class ClientBlockStateDetection {
	
	@Inject(at = @At("RETURN"), method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z")
    private void setBlock(BlockPos pPos, BlockState pNewState, int pFlags, int recursionLeft, CallbackInfoReturnable<Boolean> callback) {
        NeoForge.EVENT_BUS.post(new ClientBlockEvent.ClientChangeEvent((ClientLevel) (Object) this, pPos, pNewState));
    }
	
	@Inject(at = @At("RETURN"), method = "setServerVerifiedBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)V")
	private void setServerVerifiedBlockState(BlockPos pPos, BlockState pState, int pFlags, CallbackInfo callback) {
        NeoForge.EVENT_BUS.post(new ClientBlockEvent.ServerChangeEvent((ClientLevel) (Object) this, pPos, pState));
	}
	
}
