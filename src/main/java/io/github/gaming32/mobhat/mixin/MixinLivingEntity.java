package io.github.gaming32.mobhat.mixin;

import io.github.gaming32.mobhat.MobHatItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @Inject(
        method = "getPreferredEquipmentSlot",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void wearHat(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
        if (stack.getItem() instanceof MobHatItem) {
            cir.setReturnValue(EquipmentSlot.HEAD);
        }
    }
}
