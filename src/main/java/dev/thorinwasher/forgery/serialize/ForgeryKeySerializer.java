package dev.thorinwasher.forgery.serialize;

import dev.thorinwasher.forgery.util.ForgeryKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Locale;

public class ForgeryKeySerializer implements TypeSerializer<ForgeryKey> {
    @Override
    public ForgeryKey deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        String key = node.get(String.class);
        if (key == null) {
            return null;
        }
        return ForgeryKey.defaultNamespace("minecraft", key.toLowerCase(Locale.ROOT));
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable ForgeryKey obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.set(obj.asString());
    }
}
