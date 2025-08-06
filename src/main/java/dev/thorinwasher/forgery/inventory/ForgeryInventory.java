package dev.thorinwasher.forgery.inventory;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.block.BlockType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ForgeryInventory {

    private final String typeName;
    private final Behavior behavior;
    private final ForgeryItem[] contents;

    public ForgeryInventory(Behavior behavior, String typeName) {
        this.typeName = typeName;
        this.behavior = behavior;
        this.contents = new ForgeryItem[behavior.size()];
    }

    public void setItem(ItemRecord item) {
        contents[item.pos()] = item.forgeryItem();
    }

    public String typeName() {
        return this.typeName;
    }

    public Behavior behavior() {
        return this.behavior;
    }

    @Override
    public @NotNull String toString() {
        return String.format("ForgeryInventory{%s}", String.join(", ", List.of("typeName=" + typeName, "behavior=" + behavior, "contents=" + Arrays.deepToString(contents))));
    }

    public @NotNull List<ItemRecord> items() {
        ImmutableList.Builder<ItemRecord> items = new ImmutableList.Builder<>();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null) {
                continue;
            }
            items.add(new ItemRecord(i, contents[i]));
        }
        return items.build();
    }

    public record ItemRecord(int pos, ForgeryItem forgeryItem) {

    }

    public record Behavior(AccessBehavior access, ItemDisplayBehavior itemDisplay, int size,
                           Set<BlockType> interfaceBlocks) {

        public static Behavior fromJson(JsonElement jsonElement) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String accessBehavior = jsonObject.get("access_behavior").getAsString();
            Set<BlockType> interfaceBlocks = jsonObject.get("interface_blocks").getAsJsonArray().asList().stream()
                    .map(JsonElement::getAsString)
                    .map(Key::key)
                    .map(Registry.BLOCK::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet());
            int size = jsonObject.get("size").getAsInt();
            ItemDisplayBehavior itemDisplay = jsonObject.has("item_display") ?
                    ItemDisplayBehavior.valueOf(jsonObject.get("item_display").getAsString().toUpperCase(Locale.ROOT)) : ItemDisplayBehavior.NONE;
            return new Behavior(
                    AccessBehavior.valueOf(accessBehavior.toUpperCase(Locale.ROOT)),
                    itemDisplay,
                    size,
                    interfaceBlocks
            );
        }
    }

    public enum AccessBehavior {
        OPENABLE,
        INSERTABLE
    }

    public enum ItemDisplayBehavior {
        UNDER, ABOVE, INSIDE, NONE
    }
}
