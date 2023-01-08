package io.github.gaming32.mobhat;

import io.github.gaming32.mobhat.mixin.EntityAccessor;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Wearable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MobHatItem extends Item implements Wearable {
    public enum HoldType {
        EMPTY,
        ENTITY,
        // PLAYER
    }

    public static final String HOLD_TYPE_KEY = "HoldType";
    public static final String ENTITY_TYPE_KEY = "EntityType";
    public static final String ENTITY_KEY = "Entity";
    public static final String CUSTOM_NAME_KEY = "CustomName";

    public MobHatItem(Settings settings) {
        super(settings);
        DispenserBlock.registerBehavior(this, new ItemDispenserBehavior() {
            boolean playSound;

            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                final BlockPos offsetPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                if (getHoldType(stack) != HoldType.EMPTY) {
                    playSound = true;
                    spawnEntity(stack, pointer.getWorld(), offsetPos);
                } else {
                    playSound = false;
                    final List<LivingEntity> entities = pointer.getWorld().getEntitiesByClass(
                        LivingEntity.class,
                        Box.from(BlockBox.create(offsetPos, offsetPos)),
                        entity -> !(entity instanceof PlayerEntity)
                    );
                    if (!entities.isEmpty()) {
                        pickUpEntity(stack, entities.get(0));
                    }
                }
                return stack;
            }

            @Override
            protected void playSound(BlockPointer pointer) {
                if (playSound) {
                    super.playSound(pointer);
                }
            }

            @Override
            protected void spawnParticles(BlockPointer pointer, Direction side) {
                if (playSound) {
                    super.spawnParticles(pointer, side);
                }
            }
        });
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (user.world.isClient) {
            return ActionResult.CONSUME;
        }
        if (getHoldType(stack) != HoldType.EMPTY) {
            return ActionResult.FAIL;
        }
        if (entity instanceof PlayerEntity) {
            return ActionResult.FAIL;
        }
        MobHat.MOB_HAT_CRITERION.trigger((ServerPlayerEntity)user, entity);
        pickUpEntity(stack, entity);
        user.setStackInHand(hand, stack);
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld().isClient) {
            return ActionResult.CONSUME;
        }
        if (getHoldType(context.getStack()) == HoldType.EMPTY) {
            return ActionResult.FAIL;
        }
        if (getHoldType(context.getStack()) == HoldType.ENTITY) {
            spawnEntity(
                context.getStack(),
                (ServerWorld)context.getWorld(),
                context.getBlockPos().offset(context.getSide())
            );
        }
        return ActionResult.SUCCESS;
    }

    public static void pickUpEntity(ItemStack stack, LivingEntity entity) {
        final NbtCompound nbt = getNbt(stack);
        nbt.putString(HOLD_TYPE_KEY, HoldType.ENTITY.name());
        nbt.put(ENTITY_KEY, entity.writeNbt(new NbtCompound()));
        nbt.putString(ENTITY_TYPE_KEY, Registry.ENTITY_TYPE.getId(entity.getType()).toString());
        if (entity.hasCustomName()) {
            nbt.putString(CUSTOM_NAME_KEY, Text.Serializer.toJson(entity.getCustomName()));
        }
        entity.discard();
    }

    public static void spawnEntity(ItemStack stack, ServerWorld world, BlockPos pos) {
        final NbtCompound nbt = getNbt(stack);
        nbt.putString(HOLD_TYPE_KEY, HoldType.EMPTY.name());
        final NbtCompound entityNbt = nbt.getCompound(ENTITY_KEY);
        nbt.remove(ENTITY_KEY);
        final EntityType<?> entityType = Registry.ENTITY_TYPE.get(new Identifier(nbt.getString(ENTITY_TYPE_KEY)));
        nbt.remove(ENTITY_TYPE_KEY);
        final Text customName = nbt.contains(CUSTOM_NAME_KEY, NbtElement.STRING_TYPE)
            ? Text.Serializer.fromJson(nbt.getString(CUSTOM_NAME_KEY))
            : null;
        if (customName != null) {
            nbt.remove(CUSTOM_NAME_KEY);
        }
        final Entity entity = entityType.spawn(world, null, customName, null, pos, SpawnReason.BUCKET, true, false);
        //noinspection ConstantConditions
        entity.setYaw(entityNbt.getList("Rotation", NbtElement.FLOAT_TYPE).getFloat(0));
        entity.setPitch(entityNbt.getList("Rotation", NbtElement.FLOAT_TYPE).getFloat(1));
        entity.setHeadYaw(entity.getYaw());
        entity.setBodyYaw(entity.getYaw());
        entity.setFireTicks(entityNbt.getShort("Fire"));
        if (entityNbt.contains("Air")) {
            entity.setAir(entityNbt.getShort("Air"));
        }
        entity.setInvulnerable(entityNbt.getBoolean("Invulnerable"));
        entity.setCustomNameVisible(entityNbt.getBoolean("CustomNameVisible"));
        entity.setSilent(entityNbt.getBoolean("Silent"));
        entity.setNoGravity(entityNbt.getBoolean("NoGravity"));
        entity.setGlowing(entityNbt.getBoolean("Glowing"));
        entity.setFrozenTicks(entityNbt.getInt("TicksFrozen"));
        if (entityNbt.contains("Tags", NbtElement.LIST_TYPE)) {
            entity.getScoreboardTags().clear();
            NbtList nbtList4 = nbt.getList("Tags", NbtElement.STRING_TYPE);
            int i = Math.min(nbtList4.size(), 1024);

            for(int j = 0; j < i; ++j) {
                entity.getScoreboardTags().add(nbtList4.getString(j));
            }
        }
        ((EntityAccessor)entity).readCustomDataFromNbt(entityNbt);
    }

    public static HoldType getHoldType(ItemStack stack) {
        return HoldType.valueOf(getNbt(stack).getString(HOLD_TYPE_KEY));
    }

    public static NbtCompound getNbt(ItemStack stack) {
        final NbtCompound nbt = stack.getOrCreateSubNbt("MobHat");
        if (!nbt.contains(HOLD_TYPE_KEY, NbtElement.STRING_TYPE)) {
            nbt.putString(HOLD_TYPE_KEY, HoldType.EMPTY.name());
        }
        return nbt;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (getHoldType(stack) == HoldType.EMPTY) {
            return;
        }
        final NbtCompound nbt = getNbt(stack);
        final Text entityName;
        if (nbt.contains(CUSTOM_NAME_KEY, NbtElement.STRING_TYPE)) {
            entityName = Text.Serializer.fromJson(nbt.getString(CUSTOM_NAME_KEY));
        } else {
            entityName = Registry.ENTITY_TYPE.get(new Identifier(nbt.getString(ENTITY_TYPE_KEY))).getName();
        }
        tooltip.add(Text.translatable("item.mob_hat.mob_hat.entity").append(" ").append(entityName));
    }
}
