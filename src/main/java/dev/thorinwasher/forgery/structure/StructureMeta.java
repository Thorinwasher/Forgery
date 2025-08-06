package dev.thorinwasher.forgery.structure;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.ForgeryRegistry;
import dev.thorinwasher.forgery.inventory.ForgeryInventory;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.BlockType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public record StructureMeta<T>(Key key, Function<JsonElement, T> deserializer) implements Keyed {

    public static final StructureMeta<ForgeryStructureType> TYPE = new StructureMeta<>(Key.key(Forgery.NAMESPACE, "type"), json ->
            ForgeryRegistry.STRUCTURE_TYPES.get(Key.key(Forgery.NAMESPACE, json.getAsString()))
    );

    public static final StructureMeta<Map<String, ForgeryInventory.Behavior>> INVENTORIES = new StructureMeta<>(
            Key.key(Forgery.NAMESPACE, "inventories"),
            jsonElement -> {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                ImmutableMap.Builder<String, ForgeryInventory.Behavior> builder = new ImmutableMap.Builder<>();
                for (String key : jsonObject.keySet()) {
                    builder.put(key, ForgeryInventory.Behavior.fromJson(jsonObject.get(key)));
                }
                return builder.build();
            }
    );

    public static final StructureMeta<List<BlockTransform>> BLOCK_TRANSFORMS = new StructureMeta<>(
            Forgery.key("block_transforms"),
            jsonElement ->
                    jsonElement.getAsJsonArray().asList().stream()
                            .map(BlockTransform::fromJson)
                            .toList()
    );

    public static final StructureMeta<Integer> HEAT_RESULT = new StructureMeta<>(
            Forgery.key("heat_result"),
            JsonElement::getAsInt
    );

    public static final StructureMeta<Set<String>> PROCESS_PARAMETERS = new StructureMeta<>(
            Forgery.key("process_parameters"),
            jsonElement ->
                    jsonElement.getAsJsonArray().asList().stream()
                            .map(JsonElement::getAsString)
                            .collect(Collectors.toUnmodifiableSet())
    );

    private static Set<BlockType> parseBlocks(JsonElement json) {
        return json.getAsJsonArray().asList().stream()
                .map(JsonElement::getAsString)
                .map(NamespacedKey::fromString)
                .filter(Objects::nonNull)
                .map(Registry.BLOCK::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }
}
