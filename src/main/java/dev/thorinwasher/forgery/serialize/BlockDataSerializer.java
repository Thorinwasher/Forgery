package dev.thorinwasher.forgery.serialize;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class BlockDataSerializer implements TypeSerializer<BlockData> {
    @Override
    public BlockData deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        String value = node.get(String.class);
        if (value == null) {
            return null;
        }
        return Bukkit.createBlockData(value);
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable BlockData obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.set(obj.getAsString());
    }
}
