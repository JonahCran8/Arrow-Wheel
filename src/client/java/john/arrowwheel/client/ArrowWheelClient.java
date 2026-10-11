package john.arrowwheel.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import static net.minecraft.client.KeyMapping.Category.INVENTORY;

public class ArrowWheelClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		KeyMapping arrowWheel = KeyMappingHelper.registerKeyMapping(
				new KeyMapping(
						"Open Arrow Wheel",
						InputConstants.Type.KEYBOARD,
						InputConstants.KEY_R,
						INVENTORY
				)
		);
	}
}