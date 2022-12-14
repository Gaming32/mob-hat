package io.github.gaming32.mobhat;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MobHatCriterion extends AbstractCriterion<MobHatCriterion.Conditions> {
    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(getId(), playerPredicate, EntityPredicate.Extended.getInJson(obj, "entity", predicateDeserializer));
    }

    @Override
    public Identifier getId() {
        return MobHat.STANDARD_ID;
    }

    public void trigger(ServerPlayerEntity player, Entity entity) {
        final LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player, entity);
        trigger(player, conditions -> conditions.matches(lootContext));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final EntityPredicate.Extended predicate;

        public Conditions(Identifier id, EntityPredicate.Extended playerPredicate, EntityPredicate.Extended predicate) {
            super(id, playerPredicate);
            this.predicate = predicate;
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            final JsonObject result = new JsonObject();
            result.add("entity", predicate.toJson(predicateSerializer));
            return result;
        }

        public boolean matches(LootContext lootContext) {
            return predicate.test(lootContext);
        }
    }
}
