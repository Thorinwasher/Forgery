package dev.thorinwasher.forgery.serialize;

import com.google.common.collect.ImmutableMap;
import dev.thorinwasher.forgery.forging.ForgingIngredients;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ForgingIngredientsSerializer implements TypeSerializer<ForgingIngredients> {
    @Override
    public ForgingIngredients deserialize(@NotNull Type type, ConfigurationNode node) throws SerializationException {
        List<ConfigurationNode> configNodes = node.getList(ConfigurationNode.class);
        if (configNodes == null) {
            return null;
        }
        ImmutableMap.Builder<ForgingMaterial, Integer> output = new ImmutableMap.Builder<>();
        for (ConfigurationNode ingredientNode : configNodes) {
            Map<Object, ? extends ConfigurationNode> ingredient = ingredientNode.childrenMap();
            if (!ingredient.containsKey("material")) {
                continue;
            }
            Integer amount = ingredient.containsKey("amount") ? ingredient.get("amount").get(Integer.class) : null;
            ForgingMaterial material = ingredient.get("material").get(ForgingMaterial.class);
            if (material != null) {
                output.put(material, amount == null ? 1 : amount);
            }
        }
        return new ForgingIngredients(output.build());
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable ForgingIngredients obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        for (Map.Entry<ForgingMaterial, Integer> ingredient : obj.ingredients().entrySet()) {
            ConfigurationNode listNode = node.appendListNode();
            listNode.node("amount").set(ingredient.getValue());
            listNode.node("material").set(ingredient.getKey());
        }
    }
}
