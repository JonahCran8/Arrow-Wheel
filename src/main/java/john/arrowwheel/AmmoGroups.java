package john.arrowwheel;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;


public class AmmoGroups {

    public static TagKey<Item> findAmmoTag(Player player, ItemStack weapon) {
        Registry<Item> items = player.registryAccess().lookupOrThrow(Registries.ITEM);
        for (TagKey<Item> tag : items.listTagIds().toList()) {
            Identifier id = tag.location();
            if (!id.getNamespace().equals(ArrowWheel.MOD_ID)) continue;
            if (!id.getPath().startsWith("weapons/")) continue;
            if (!weapon.is(tag)) continue;

            String group = id.getPath().substring("weapons/".length());
            return TagKey.create(Registries.ITEM, ArrowWheel.id("ammo/" + group));
        }
        return null;   // not a wheel weapon
    }

    public static List<ItemStack> collectAmmo(Player player, TagKey<Item> ammoTag) {
        List<ItemStack> types = new ArrayList<>();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty() || !stack.is(ammoTag)) continue;

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
}
