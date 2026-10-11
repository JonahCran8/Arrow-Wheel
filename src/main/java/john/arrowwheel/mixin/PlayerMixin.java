package john.arrowwheel.mixin;

import john.arrowwheel.AmmoGroups;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "getProjectile", at = @At("HEAD"), cancellable = true)
    private void arrowwheel$getProjectile(ItemStack weapon, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack found = AmmoGroups.findSelectedAmmo((Player) (Object) this, weapon);
        if (found != null) {
            cir.setReturnValue(found);
        }
    }
}
