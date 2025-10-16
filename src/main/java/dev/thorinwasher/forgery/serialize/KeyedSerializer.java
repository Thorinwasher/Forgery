package dev.thorinwasher.forgery.serialize;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Optional;

public class KeyedSerializer<T extends Keyed> implements TypeSerializer<T> {

    private final RegistryKey<T> registryKey;

    public KeyedSerializer(RegistryKey<T> registryKey) {
        this.registryKey = registryKey;
    }

    @Override
    public T deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        String keyString = node.get(String.class);
        if (keyString == null) {
            return null;
        }
        return Optional.ofNullable(NamespacedKey.fromString(keyString))
                .flatMap(key -> Optional.ofNullable(RegistryAccess.registryAccess().getRegistry(registryKey).get(key)))
                .orElse(null);
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable T obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.set(obj.key().asString());
    }
}
