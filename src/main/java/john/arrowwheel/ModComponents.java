package john.arrowwheel;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModComponents {
    public static final DataComponentType<SelectedAmmo> SELECTED_AMMO = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ArrowWheel.id("selected_ammo"),
            DataComponentType.<SelectedAmmo>builder()
                    .persistent(SelectedAmmo.CODEC)
                    .networkSynchronized(SelectedAmmo.STREAM_CODEC)
                    .build()
    );

    public static void initialize() { }   // calling this forces the line above to run
}
