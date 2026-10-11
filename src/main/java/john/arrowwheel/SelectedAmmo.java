package john.arrowwheel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record SelectedAmmo(ItemStack ammo) {
    public static final Codec<SelectedAmmo> CODEC = RecordCodecBuilder.create(i -> i.group(
            ItemStack.CODEC.fieldOf("ammo").forGetter(SelectedAmmo::ammo)
    ).apply(i, SelectedAmmo::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectedAmmo> STREAM_CODEC =
            StreamCodec.composite(ItemStack.STREAM_CODEC, SelectedAmmo::ammo, SelectedAmmo::new);

    // ItemStack compares by identity, so compare by contents instead
    @Override public boolean equals(Object o) {
        return o instanceof SelectedAmmo other && ItemStack.matches(ammo, other.ammo);
    }
    @Override public int hashCode() { return ItemStack.hashItemAndComponents(ammo); }
}
