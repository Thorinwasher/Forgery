package dev.thorinwasher.forgery.structure;

import org.bukkit.block.data.BlockData;

public record BlockTransform(Condition condition, BlockData from, BlockData to, BlockConversion blockConversion) {


    public sealed interface Condition {
    }

    public record InventoryEmptyCondition(String inventoryTypeName) implements Condition {
    }

    public enum BlockConversion {
        KEEP_OTHER_DATA
    }
}
