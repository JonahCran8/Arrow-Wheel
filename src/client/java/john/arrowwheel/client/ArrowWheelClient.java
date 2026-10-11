package john.arrowwheel.client;

import com.mojang.blaze3d.platform.InputConstants;
import john.arrowwheel.ArrowWheel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix3x2fStack;
import java.util.ArrayList;
import java.util.List;
import static net.minecraft.client.KeyMapping.Category.INVENTORY;

public class ArrowWheelClient implements ClientModInitializer {

	private static boolean popupOpen = false;
	private static KeyMapping arrowWheel;
	private static final Identifier HOTBAR_TEXTURE =
			Identifier.withDefaultNamespace("textures/gui/sprites/hud/hotbar.png");
	private static final float SCALE = 2f / 3f;
	private static final int BOTTOM_MARGIN = 24;
	private static final int MAX_SLOTS = 9;

	// Create a list of held arrows
	private static List<ItemStack> collectArrows(Player player) {
		List<ItemStack> types = new ArrayList<>();
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty() || !stack.is(ItemTags.ARROWS)) continue;

			boolean merged = false;
			for (ItemStack existing : types) {
				if (ItemStack.isSameItemSameComponents(existing, stack)) {
					existing.grow(stack.getCount());
					merged = true;
					break;
				}
			}
			if (!merged) types.add(stack.copy());
		}
		return types;
	}

	@Override
	public void onInitializeClient() {
		System.out.println("ArrowWheelClient loaded");
		arrowWheel = KeyMappingHelper.registerKeyMapping(
				new KeyMapping(
						"key.arrowwheel.open_wheel",
						InputConstants.Type.KEYBOARD,
						InputConstants.KEY_R,
						INVENTORY
				)
		);

		// Check button press
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				popupOpen = false;
				return;
			}

			boolean holdingBow = client.player.getMainHandItem().is(Items.BOW);

			while (arrowWheel.consumeClick()) {
				if (holdingBow) {
					popupOpen = !popupOpen;
				}
			}

			if (!holdingBow) {
				popupOpen = false;
			}
		});

		HudElementRegistry.attachElementAfter(
				VanillaHudElements.HOTBAR, ArrowWheel.id("arrow_popup"),
				ArrowWheelClient::render);
	}

	private static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
		if (!popupOpen) return;
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null || !player.getMainHandItem().is(Items.BOW)) return;

		List<ItemStack> arrows = collectArrows(player);
		int slots = Math.min(arrows.size(), MAX_SLOTS);
		if (slots == 0) return;

		int screenW = client.getWindow().getGuiScaledWidth();
		int screenH = client.getWindow().getGuiScaledHeight();

		int barW = slots * 20 + 2;            // width of an N-slot hotbar, unscaled
		float scaledW = barW * SCALE;
		float scaledH = 22 * SCALE;

		// centre of the bow's slot in the vanilla hotbar
		float centerX = screenW / 2f - 80 + 20 * player.getInventory().getSelectedSlot();
		float x = Mth.clamp(centerX - scaledW / 2f, 2, screenW - scaledW - 2);
		float y = screenH - BOTTOM_MARGIN - scaledH;

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(x, y);
		pose.scale(SCALE, SCALE);

		// background: left part of the vanilla texture, then its 1px right edge
		graphics.blit(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE,
				0, 0, 0, 0, barW - 1, 22, barW - 1, 22, 182, 22);
		graphics.blit(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE,
				barW - 1, 0, 181, 0, 1, 22, 1, 22, 182, 22);

		for (int i = 0; i < slots; i++) {
			ItemStack stack = arrows.get(i);
			int itemX = 3 + i * 20;
			int itemY = 3;
			graphics.item(stack, itemX, itemY);

			int count = stack.getCount();
			if (count > 1) {
				String text = String.valueOf(count);
				int textX = itemX + 17 - client.font.width(text);
				int textY = itemY + 9;
				graphics.text(client.font, text, textX, textY, 0xFFFFFFFF, true);
			}
		}

		pose.popMatrix();
	}

}