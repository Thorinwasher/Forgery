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
        String inventoryEmptyConditionInventory = node.node("condition", "inventory_empty").get(String.class);
        BlockData from = node.node("from").get(BlockData.class);
        BlockData to = node.node("to").get(BlockData.class);
        if (inventoryEmptyConditionInventory == null || from == null || to == null) {
            throw new SerializationException("Missing data for block transform, requires: condition, from, to");
        }
        BlockTransform.BlockConversion blockConversion = node.node("block_conversion").get(BlockTransform.BlockConversion.class);
        return new BlockTransform(
                new BlockTransform.InventoryEmptyCondition(inventoryEmptyConditionInventory),
                from,
                to,
                blockConversion == null ? BlockTransform.BlockConversion.KEEP_OTHER_DATA : blockConversion
        );
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable BlockTransform obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.node("condition", "inventory_empty").set(((BlockTransform.InventoryEmptyCondition) obj.condition()).inventoryTypeName());
        node.node("from").set(BlockData.class);
        node.node("to").set(BlockData.class);
        node.node("block_conversion").set(obj.blockConversion());
    }
}
