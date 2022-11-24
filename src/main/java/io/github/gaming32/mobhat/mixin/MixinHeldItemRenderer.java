package io.github.gaming32.mobhat.mixin;

import io.github.gaming32.mobhat.MobHat;
import io.github.gaming32.mobhat.MobHatItem;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {
    @Redirect(
        method = "renderFirstPersonItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z",
            ordinal = 1
        )
    )
    private boolean hatIsCrossbow(ItemStack self, Item other) {
        return self.isOf(other) ||
            (self.isOf(MobHat.MOB_HAT_ITEM) && MobHatItem.getHoldType(self) != MobHatItem.HoldType.EMPTY);
    }

    @Redirect(
        method = "renderFirstPersonItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/CrossbowItem;isCharged(Lnet/minecraft/item/ItemStack;)Z"
        )
    )
    private boolean hatIsCharged(ItemStack stack) {
        return stack.isOf(MobHat.MOB_HAT_ITEM) || CrossbowItem.isCharged(stack);
    }
}
