package dev.thorinwasher.forgery.structure;

import dev.thorinwasher.forgery.vector.BlockLocation;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.List;

public record BlockTransform(List<Condition> conditions, BlockData from, BlockData to,
                             BlockConversion blockConversion) {


    public void applyToBlock(BlockLocation location) {
        convertBlockData(location, from, to);
    }

    public void deapplyToBlock(BlockLocation location) {
        convertBlockData(location, to, from);
    }

    private void convertBlockData(BlockLocation location, BlockData filter, BlockData toApply) {
        Block block = location.toBlock();
        BlockData initial = block.getBlockData();
        if (!initial.matches(filter)) {
            return;
        }
        if (toApply.getMaterial() != initial.getMaterial()) {
            block.setBlockData(toApply);
            return;
        }
        block.setBlockData(initial.merge(toApply));
    }

    public sealed interface Condition {
    }

    public record InventoryEmptyCondition(String inventoryTypeName) implements Condition {
    }

    public record StructureAgeCondition(long age) implements Condition {
    }

    public enum BlockConversion {
        KEEP_OTHER_DATA
    }
}
