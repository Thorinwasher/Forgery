package dev.thorinwasher.forgery.serialize;

import dev.thorinwasher.forgery.forging.ToolInput;
import dev.thorinwasher.forgery.util.ForgeryKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ToolInputSerializer implements TypeSerializer<ToolInput> {

    @Override
    public ToolInput deserialize(@NotNull Type type, ConfigurationNode node) throws SerializationException {
        String value = node.get(String.class);
        if (value == null) {
            return null;
        }
        ForgeryKey key = ForgeryKey.defaultNamespace("minecraft", value);
        return new ToolInput(key);
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable ToolInput obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.set(obj.key().asString());
    }
}
