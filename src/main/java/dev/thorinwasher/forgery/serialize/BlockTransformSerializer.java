package dev.thorinwasher.forgery.serialize;

import dev.thorinwasher.forgery.structure.BlockTransform;
import org.bukkit.block.data.BlockData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class BlockTransformSerializer implements TypeSerializer<BlockTransform> {
    @Override
    public BlockTransform deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        String toState = node.node("to_state").get(String.class);
        BlockData from = node.node("from").get(BlockData.class);
        BlockData to = node.node("to").get(BlockData.class);
        if (toState == null || from == null || to == null) {
            throw new SerializationException("Missing data for block transform, requires: to_state, from, to");
        }
        BlockTransform.BlockConversion blockConversion = node.node("block_conversion").get(BlockTransform.BlockConversion.class);
        return new BlockTransform(
                toState,
                from,
                to,
                blockConversion == null ? BlockTransform.BlockConversion.KEEP_OTHER_DATA : blockConversion
        );
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable BlockTransform obj, @NotNull ConfigurationNode node) throws
            SerializationException {
        if (obj == null) {
            return;
        }
        node.node("to_state").set(obj.toState());
        node.node("from").set(obj.from());
        node.node("to").set(obj.to());
        node.node("block_conversion").set(obj.blockConversion());
    }
}
