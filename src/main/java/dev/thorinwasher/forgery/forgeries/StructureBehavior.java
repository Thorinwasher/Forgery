package dev.thorinwasher.forgery.forgeries;

import com.google.common.base.Preconditions;
import dev.thorinwasher.forgery.ForgeryRegistry;
import dev.thorinwasher.forgery.inventory.ForgeryInventory;
import dev.thorinwasher.forgery.structure.ForgeryStructureType;
import dev.thorinwasher.forgery.structure.PlacedForgeryStructure;
import dev.thorinwasher.forgery.structure.StructureMeta;
import dev.thorinwasher.forgery.vector.BlockLocation;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StructureBehavior implements StructureHolder<StructureBehavior>, Interactable {
    private final UUID uuid;
    private PlacedForgeryStructure<StructureBehavior> structure;
    private Map<String, ForgeryInventory> inventories = new HashMap<>();

    public StructureBehavior(UUID blastFurnaceId) {
        this.uuid = blastFurnaceId;
    }

    @Override
    public PlacedForgeryStructure<StructureBehavior> placedStructure() {
        return this.structure;
    }

    @Override
    public void setStructure(PlacedForgeryStructure<StructureBehavior> structure) {
        this.structure = structure;
    }

    @Override
    public ForgeryStructureType structureType() {
        return ForgeryRegistry.STRUCTURE_TYPES.get("blast_furnace");
    }

    @Override
    public void interact(Player actor, BlockLocation location) {
        Block block = location.toBlock();
        BlockType blockType = block.getType().asBlockType();
    }

    public @NotNull ForgeryInventory inventory(String inventoryType) {
        Map<String, ForgeryInventory.Behavior> possibleInventories = placedStructure().metaValue(StructureMeta.INVENTORIES);
        ForgeryInventory.Behavior behavior = possibleInventories.get(inventoryType);
        Preconditions.checkArgument(behavior != null, "Inventory does not have inventory type: " + inventoryType);
        ForgeryInventory forgeryInventory = inventories.get(inventoryType);
        if (forgeryInventory == null) {
            forgeryInventory = new ForgeryInventory(behavior, inventoryType);
        }
        return forgeryInventory;
    }

    @Override
    public UUID uuid() {
        return this.uuid;
    }

    public void setInventories(List<ForgeryInventory> forgeryInventories) {
        forgeryInventories.forEach(
                inventory -> inventories.put(inventory.typeName(), inventory)
        );
    }
}
