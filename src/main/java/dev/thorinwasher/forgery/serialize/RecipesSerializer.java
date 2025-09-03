package dev.thorinwasher.forgery.serialize;

import com.google.common.collect.ImmutableMap;
import dev.thorinwasher.forgery.forging.ForgingSteps;
import dev.thorinwasher.forgery.recipe.Recipe;
import dev.thorinwasher.forgery.recipe.RecipeResult;
import dev.thorinwasher.forgery.util.ForgeryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Map;

public class RecipesSerializer implements TypeSerializer<Map<ForgeryKey, Recipe>> {
    @Override
    public Map<ForgeryKey, Recipe> deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        ImmutableMap.Builder<ForgeryKey, Recipe> output = new ImmutableMap.Builder<>();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
            ForgeryKey key = ForgeryKey.forgery(entry.getKey().toString());
            ConfigurationNode configurationNode = entry.getValue();
            RecipeResult recipeResult = configurationNode.node("result").get(RecipeResult.class);
            ForgingSteps steps = configurationNode.node("steps").get(ForgingSteps.class);
            String structureType = configurationNode.node("structure").get(String.class);
            output.put(key, new Recipe(steps, recipeResult, structureType, key));
        }
        return output.build();
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable Map<ForgeryKey, Recipe> obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        for (Map.Entry<ForgeryKey, Recipe> entry : obj.entrySet()) {
            ConfigurationNode configurationNode = node.node(entry.getKey().forgery());
            configurationNode.node("result").set(entry.getValue().result());
            configurationNode.node("steps").set(entry.getValue().steps());
            configurationNode.node("structure").set(entry.getValue().structureType());
        }
    }
}
