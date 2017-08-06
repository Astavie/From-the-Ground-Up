package ftgumod.client.shader;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FramebufferTransparent extends Framebuffer {

	private final int factor;

	public FramebufferTransparent(int width, int height, int factor, boolean useDepthIn) {
		super(width * factor, height * factor, useDepthIn);
		this.factor = factor;
	}

	@Override
	public void framebufferRenderExt(int width, int height, boolean p_178038_3_) {
		framebufferRender(0, 0, 0, 0, width, height, p_178038_3_);
	}

	public void framebufferRender(int x, int y, int tX, int tY, int tWidth, int tHeight, boolean useDepthIn) {
		x *= factor;
		y *= factor;
		tX *= factor;
		tY *= factor;
		tWidth *= factor;
		tHeight *= factor;

		if (OpenGlHelper.isFramebufferEnabled()) {
			GlStateManager.colorMask(true, true, true, false);
			GlStateManager.disableDepth();
			GlStateManager.depthMask(false);
			GlStateManager.matrixMode(5889);
			GlStateManager.loadIdentity();
			GlStateManager.ortho(0.0D, tWidth, tHeight, 0.0D, 1000.0D, 3000.0D);
			GlStateManager.matrixMode(5888);
			GlStateManager.loadIdentity();
			GlStateManager.translate(0, 0, -2000.0F);
			GlStateManager.viewport(x, y, tWidth, tHeight);
			GlStateManager.enableTexture2D();
			GlStateManager.disableLighting();
			// GlStateManager.disableAlpha();

			if (useDepthIn) {
				GlStateManager.disableBlend();
				GlStateManager.enableColorMaterial();
			}

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.bindFramebufferTexture();
			double f = (double) tX / framebufferTextureWidth;
			double f1 = (double) tY / framebufferTextureHeight;
			double f2 = (double) tWidth / framebufferTextureWidth;
			double f3 = (double) tHeight / framebufferTextureHeight;
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			bufferbuilder.pos(0, tHeight, 0.0D).tex(f, f1).color(255, 255, 255, 255).endVertex();
			bufferbuilder.pos(tWidth, tHeight, 0.0D).tex(f2 + f, f1).color(255, 255, 255, 255).endVertex();
			bufferbuilder.pos(tWidth, 0, 0.0D).tex(f2 + f, f3 + f1).color(255, 255, 255, 255).endVertex();
			bufferbuilder.pos(0, 0, 0.0D).tex(f, f3 + f1).color(255, 255, 255, 255).endVertex();
			tessellator.draw();
			this.unbindFramebufferTexture();
			GlStateManager.depthMask(true);
			GlStateManager.colorMask(true, true, true, true);
		}
	}

}
