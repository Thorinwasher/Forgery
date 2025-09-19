package dev.thorinwasher.forgery.serialize;

import com.google.common.base.Preconditions;
import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.recipe.ItemReference;
import dev.thorinwasher.forgery.recipe.MaterialReference;
import dev.thorinwasher.forgery.recipe.RecipeResult;
import dev.thorinwasher.forgery.util.ForgeryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public record RecipeResultSerializer(Map<Key, ItemReference> itemReferences) implements TypeSerializer<RecipeResult> {


    @Override
    public RecipeResult deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        boolean overrideLore = !node.hasChild("override-lore") || node.node("override-lore").getBoolean();
        int amount = Math.max(1, node.node("amount").getInt(1));
        Preconditions.checkArgument(node.hasChild("stored-item") != node.hasChild("material"), "Expected one definition of stored-item or material.");
        String nameString = node.node("name").getString();
        Component name = nameString != null ? MiniMessage.miniMessage().deserialize(nameString) : null;
        List<String> loreString = node.node("lore").getList(String.class);
        List<Component> lore = loreString != null ? loreString.stream().map(MiniMessage.miniMessage()::deserialize).toList() :
                List.of();
        if (node.hasChild("stored-item")) {
            Key itemKey = Forgery.key(node.node("stored-item").get(String.class));
            if (!itemReferences.containsKey(itemKey)) {
                throw new IllegalArgumentException("There is no item reference with key '" + itemKey + "'");
            }
            return new RecipeResult(itemReferences.get(itemKey), amount, overrideLore, lore, name);
        }
        if (node.hasChild("material")) {
            return new RecipeResult(
                    new MaterialReference(new ForgingMaterial(ForgeryKey.defaultNamespace("minecraft", node.node("material").getString()))),
                    amount,
                    overrideLore,
                    lore,
                    name
            );
        }
        return null;
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
        switch (obj.itemWriter()) {
            case ItemReference itemReference -> node.node("stored-item").set(itemReference.getKey().value());
            case MaterialReference materialReference -> node.node("material").set(materialReference.material().key());
            default -> throw new IllegalStateException("Unexpected value: " + obj.itemWriter());
        }
    }
}
