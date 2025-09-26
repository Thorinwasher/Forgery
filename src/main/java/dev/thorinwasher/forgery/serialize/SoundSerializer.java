package dev.thorinwasher.forgery.serialize;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class SoundSerializer implements TypeSerializer<Sound> {
    @Override
    public Sound deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String string = node.getString();
        if (string == null) {
            return null;
        }
        NamespacedKey key = NamespacedKey.fromString(string);
        if (key == null) {
            return null;
        }
        return Registry.SOUND_EVENT.get(key);
    }

    @Override
    public void serialize(Type type, @Nullable Sound obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        NamespacedKey key = Registry.SOUND_EVENT.getKey(obj);
        if (key == null) {
            return;
        }
        node.set(key.asMinimalString());
    }
}
