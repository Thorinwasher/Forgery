package dev.thorinwasher.forgery.serialize;

import com.google.common.collect.ImmutableList;
import dev.thorinwasher.forgery.structure.BlockTransform;
import dev.thorinwasher.forgery.util.Pair;
import org.bukkit.block.data.BlockData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class BlockTransformSerializer implements TypeSerializer<BlockTransform> {
    @Override
    public BlockTransform deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        Map<Object, ? extends ConfigurationNode> conditionNode = node.node("condition").childrenMap();
        ImmutableList.Builder<BlockTransform.Condition> conditions = new ImmutableList.Builder<>();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : conditionNode.entrySet()) {
            conditions.add(switch (entry.getKey().toString()) {
                case "inventory_empty" ->
                        new BlockTransform.InventoryEmptyCondition(entry.getValue().get(String.class));
                case "structure_lifetime" -> new BlockTransform.StructureAgeCondition(entry.getValue().get(Long.class));
                default -> throw new SerializationException("Unknown conditions: " + entry.getKey());
            });
        }
        BlockData from = node.node("from").get(BlockData.class);
        BlockData to = node.node("to").get(BlockData.class);
        List<BlockTransform.Condition> conditionsBuilt = conditions.build();
        if (conditionsBuilt.isEmpty() || from == null || to == null) {
            throw new SerializationException("Missing data for block transform, requires: conditions, from, to");
        }
        BlockTransform.BlockConversion blockConversion = node.node("block_conversion").get(BlockTransform.BlockConversion.class);
        return new BlockTransform(
                conditionsBuilt,
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
        ConfigurationNode conditionNode = node.node("condition");
        for (BlockTransform.Condition condition : obj.conditions()) {
            Pair<String, Object> entry = switch (condition) {
                case BlockTransform.InventoryEmptyCondition inventoryEmptyCondition ->
                        new Pair<>("inventory_empty", inventoryEmptyCondition.inventoryTypeName());
                case BlockTransform.StructureAgeCondition structureAgeCondition ->
                        new Pair<>("structure_lifetime", structureAgeCondition.age());
            };
            conditionNode.node(entry.first()).set(entry.second());
        }
        node.node("from").set(BlockData.class);
        node.node("to").set(BlockData.class);
        node.node("block_conversion").set(obj.blockConversion());
    }
}
