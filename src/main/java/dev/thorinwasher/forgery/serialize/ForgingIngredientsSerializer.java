package dev.thorinwasher.forgery.serialize;

import com.google.common.collect.ImmutableMap;
import dev.thorinwasher.forgery.forging.ForgingIngredients;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.util.ForgeryKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Map;

public class ForgingIngredientsSerializer implements TypeSerializer<ForgingIngredients> {
    @Override
    public ForgingIngredients deserialize(@NotNull Type type, ConfigurationNode node) throws SerializationException {
        Map<Object, ? extends ConfigurationNode> configNodes = node.childrenMap();
        ImmutableMap.Builder<ForgingMaterial, Integer> output = new ImmutableMap.Builder<>();
        for (Map.Entry<Object, ? extends ConfigurationNode> ingredientNode : configNodes.entrySet()) {
            int amount;
            int quality;
            if (ingredientNode.getValue().isMap()) {
                amount = ingredientNode.getValue().node("amount").getInt(1);
                quality = ingredientNode.getValue().node("quality").getInt(10);
            } else {
                amount = ingredientNode.getValue().getInt(1);
                quality = 10;
            }
            ForgingMaterial material = new ForgingMaterial(ForgeryKey.defaultNamespace("minecraft", ingredientNode.getKey().toString()), quality);
            output.put(material, amount);
        }
        return new ForgingIngredients(output.build());
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable ForgingIngredients obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        for (Map.Entry<ForgingMaterial, Integer> ingredient : obj.ingredients().entrySet()) {
            if (ingredient.getKey().key() == null) {
                continue;
            }
            ForgingMaterial material = ingredient.getKey();
            ConfigurationNode listNode = node.node(material.key().minimize("minecraft"));
            if (material.score() == 10) {
                listNode.set(ingredient.getValue());
            } else {
                listNode.node("amount").set(ingredient.getValue());
                listNode.node("quality").set(material.score());
            }
        }
    }
}
