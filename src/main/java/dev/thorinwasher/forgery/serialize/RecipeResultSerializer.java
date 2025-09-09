package dev.thorinwasher.forgery.serialize;

import com.google.common.base.Preconditions;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.recipe.RecipeResult;
import dev.thorinwasher.forgery.util.ForgeryKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class RecipeResultSerializer implements TypeSerializer<RecipeResult> {
    @Override
    public RecipeResult deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        boolean overrideLore = !node.hasChild("override-lore") || node.node("override-lore").getBoolean();
        Preconditions.checkArgument(node.hasChild("data") != node.hasChild("material"));
        if (node.hasChild("data")) {
            return new RecipeResult(new RecipeResult.DataBased(null), overrideLore);
        } else {
            return new RecipeResult(new RecipeResult.PluginItem(
                    new ForgingMaterial(
                            ForgeryKey.defaultNamespace("minecraft", node.node("material").get(String.class))
                    )), overrideLore
            );
        }
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable RecipeResult obj, @NotNull ConfigurationNode node) throws SerializationException {

    }
}
