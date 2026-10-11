package john.arrowwheel.client;

import com.mojang.blaze3d.platform.InputConstants;
import john.arrowwheel.AmmoGroups;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import java.util.List;
import static net.minecraft.client.KeyMapping.Category.INVENTORY;

public class ArrowWheelClient implements ClientModInitializer {

	private static boolean popupOpen = false;
	private static KeyMapping arrowWheel;
	private static int selectedIndex = 0;
	private static final Identifier SELECTION_TEXTURE =
			Identifier.withDefaultNamespace("textures/gui/sprites/hud/hotbar_selection.png");
	private static final Identifier HOTBAR_TEXTURE =
			Identifier.withDefaultNamespace("textures/gui/sprites/hud/hotbar.png");
	private static final float SCALE = 2f / 3f;
	private static final int BOTTOM_MARGIN = 24;
	private static final int MAX_SLOTS = 9;

	// The ammo tag for whatever is in the main hand, or null if it isn't a wheel weapon
	private static TagKey<Item> currentAmmoTag(Minecraft client) {
		if (client.player == null) return null;
		return AmmoGroups.findAmmoTag(client.player, client.player.getMainHandItem());
	}

	public static boolean handleScroll(double yOffset) {
		Minecraft client = Minecraft.getInstance();
		if (!popupOpen || client.gui.screen() != null || client.player == null) return false;

		TagKey<Item> ammoTag = currentAmmoTag(client);
		if (ammoTag == null) return false;

		int count = Math.min(AmmoGroups.collectAmmo(client.player, ammoTag).size(), MAX_SLOTS);
		if (count == 0) return false;

		int dir = yOffset > 0 ? -1 : 1;
		selectedIndex = Math.floorMod(selectedIndex + dir, count);
		return true;
	}

	@Override
	public void onInitializeClient() {
		arrowWheel = KeyMappingHelper.registerKeyMapping(
				new KeyMapping(
						"key.arrowwheel.open_wheel",
						InputConstants.Type.KEYBOARD,
						InputConstants.KEY_R,
						INVENTORY
				)
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				popupOpen = false;
				return;
			}

			boolean holdingWeapon = currentAmmoTag(client) != null;

			while (arrowWheel.consumeClick()) {
				if (holdingWeapon) {
					popupOpen = !popupOpen;
				}
			}

			if (!holdingWeapon) {
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
		if (player == null) return;

		TagKey<Item> ammoTag = AmmoGroups.findAmmoTag(player, player.getMainHandItem());
		if (ammoTag == null) return;   // not holding a wheel weapon

		List<ItemStack> ammo = AmmoGroups.collectAmmo(player, ammoTag);
		int slots = Math.min(ammo.size(), MAX_SLOTS);
		if (slots == 0) return;

		int screenW = client.getWindow().getGuiScaledWidth();
		int screenH = client.getWindow().getGuiScaledHeight();

		int barW = slots * 20 + 2;
		float scaledW = barW * SCALE;
		float scaledH = 22 * SCALE;

		// centre of the weapon's slot in the vanilla hotbar
		float centerX = screenW / 2f - 80 + 20 * player.getInventory().getSelectedSlot();
		float x = Mth.clamp(centerX - scaledW / 2f, 2, screenW - scaledW - 2);
		float y = screenH - BOTTOM_MARGIN - scaledH;

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(x, y);
		pose.scale(SCALE, SCALE);

		graphics.blit(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE,
				0, 0, 0, 0, barW - 1, 22, barW - 1, 22, 182, 22);
		graphics.blit(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE,
				barW - 1, 0, 181, 0, 1, 22, 1, 22, 182, 22);

		selectedIndex = Math.min(selectedIndex, slots - 1);

		graphics.blit(RenderPipelines.GUI_TEXTURED, SELECTION_TEXTURE,
				-1 + 20 * selectedIndex, -1, 0, 0, 24, 24, 24, 24, 24, 24);

		for (int i = 0; i < slots; i++) {
			ItemStack stack = ammo.get(i);
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

		ItemStack selected = ammo.get(selectedIndex);
		Component name = selected.getHoverName();
		int textW = client.font.width(name);

		float slotCenterX = x + (11 + 20 * selectedIndex) * SCALE;
		int textX = Mth.clamp((int) (slotCenterX - textW / 2f), 2, screenW - textW - 2);
		int textY = (int) y - 12;
		graphics.text(client.font, name, textX, textY, 0xFFFFFFFF, true);
	}

}