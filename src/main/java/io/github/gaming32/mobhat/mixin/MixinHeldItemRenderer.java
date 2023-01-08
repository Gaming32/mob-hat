package io.github.gaming32.mobhat.mixin;

import io.github.gaming32.mobhat.MobHat;
import io.github.gaming32.mobhat.MobHatItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer {
    @Shadow protected abstract void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress);

    @Shadow protected abstract void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress);

    @Shadow public abstract void renderItem(LivingEntity entity, ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    @Inject(
        method = "renderFirstPersonItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;isEmpty()Z",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private void renderHatSpecially(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (item.isEmpty() || !item.isOf(MobHat.MOB_HAT_ITEM) || MobHatItem.getHoldType(item) == MobHatItem.HoldType.EMPTY) return;
        ci.cancel();

        boolean bl = hand == Hand.MAIN_HAND;
        Arm arm = bl ? player.getMainArm() : player.getMainArm().getOpposite();

        boolean bl3 = arm == Arm.RIGHT;
        int i = bl3 ? 1 : -1;
        if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
            applyEquipOffset(matrices, arm, equipProgress);
            matrices.translate((float)i * -0.4785682F, -0.094387F, 0.05731531F);
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-11.935F));
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((float)i * 65.3F));
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion((float)i * -9.785F));
            assert MinecraftClient.getInstance().player != null;
            float f = (float)item.getMaxUseTime() - ((float)MinecraftClient.getInstance().player.getItemUseTimeLeft() - tickDelta + 1.0F);
            float g = f / (float)CrossbowItem.getPullTime(item);
            if (g > 1.0F) {
                g = 1.0F;
            }

            if (g > 0.1F) {
                float h = MathHelper.sin((f - 0.1F) * 1.3F);
                float j = g - 0.1F;
                float k = h * j;
                matrices.translate(k * 0.0F, k * 0.004F, k * 0.0F);
            }

            matrices.translate(g * 0.0F, g * 0.0F, g * 0.04F);
            matrices.scale(1.0F, 1.0F, 1.0F + g * 0.2F);
            matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion((float)i * 45.0F));
        } else {
            float f = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
            float g = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) (Math.PI * 2));
            float h = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);
            matrices.translate((float)i * f, g, h);
            this.applyEquipOffset(matrices, arm, equipProgress);
            applySwingOffset(matrices, arm, swingProgress);
            if (swingProgress < 0.001F && bl) {
                matrices.translate((float)i * -0.641864F, 0.0, 0.0);
                matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((float)i * 10.0F));
            }
        }

        renderItem(
            player,
            item,
            bl3 ? ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND : ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND,
            !bl3,
            matrices,
            vertexConsumers,
            light
        );

        matrices.pop();
    }
}
