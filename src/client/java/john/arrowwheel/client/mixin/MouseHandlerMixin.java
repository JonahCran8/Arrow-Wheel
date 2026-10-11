package john.arrowwheel.client.mixin;

import john.arrowwheel.client.ArrowWheelClient;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void arrowwheel$onScroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
        if (ArrowWheelClient.handleScroll(yOffset)) {
            ci.cancel();   // stop vanilla from changing your hotbar slot
        }
    }
}
