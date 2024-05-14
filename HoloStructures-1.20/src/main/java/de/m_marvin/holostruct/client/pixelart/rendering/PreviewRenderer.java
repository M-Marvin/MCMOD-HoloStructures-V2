package de.m_marvin.holostruct.client.pixelart.rendering;

import java.awt.image.BufferedImage;

import de.m_marvin.holostruct.HoloStruct;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = HoloStruct.MODID, bus = Bus.FORGE, value = Dist.CLIENT)
public class PreviewRenderer {
	
	public static final ResourceLocation PREVIEW_TEXTURE_LOC = new ResourceLocation("holostruct:map_preview");
	
	protected DynamicTexture previewTexture;
	protected BufferedImage previewImage;
	
	@SubscribeEvent
	public static void onRenderLevelLast(net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent.Chat event) {
		HoloStruct.CLIENT.PIXELART_PREVIEW.renderPreview(event.getGuiGraphics());
	}
	
	public void setPreviewImage(BufferedImage previewImage) {
		this.previewImage = previewImage;
		
		if (this.previewImage != null) {
			
			if (this.previewTexture == null) {
				this.previewTexture = new DynamicTexture(128, 128, false);
				Minecraft.getInstance().getTextureManager().register(PREVIEW_TEXTURE_LOC, previewTexture);
			}
			
			for (int x = 0; x < 128; x++) {
				for (int y = 0; y < 128; y++) {
					int rgb = this.previewImage.getRGB(x, y);
					int bgr = (rgb >> 16 & 0xFF) | ((rgb >> 8 & 0xFF) << 8) | ((rgb & 0xFF) << 16) | 0xFF000000;
					
					this.previewTexture.getPixels().setPixelRGBA(x, y, bgr);
				}
			}
			this.previewTexture.upload();
		}
	}
	
	public BufferedImage getPreviewImage() {
		return previewImage;
	}
	
	public void renderPreview(GuiGraphics graphics) {
		
		if (this.previewImage != null) {
			
			int posX = graphics.guiWidth() - 128 - 15;
			int posY = 5;
			
			graphics.fill(posX, posY, posX + 128 + 10, posY + 128 + 10, 0xB0202020);
			graphics.blit(PREVIEW_TEXTURE_LOC, posX + 5, posY + 5, 0, 0, 0, 128, 128, 128, 128);
		}
		
	}
	
}
