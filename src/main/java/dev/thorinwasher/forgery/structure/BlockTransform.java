package dev.thorinwasher.forgery.structure;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;

import java.util.Locale;

public record BlockTransform(Condition condition, BlockData from, BlockData to, BlockConversion blockConversion) {

    public static BlockTransform fromJson(JsonElement json) {
        if (!(json instanceof JsonObject jsonObject)) {
            throw new IllegalArgumentException("Invalid format for block transform.");
        }
        JsonObject condition = jsonObject.get("condition").getAsJsonObject();
        // TODO allow / process more conditions
        String inventoryEmptyConditionInventory = condition.get("inventory_empty").getAsString();
        Condition inventoryEmptyCondition = new InventoryEmptyCondition(inventoryEmptyConditionInventory);
        BlockData from = Bukkit.createBlockData(jsonObject.get("from").getAsString());
        BlockData to = Bukkit.createBlockData(jsonObject.get("to").getAsString());
        BlockConversion blockConversion = BlockConversion.valueOf(jsonObject.get("block_conversion").getAsString().toUpperCase(Locale.ROOT));
        return new BlockTransform(inventoryEmptyCondition, from, to, blockConversion);
    }

    public JsonElement asJson() {
        JsonObject output = new JsonObject();
        JsonObject conditionJson = new JsonObject();
        conditionJson.add("inventory_empty", new JsonPrimitive(((InventoryEmptyCondition) condition).inventoryTypeName()));
        output.add("condition", conditionJson);
        output.add("from", new JsonPrimitive(from.getAsString()));
        output.add("to", new JsonPrimitive(to.getAsString()));
        output.add("block_conversion", new JsonPrimitive(blockConversion.toString().toLowerCase(Locale.ROOT)));
        return output;
    }

    public sealed interface Condition {
    }

    public record InventoryEmptyCondition(String inventoryTypeName) implements Condition {
    }

    public enum BlockConversion {
        KEEP_OTHER_DATA
    }
}
