package john.arrowwheel;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record SelectAmmoPayload(ItemStack ammo) implements CustomPacketPayload {
    public static final Type<SelectAmmoPayload> TYPE =
            new Type<>(ArrowWheel.id("select_ammo"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectAmmoPayload> CODEC =
            StreamCodec.composite(ItemStack.OPTIONAL_STREAM_CODEC, SelectAmmoPayload::ammo,
                    SelectAmmoPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
