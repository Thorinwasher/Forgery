package dev.thorinwasher.forgery.serialize;

import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.util.ForgeryKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ForgingMaterialSerializer implements TypeSerializer<ForgingMaterial> {
    @Override
    public ForgingMaterial deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        ForgeryKey forgeryKey = node.node("key").get(ForgeryKey.class);
        int score = node.node("score").get(Integer.class, 10);
        return new ForgingMaterial(forgeryKey, score);
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable ForgingMaterial obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        if (obj.key() != null) {
            node.node("key").set(obj.key());
        }
        if (obj.score() != 10) {
            node.node("score").set(obj.score());
        }
    }
}
