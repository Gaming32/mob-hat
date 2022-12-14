package io.github.gaming32.mobhat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MobHat implements ModInitializer {
    public static final Identifier STANDARD_ID = new Identifier("mob_hat", "mob_hat");

    public static final Item MOB_HAT_ITEM = new MobHatItem(
        new FabricItemSettings()
            .group(ItemGroup.MISC)
            .maxCount(1)
    );
    public static final MobHatCriterion MOB_HAT_CRITERION = new MobHatCriterion();

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, STANDARD_ID, MOB_HAT_ITEM);
        Criteria.register(MOB_HAT_CRITERION);
    }
}
