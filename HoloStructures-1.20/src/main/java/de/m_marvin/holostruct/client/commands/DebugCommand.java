package de.m_marvin.holostruct.client.commands;

import java.io.File;

import org.openjdk.nashorn.internal.runtime.regexp.joni.Region;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import de.m_marvin.blueprints.api.RegistryName;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.levelbound.access.clientlevel.ClientCommandDispatcher;
import de.m_marvin.holostruct.client.levelbound.access.clientlevel.commanddispatcher.AddEntityCommand;
import de.m_marvin.holostruct.client.levelbound.access.clientlevel.commanddispatcher.Command;
import de.m_marvin.holostruct.client.rendering.posteffect.SelectivePostChain;
import de.m_marvin.univec.impl.Vec3d;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class DebugCommand {
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("debug_hs2")
		.then(
				Commands.literal("framedump")
				.then(
						Commands.argument("framebuffer", StringArgumentType.string())
						.then(
								Commands.argument("file", StringArgumentType.word())
								.executes(source ->
										dumpFrame(source, StringArgumentType.getString(source, "framebuffer"), StringArgumentType.getString(source, "file"))
								)	
						)
				)
		)
		.then(
				Commands.literal("loadshader")
				.then(
						Commands.argument("posteffect", StringArgumentType.string())
						.executes(source ->
								changePostEffect(source, StringArgumentType.getString(source, "posteffect"))
						)
				)
		)
		.then(
				Commands.literal("test")
				.executes(source ->
						commandDispatch(source)
				)
		));
	}
	
	public static int commandDispatch(CommandContext<CommandSourceStack> source) {
		
		try {
			
			Command<Boolean> cmd = new AddEntityCommand(new EntityData(new Vec3d(0, 100, 0), new RegistryName("minecraft:pig")));
			
			HoloStruct.CLIENT.COMMAND_DISPATCHER.startDispatch(cmd);
		} catch (Throwable e) {
			source.getSource().sendFailure(Component.literal("Exception was thrown: " + e.getMessage()));
		}
		
		return 0;
		
	}
	
	public static int changePostEffect(CommandContext<CommandSourceStack> source, String postEffectName) {
		
		try {
			ResourceLocation effect = new ResourceLocation(postEffectName);
			if (!HoloStruct.CLIENT.HOLORENDERER.loadPostEffect(effect)) {
				source.getSource().sendFailure(Component.literal("Failed to apply shader!"));
				return 0;
			}
			source.getSource().sendSuccess(() -> Component.literal("Applied shader"), false);
			return 1;
		} catch (Throwable e) {
			source.getSource().sendFailure(Component.literal("An exception was thrown!"));
			return 0;
		}
		
	}
	
	@SuppressWarnings("resource")
	public static int dumpFrame(CommandContext<CommandSourceStack> source, String framebufferName, String fileName) {
		
		if (!RenderSystem.isOnRenderThread()) {
			source.getSource().sendFailure(Component.literal("Not on render thread!"));
			return 0;
		}
		
		try {
			
			SelectivePostChain postChain = HoloStruct.CLIENT.HOLORENDERER.getActivePostEffect();
			if (postChain != null) {
				RenderTarget framebuffer = postChain.getTempTarget(framebufferName);
				if (framebuffer != null) {
					NativeImage frameimage = new NativeImage(framebuffer.width, framebuffer.height, false);
					RenderSystem.bindTexture(framebuffer.getColorTextureId());
					frameimage.downloadTexture(0, false);
					frameimage.flipY();
					
					File folder = new File(Minecraft.getInstance().gameDirectory, "framedump");
					folder.mkdir();
					frameimage.writeToFile(new File(folder, fileName + ".png"));
					
					frameimage.close();
					
					source.getSource().sendSuccess(() -> Component.literal("Saved frame to file"), false);
					return 1;
				}
			}
			
			source.getSource().sendFailure(Component.literal("Framebuffer not found!"));
			return 0;
		} catch (Throwable e) {
			source.getSource().sendFailure(Component.literal("An exception was thrown!"));
			e.printStackTrace();
			return 0;
		}
		
	}
	
}
