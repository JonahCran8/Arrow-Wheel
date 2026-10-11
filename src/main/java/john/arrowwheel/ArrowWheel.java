package john.arrowwheel;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrowWheel implements ModInitializer {
	public static final String MOD_ID = "arrow-wheel";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModComponents.initialize();
		PayloadTypeRegistry.serverboundPlay().register(SelectAmmoPayload.TYPE, SelectAmmoPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SelectAmmoPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			ItemStack weapon = player.getMainHandItem();

			TagKey<Item> tag = AmmoGroups.findAmmoTag(player, weapon);
			if (tag == null) return;                       // not holding a wheel weapon: ignore

			Inventory inv = player.getInventory();
			for (int i = 0; i < inv.getContainerSize(); i++) {
				ItemStack s = inv.getItem(i);
				if (!s.isEmpty() && s.is(tag)
						&& ItemStack.isSameItemSameComponents(s, payload.ammo())) {
					weapon.set(ModComponents.SELECTED_AMMO, new SelectedAmmo(s.copyWithCount(1)));
					return;
				}
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
