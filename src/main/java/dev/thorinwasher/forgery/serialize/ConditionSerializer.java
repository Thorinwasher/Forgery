package dev.thorinwasher.forgery.serialize;

import dev.thorinwasher.forgery.structure.Condition;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ConditionSerializer implements TypeSerializer<Condition> {
    @Override
    public Condition deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        if (node.hasChild("inventory_empty")) {
            String inventory = node.node("inventory_empty").get(String.class);
            return inventory != null ? new Condition.InventoryEmptyCondition(inventory) : null;
        }
        if (node.hasChild("structure_age")) {
            Long age = node.node("structure_age").get(Long.class);
            return age != null ? new Condition.StructureAgeCondition(age) : null;
        }
        if (node.hasChild("not")) {
            Condition condition = node.node("not").get(Condition.class);
            return condition != null ? new Condition.InvertedCondition(condition) : null;
        }
        return null;
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable Condition obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        switch (obj) {
            case Condition.InventoryEmptyCondition inventoryEmptyCondition ->
                    node.node("inventory_empty").set(inventoryEmptyCondition.inventoryTypeName());
            case Condition.InvertedCondition invertedCondition -> node.node("not").set(invertedCondition.condition());
            case Condition.StructureAgeCondition structureAgeCondition ->
                    node.node("structure_age").set(structureAgeCondition.age());
        }
    }
}
