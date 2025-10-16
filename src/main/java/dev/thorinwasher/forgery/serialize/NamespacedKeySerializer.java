package dev.thorinwasher.forgery.serialize;

import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class NamespacedKeySerializer implements TypeSerializer<NamespacedKey> {
    @Override
    public NamespacedKey deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String key = node.getString();
        if (!Key.parseable(key)) {
            return null;
        }
        return NamespacedKey.fromString(key);
    }

    @Override
    public void serialize(Type type, @Nullable NamespacedKey obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.set(obj.asMinimalString());
    }
}
