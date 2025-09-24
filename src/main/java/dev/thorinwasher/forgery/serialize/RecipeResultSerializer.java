package dev.thorinwasher.forgery.serialize;

import com.google.common.base.Preconditions;
import dev.thorinwasher.forgery.recipe.RecipeResult;
import dev.thorinwasher.forgery.util.ForgeryKey;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;

public record RecipeResultSerializer() implements TypeSerializer<RecipeResult> {


    @Override
    public RecipeResult deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        boolean overrideLore = !node.hasChild("override-lore") || node.node("override-lore").getBoolean();
        int amount = Math.max(1, node.node("amount").getInt(1));
        Preconditions.checkArgument(node.hasChild("material"), "Expected one definition of stored-item or material.");
        Component name = node.node("name").get(Component.class);
        List<Component> lore = node.node("lore").getList(Component.class);
        String toolId = node.node("tool-id").getString();
        return new RecipeResult(
                ForgeryKey.defaultNamespace("minecraft", node.node("material").getString()),
                amount,
                overrideLore,
                lore,
                name,
                toolId
        );
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable RecipeResult obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.node("override-lore").set(obj.overrideLore());
        if (obj.amount() > 1) {
            node.node("amount").set(obj.amount());
        }
        node.node("material").set(obj.key().minimize("minecraft"));
        if (obj.name() != null) {
            node.node("name").set(obj.name());
        }
        if (!obj.lore().isEmpty()) {
            node.node("lore").set(obj.lore());
        }
        if (obj.toolId() != null) {
            node.node("tool-id").set(obj.toolId());
        }
    }
}
