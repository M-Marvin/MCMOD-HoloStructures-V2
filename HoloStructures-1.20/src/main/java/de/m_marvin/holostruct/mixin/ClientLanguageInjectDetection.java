package de.m_marvin.holostruct.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.m_marvin.holostruct.client.event.ClientLanguageInjectEvent;
import net.minecraft.locale.Language;
import net.neoforged.neoforge.common.NeoForge;

@Mixin(Language.class)
public class ClientLanguageInjectDetection {
	
	@Inject(at = @At("RETURN"), method = "inject(Lnet/minecraft/locale/Language;)V")
	private static void inject(Language pInstance, CallbackInfo callback) {
		NeoForge.EVENT_BUS.post(new ClientLanguageInjectEvent(pInstance));
	}
	
}
