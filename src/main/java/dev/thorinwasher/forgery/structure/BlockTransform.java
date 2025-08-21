package dev.thorinwasher.forgery.structure;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;

import java.util.Locale;

public record BlockTransform(Condition condition, BlockData from, BlockData to, BlockConversion blockConversion) {


    public sealed interface Condition {
    }

    public record InventoryEmptyCondition(String inventoryTypeName) implements Condition {
    }

    public enum BlockConversion {
        KEEP_OTHER_DATA
    }
}
