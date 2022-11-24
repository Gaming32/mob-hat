package io.github.gaming32.mobhat.mixin;

import io.github.gaming32.mobhat.MobHatItem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer {
    @Inject(
        method = "getArmPose",
        at = @At("TAIL"),
        cancellable = true
    )
    private static void holdLikeCrossbow(AbstractClientPlayerEntity player, Hand hand, CallbackInfoReturnable<BipedEntityModel.ArmPose> cir) {
        final ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem() instanceof MobHatItem && MobHatItem.getHoldType(stack) != MobHatItem.HoldType.EMPTY) {
            cir.setReturnValue(BipedEntityModel.ArmPose.BLOCK);
        }
    }
}
