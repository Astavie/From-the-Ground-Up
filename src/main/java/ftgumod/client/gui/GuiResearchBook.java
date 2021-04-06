package ftgumod.client.gui;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ftgumod.Content;
import ftgumod.FTGU;
import ftgumod.FTGUConfig;
import ftgumod.api.technology.ITechnology;
import ftgumod.api.technology.unlock.IUnlock;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.server.CopyTechMessage;
import ftgumod.proxy.ProxyClient;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import ftgumod.util.StackUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public class GuiResearchBook extends GuiScreen {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation ACHIEVEMENT_BACKGROUND = new ResourceLocation(FTGU.MODID,
			"textures/gui/achievement/achievement_background.png");
	private static final ResourceLocation STAINED_CLAY = new ResourceLocation(
			"textures/blocks/hardened_clay_stained_cyan.png");
	private static final ResourceLocation RECIPE_BOOK = new ResourceLocation("textures/gui/recipe_book.png");
	public static Map<ResourceLocation, Float> zoom;
	public static Map<ResourceLocation, Double> xScrollO;
	public static Map<ResourceLocation, Double> yScrollO;
	private static boolean state = true;
	private static Technology root;
	private static Technology selected;
	private static int scroll = 1;
	private final EntityPlayer player;
	private final int num = 4;

	private int x_min;
	private int y_min;
	private int x_max;
	private int y_max;
	private final int imageWidth;
	private final int imageHeight;
	private double xScrollP;
	private double yScrollP;
	private double xScrollTarget;
	private double yScrollTarget;
	private int scrolling;
	private double xLastScroll;
	private double yLastScroll;
	private int pages;

	public GuiResearchBook(EntityPlayer player) {
		this.player = player;

		imageWidth = 256;
		imageHeight = 202;

		if (root == null || !TechnologyManager.INSTANCE.contains(root) || !root.canResearchIgnoreResearched(player)) {
			for (Technology technology : TechnologyManager.INSTANCE.getRoots()) {
				if (technology.canResearchIgnoreResearched(player)) {
					root = technology;
					break;
				}
			}
		}
	}

	@Override
	public void initGui() {
		if (selected == null || !TechnologyManager.INSTANCE.contains(selected) || !selected.isResearched(player)) {
			selected = null;
			state = true;
		}

		xScrollP = xScrollTarget = xScrollO.get(root.getRegistryName());
		yScrollP = yScrollTarget = yScrollO.get(root.getRegistryName());

		buttonList.clear();
		if (state) {
			Set<Technology> tree = new HashSet<>();
			root.getChildren(tree, true);

			x_min = (int) root.getDisplayInfo().getX();
			y_min = (int) root.getDisplayInfo().getY();
			x_max = (int) root.getDisplayInfo().getX();
			y_max = (int) root.getDisplayInfo().getY();

			for (Technology technology : tree) {
				if (technology.getDisplayInfo().getX() < x_min)
					x_min = (int) technology.getDisplayInfo().getX();
				else if (technology.getDisplayInfo().getX() > x_max)
					x_max = (int) technology.getDisplayInfo().getX();
				if (technology.getDisplayInfo().getY() < y_min)
					y_min = (int) technology.getDisplayInfo().getY();
				else if (technology.getDisplayInfo().getY() > y_max)
					y_max = (int) technology.getDisplayInfo().getY();
			}

			x_min = x_min * 24 - 112;
			y_min = y_min * 24 - 112;
			x_max = x_max * 24 - 77;
			y_max = y_max * 24 - 77;

			GuiButton page = new GuiButton(2, (width - imageWidth) / 2 + 24, height / 2 + 74, 125, 20,
					root.getDisplayInfo().getTitle().getFormattedText());
			if (TechnologyManager.INSTANCE.getRoots().stream().filter(t -> t.canResearchIgnoreResearched(player))
					.count() < 2)
				page.enabled = false;

			buttonList.add(new GuiButton(1, width / 2 + 24, height / 2 + 74, 80, 20, I18n.format("gui.done")));
			buttonList.add(page);

			scroll = 1;
		} else {
			buttonList.add(new GuiButton(1, width / 2 + 24, height / 2 + 74, 80, 20, I18n.format("gui.done")));
			if (FTGUConfig.allowResearchCopy && selected.canCopy()) {
				GuiButton copy = new GuiButton(2, (width - imageWidth) / 2 + 24, height / 2 + 74, 125, 20,
						I18n.format("gui.copy"));
				copy.enabled = false;
				for (int i = 0; i < player.inventory.getSizeInventory(); i++)
					if (!player.inventory.getStackInSlot(i).isEmpty()
							&& player.inventory.getStackInSlot(i).getItem() == Content.i_parchmentEmpty) {
						copy.enabled = true;
						break;
					}
				buttonList.add(copy);
			}

			pages = (int) Math.max(
					Math.ceil(((double) selected.getUnlock().stream().filter(IUnlock::isDisplayed).count()) / num), 1);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 1) {
			if (state) {
				mc.displayGuiScreen(null);
				mc.setIngameFocus();
			} else {
				state = true;
				initGui();
			}
		} else if (button.id == 2) {
			if (state) {
				Technology first = null;
				boolean next = false;
				for (Technology technology : TechnologyManager.INSTANCE.getRoots()) {
					if (technology.canResearchIgnoreResearched(player)) {
						if (next) {
							next = false;
							root = technology;
							break;
						}
						if (first == null)
							first = technology;
					}
					if (technology == root)
						next = true;
				}
				if (next)
					root = first;
				initGui();
			} else {
				PacketDispatcher.sendToServer(new CopyTechMessage(selected));
			}
		}
	}

	@Override
	protected void keyTyped(char key, int id) throws IOException {
		if (mc.gameSettings.keyBindInventory.isActiveAndMatches(id) || ProxyClient.key.isActiveAndMatches(id)) {
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		} else {
			super.keyTyped(key, id);
		}
	}

	@Override
	public void drawScreen(int x, int y, float z) {
		if (state) {
			if (Mouse.isButtonDown(0)) {
				int i = (width - imageWidth) / 2;
				int j = (height - imageHeight) / 2;
				int k = i + 8;
				int l = j + 17;

				if ((scrolling == 0 || scrolling == 1) && x >= k && x < k + 224 && y >= l && y < l + 155) {
					if (scrolling == 0) {
						scrolling = 1;
					} else {
						xScrollP -= (float) (x - xLastScroll) * zoom.get(root.getRegistryName());
						yScrollP -= (float) (y - yLastScroll) * zoom.get(root.getRegistryName());

						xScrollTarget = xScrollP;
						yScrollTarget = yScrollP;

						xScrollO.put(root.getRegistryName(), xScrollP);
						yScrollO.put(root.getRegistryName(), yScrollP);
					}
					xLastScroll = x;
					yLastScroll = y;
				}
			} else {
				scrolling = 0;
			}

			int i1 = Mouse.getDWheel();
			float f3 = zoom.get(root.getRegistryName());
			zoom.put(root.getRegistryName(),
					MathHelper.clamp(i1 < 0 ? f3 + 0.25F : i1 > 0 ? f3 - 0.25F : f3, 1.0F, 2.0F));

			if (zoom.get(root.getRegistryName()) != f3) {
				float f4 = f3 * imageWidth;
				float f = f3 * imageHeight;
				float f1 = zoom.get(root.getRegistryName()) * imageWidth;
				float f2 = zoom.get(root.getRegistryName()) * imageHeight;

				xScrollP -= (f1 - f4) * 0.5F;
				yScrollP -= (f2 - f) * 0.5F;

				xScrollTarget = xScrollP;
				yScrollTarget = yScrollP;

				xScrollO.put(root.getRegistryName(), xScrollP);
				yScrollO.put(root.getRegistryName(), yScrollP);
			}

			if (xScrollTarget < x_min)
				xScrollTarget = x_min;
			if (yScrollTarget < y_min)
				yScrollTarget = y_min;
			if (xScrollTarget >= x_max)
				xScrollTarget = x_max - 1;
			if (yScrollTarget >= y_max)
				yScrollTarget = y_max - 1;
		}

		drawDefaultBackground();
		drawResearchScreen(x, y, z);

		GlStateManager.disableLighting();
		GlStateManager.disableDepth();

		drawTitle();

		GlStateManager.enableLighting();
		GlStateManager.enableDepth();
	}

	@Override
	public void mouseClicked(int x, int y, int b) throws IOException {
		if (b == 0 && selected != null && selected.isResearched(player)) {
			state = false;
			initGui();
		}
		super.mouseClicked(x, y, b);
	}

	@Override
	public void updateScreen() {
		xScrollO.put(root.getRegistryName(), xScrollP);
		yScrollO.put(root.getRegistryName(), yScrollP);
		double d0 = xScrollTarget - xScrollP;
		double d1 = yScrollTarget - yScrollP;
		if (d0 * d0 + d1 * d1 < 4D) {
			xScrollP += d0;
			yScrollP += d1;
		} else {
			xScrollP += d0 * 0.85D;
			yScrollP += d1 * 0.85D;
		}
	}

	private void drawTitle() {
		int i = (width - imageWidth) / 2;
		int j = (height - imageHeight) / 2;
		fontRenderer.drawString(I18n.format("item.research_book.name"), i + 15, j + 5, 0x404040);
	}

	private void drawResearchScreen(int x, int y, float z) {
		int k = (width - imageWidth) / 2;
		int l = (height - imageHeight) / 2;
		int i1 = k + 16;
		int j1 = l + 17;

		GlStateManager.depthFunc(518);
		GlStateManager.pushMatrix();
		GlStateManager.translate(i1, j1, -200F);
		GlStateManager.enableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableColorMaterial();

		for (int l3 = 0; l3 < 10; l3++) {
			for (int i4 = 0; i4 < 14; i4++) {
				if (root.getDisplayInfo().getBackground() == null)
					mc.getTextureManager().bindTexture(STAINED_CLAY);
				else
					mc.getTextureManager().bindTexture(root.getDisplayInfo().getBackground());
				drawModalRectWithCustomSizedTexture(i4 * 16, l3 * 16, 0, 0, 16, 16, 16, 16);
			}
		}
		mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);

		GlStateManager.enableDepth();
		GlStateManager.depthFunc(515);
		if (state) {
			GlStateManager.scale(1.0F / zoom.get(root.getRegistryName()), 1.0F / zoom.get(root.getRegistryName()),
					1.0F);

			int i = MathHelper.floor(
					xScrollO.get(root.getRegistryName()) + (xScrollP - xScrollO.get(root.getRegistryName())) * z);
			int j = MathHelper.floor(
					yScrollO.get(root.getRegistryName()) + (yScrollP - yScrollO.get(root.getRegistryName())) * z);

			if (i < x_min)
				i = x_min;
			if (j < y_min)
				j = y_min;
			if (i >= x_max)
				i = x_max - 1;
			if (j >= y_max)
				j = y_max - 1;

			Set<Technology> tech = new HashSet<>();
			root.getChildren(tech, true);

			if (tech != null) {
				try {
					for (Technology t1 : tech) {
						if (!t1.canResearchIgnoreResearched(player))
							continue;
						if (!t1.isResearched(player) && !t1.isUnlocked(player))
							continue;
						if (t1.getDisplayInfo().isHidden() && !t1.isResearched(player))
							continue;
						if (t1.getParent() == null || !tech.contains(t1.getParent()))
							continue;
						int xStart = (int) ((t1.getDisplayInfo().getX() * 24 - i) + 11);
						int yStart = (int) ((t1.getDisplayInfo().getY() * 24 - j) + 11);
						int xStop = (int) ((t1.getParent().getDisplayInfo().getX() * 24 - i) + 11);
						int yStop = (int) ((t1.getParent().getDisplayInfo().getY() * 24 - j) + 11);

						boolean flag = t1.isResearched(player);

						int l4;
						if (flag)
							l4 = 0xffa0a0a0;
						else
							l4 = 0xff00ff00;

						drawHorizontalLine(xStart, xStop, yStart, l4);
						drawVerticalLine(xStop, yStart, yStop, l4);

						if (xStart > xStop)
							drawTexturedModalRect(xStart - 11 - 7, yStart - 5, 114, 234, 7, 11);
						else if (xStart < xStop)
							drawTexturedModalRect(xStart + 11, yStart - 5, 107, 234, 7, 11);
						else if (yStart > yStop)
							drawTexturedModalRect(xStart - 5, yStart - 11 - 7, 96, 234, 11, 7);
						else if (yStart < yStop)
							drawTexturedModalRect(xStart - 5, yStart + 11, 96, 241, 11, 7);
					}

					selected = null;

					float f3 = (x - i1) * zoom.get(root.getRegistryName());
					float f4 = (y - j1) * zoom.get(root.getRegistryName());

					RenderHelper.enableGUIStandardItemLighting();
					GlStateManager.disableLighting();
					GlStateManager.enableRescaleNormal();
					GlStateManager.enableColorMaterial();

					for (Technology t2 : tech) {
						if (!t2.canResearchIgnoreResearched(player))
							continue;
						if (!t2.isResearched(player) && !t2.isUnlocked(player))
							continue;
						if (t2.getDisplayInfo().isHidden() && !t2.isResearched(player))
							continue;
						int l6 = (int) (t2.getDisplayInfo().getX() * 24 - i);
						int j7 = (int) (t2.getDisplayInfo().getY() * 24 - j);
						if (l6 < -24 || j7 < -24 || l6 > 224F * zoom.get(root.getRegistryName())
								|| j7 > 155F * zoom.get(root.getRegistryName()))
							continue;

						if (t2.isResearched(player))
							GlStateManager.color(0.75F, 0.75F, 0.75F, 1.0F);
						else
							GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

						mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);
						GlStateManager.enableBlend();
						if (t2.hasCustomUnlock())
							drawTexturedModalRect(l6 - 2, j7 - 2, 26, 202, 26, 26);
						else
							drawTexturedModalRect(l6 - 2, j7 - 2, 0, 202, 26, 26);
						GlStateManager.disableBlend();

						GlStateManager.disableLighting();
						GlStateManager.enableCull();
						itemRender.renderItemAndEffectIntoGUI(t2.getDisplayInfo().getIcon(), l6 + 3, j7 + 3);
						GlStateManager.blendFunc(net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA,
								net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
						GlStateManager.disableLighting();

						GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
						if (f3 >= l6 && f3 <= l6 + 22 && f4 >= j7 && f4 <= j7 + 22
								&& t2.canResearchIgnoreResearched(player))
							selected = t2;
					}
				} catch (ConcurrentModificationException e) {
					LOGGER.debug("Prevented ConcurrentModificationException while rendering GuiResearchBook");
				}
			}
		} else {
			int wheel = Mouse.getDWheel();
			if (wheel < 0)
				scroll = Math.min(scroll + 1, pages);
			if (wheel > 0)
				scroll = Math.max(scroll - 1, 1);

			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.enableRescaleNormal();
			GlStateManager.enableColorMaterial();
			List<IUnlock> display = selected.getUnlock().stream().filter(IUnlock::isDisplayed)
					.collect(Collectors.toList());
			for (int pos = 0; pos < num; pos++) {
				int n = pos + (num * (scroll - 1));
				if (n >= display.size())
					break;

				ItemStack[] list = display.get(n).getIcon().getMatchingStacks();

				NonNullList[] sub = new NonNullList[list.length];
				int length = 0;

				for (int q = 0; q < list.length; q++) {
					if (list[q].getItemDamage() == OreDictionary.WILDCARD_VALUE) {
						sub[q] = NonNullList.create();

						// noinspection unchecked
						list[q].getItem().getSubItems(list[q].getItem().getCreativeTab(), sub[q]);
					} else
						sub[q] = NonNullList.from(null, list[q]);

					length += sub[q].size();
				}

				list = new ItemStack[length];
				int pp = 0;
				for (NonNullList nonNullList : sub)
					// noinspection unchecked
					for (ItemStack stack : (NonNullList<ItemStack>) nonNullList)
						list[pp++] = stack;

				long tick = mc.world.getTotalWorldTime() / 30;
				int index = (int) (tick % list.length);

				ItemStack item = list[index];

				mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);
				GlStateManager.enableBlend();
				drawTexturedModalRect(6, 37 + (pos * 28), 0, 202, 26, 26);
				GlStateManager.disableBlend();

				GlStateManager.disableLighting();
				GlStateManager.enableCull();
				itemRender.renderItemAndEffectIntoGUI(item, 11, 42 + (pos * 28));
				GlStateManager.blendFunc(net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA,
						net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				GlStateManager.disableLighting();

				fontRenderer.drawStringWithShadow(item.getDisplayName(), 35, 45 + (pos * 28), 0xFFFFFF);

				if (x >= i1 + 6 && x < i1 + 32 && y >= j1 + 37 + (pos * 28) && y < j1 + 63 + (pos * 28)) {
					int r = 0;
					for (IRecipe recipe : ForgeRegistries.RECIPES) {
						if (StackUtils.INSTANCE.isStackOf(item, recipe.getRecipeOutput())) {
							mc.getTextureManager().bindTexture(RECIPE_BOOK);

							int xp = 31 + (r * 25);
							int yp = 38 + (pos * 28);
							drawTexturedModalRect(xp, yp, 152, 78, 24, 24);

							int width = 3;
							int height = 3;

							if (recipe instanceof IShapedRecipe) {
								IShapedRecipe shaped = (IShapedRecipe) recipe;
								width = shaped.getRecipeWidth();
								height = shaped.getRecipeHeight();
							}

							Iterator<Ingredient> iterator = recipe.getIngredients().iterator();

							outer: for (int i = 0; i < height; ++i) {
								int kk = 3 + i * 7;

								for (int j = 0; j < width; ++j) {
									if (iterator.hasNext()) {
										ItemStack[] stack = (iterator.next()).getMatchingStacks();

										if (stack.length != 0) {
											int l1 = 3 + j * 7;
											GlStateManager.pushMatrix();
											int i2 = (int) ((float) (xp + l1) / 0.42F - 3.0F);
											int j2 = (int) ((float) (yp + kk) / 0.42F - 3.0F);
											GlStateManager.scale(0.42F, 0.42F, 1.0F);
											GlStateManager.enableLighting();
											mc.getRenderItem().renderItemAndEffectIntoGUI(
													stack[(int) (tick % stack.length)], i2, j2);
											GlStateManager.disableLighting();
											GlStateManager.popMatrix();
										}
									} else
										break outer;
								}
							}
							r++;
						}
					}
				}
			}
		}

		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GlStateManager.popMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);
		drawTexturedModalRect(k, l, 0, 0, imageWidth, imageHeight);
		zLevel = 0.0F;
		GlStateManager.depthFunc(515);
		GlStateManager.disableDepth();
		GlStateManager.enableTexture2D();

		super.drawScreen(x, y, z);
		if (selected != null) {
			if (state) {
				String s = selected.getDisplayInfo().getTitle().getFormattedText();
				String s1 = selected.getDisplayInfo().getDescription().getFormattedText();

				int children = 0;
				for (ITechnology child : selected.getChildren())
					if (child.isRoot())
						children++;

				int i7 = x + 12;
				int k7 = y - 4;

				int j8 = Math.max(fontRenderer.getStringWidth(s), 120);
				int i9 = fontRenderer.getWordWrappedHeight(s1, j8);
				if (selected.isResearched(player) || children > 0)
					i9 += 12;

				drawGradientRect(i7 - 3, k7 - 3, i7 + j8 + 3, k7 + i9 + 3 + 12, 0xc0000000, 0xc0000000);
				fontRenderer.drawSplitString(s1, i7, k7 + 12, j8, 0xffa0a0a0);
				if (selected.isResearched(player))
					fontRenderer.drawStringWithShadow(I18n.format("technology.researched"), i7, k7 + i9 + 4,
							0xff9090ff);
				else if (children > 0)
					fontRenderer.drawStringWithShadow(I18n.format(children == 1 ? "technology.tab" : "technology.tabs"),
							i7, k7 + i9 + 4, 0xffff5555);
				fontRenderer.drawStringWithShadow(s, i7, k7, -1);
			} else {
				String s1 = selected.getDisplayInfo().getTitle().getFormattedText();
				int x1 = (width - fontRenderer.getStringWidth(s1)) / 2;
				fontRenderer.drawStringWithShadow(s1, x1, l + 22, 0xffffff);

				String s2 = selected.getDisplayInfo().getDescription().getFormattedText();
				int x2 = width / 2;
				int y2 = l + 32;

				for (String s : fontRenderer.listFormattedStringToWidth(s2, 211)) {
					fontRenderer.drawStringWithShadow(s, x2 - (fontRenderer.getStringWidth(s) / 2), y2, 0xffa0a0a0);
					y2 += fontRenderer.FONT_HEIGHT;
				}

				String s3 = scroll + "/" + pages;
				int x3 = (width + imageWidth) / 2 - fontRenderer.getStringWidth(s3);
				int y3 = (height + imageHeight) / 2;
				fontRenderer.drawStringWithShadow(s3, x3 - 21, y3 - 44, 0xffa0a0a0);
			}
		}

		GlStateManager.enableDepth();
		GlStateManager.enableLighting();
		RenderHelper.disableStandardItemLighting();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

}
