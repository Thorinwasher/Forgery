package dev.thorinwasher.forgery.serialize;

import com.google.common.collect.ImmutableMap;
import dev.thorinwasher.forgery.recipe.CraftingRecipe;
import dev.thorinwasher.forgery.recipe.RecipeResult;
import dev.thorinwasher.forgery.util.ForgeryKey;
import dev.thorinwasher.forgery.util.SerializationPrecondition;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class CraftingRecipeSerializer implements TypeSerializer<CraftingRecipe> {
    @Override
    public CraftingRecipe deserialize(Type type, ConfigurationNode node) throws SerializationException {
        RecipeResult result = node.node("result").get(RecipeResult.class);
        CraftingRecipe.Shape recipeShape;
        if (node.hasChild("shape")) {
            String shape = node.node("shape").get(String.class);
            Map<Object, ? extends ConfigurationNode> materialChoice = node.node("materials").childrenMap();
            SerializationPrecondition.check(node, shape != null, "Recipe has to have a shape");
            SerializationPrecondition.check(node, !materialChoice.isEmpty(), "Recipe has to have materials");
            ImmutableMap.Builder<Character, ForgeryKey> choices = new ImmutableMap.Builder<>();
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : materialChoice.entrySet()) {
                if (!(entry.getKey().toString() instanceof String string) || string.length() != 1) {
                    throw new SerializationException("Invalid material choice: " + entry.getKey());
                }
                choices.put(string.charAt(0), ForgeryKey.defaultNamespace("minecraft", entry.getValue().getString()));
            }
            recipeShape = new CraftingRecipe.Shaped(shape, choices.build());
        } else {
            List<String> ingredients = node.node("ingredients").getList(String.class);
            SerializationPrecondition.check(node, ingredients != null && !ingredients.isEmpty(), "ingredients key required with list type");
            recipeShape = new CraftingRecipe.Shapeless(ingredients.stream()
                    .map(string -> ForgeryKey.defaultNamespace("minecraft", string))
                    .toList()
            );
        }
        SerializationPrecondition.check(node, result != null, "Recipe has to have a result");
        return new CraftingRecipe(
                result,
                recipeShape
        );
    }

    @Override
    public void serialize(Type type, @Nullable CraftingRecipe obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.node("result").set(obj.result());
        switch (obj.shape()) {
            case CraftingRecipe.Shaped shaped -> {
                node.node("shape").set(shaped.shape());
                for (Map.Entry<Character, ForgeryKey> entry : shaped.materials().entrySet()) {
                    node.node("materials", entry.getKey()).set(entry.getValue().minimize("minecraft"));
                }
            }
            case CraftingRecipe.Shapeless shapeless -> {
                node.node("ingredients").set(shapeless.ingredients().stream()
                        .map(key -> key.minimize("minecraft"))
                        .toList()
                );
            }
        }
    }
}
