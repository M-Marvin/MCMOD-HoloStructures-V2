package de.m_marvin.holostruct.client.registries;

import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.commands.arguments.BlueprintArgument;
import de.m_marvin.holostruct.client.commands.arguments.BlueprintFormatArgument;
import de.m_marvin.holostruct.client.commands.arguments.BlueprintPathArgument;
import de.m_marvin.holostruct.client.commands.arguments.HologramArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CommandArguments {

	private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, HoloStruct.MODID);
	public static void register(IEventBus bus) {
		COMMAND_ARGUMENT_TYPES.register(bus);
	}
	
	public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<BlueprintPathArgument>> BLUEPRINT_PATH = COMMAND_ARGUMENT_TYPES.register("blueprint_path", () -> 
		ArgumentTypeInfos.registerByClass(BlueprintPathArgument.class, SingletonArgumentInfo.contextFree(BlueprintPathArgument::load)));

	public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<BlueprintFormatArgument>> BLUEPRINT_FORMAT = COMMAND_ARGUMENT_TYPES.register("blueprint_format", () -> 
		ArgumentTypeInfos.registerByClass(BlueprintFormatArgument.class, SingletonArgumentInfo.contextFree(BlueprintFormatArgument::format)));
	
	public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<BlueprintArgument>> BLUEPRINT_NAME = COMMAND_ARGUMENT_TYPES.register("blueprint_name", () -> 
		ArgumentTypeInfos.registerByClass(BlueprintArgument.class, SingletonArgumentInfo.contextFree(BlueprintArgument::blueprint)));
	
	public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<HologramArgument>> HOLOGRAM_NAME = COMMAND_ARGUMENT_TYPES.register("hologram_name", () -> 
		ArgumentTypeInfos.registerByClass(HologramArgument.class, SingletonArgumentInfo.contextFree(HologramArgument::hologram)));
	
}