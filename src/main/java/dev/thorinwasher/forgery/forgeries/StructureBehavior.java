package dev.thorinwasher.forgery.forgeries;

import com.google.common.base.Preconditions;
import dev.thorinwasher.forgery.database.PersistencyAccess;
import dev.thorinwasher.forgery.inventory.ForgeryInventory;
import dev.thorinwasher.forgery.inventory.InventoryStoredData;
import dev.thorinwasher.forgery.structure.PlacedForgeryStructure;
import dev.thorinwasher.forgery.structure.StructureMeta;
import dev.thorinwasher.forgery.vector.BlockLocation;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StructureBehavior implements Interactable {
    private final UUID uuid;
    private final PersistencyAccess persistencyAccess;
    private PlacedForgeryStructure structure;
    private Map<String, ForgeryInventory> inventories = new HashMap<>();
    private Map<BlockType, String> blockTypeInventoryTypeMap = new HashMap<>();

    public StructureBehavior(UUID blastFurnaceId, PersistencyAccess persistencyAccess) {
        this.uuid = blastFurnaceId;
        this.persistencyAccess = persistencyAccess;
    }

    public PlacedForgeryStructure placedStructure() {
        return this.structure;
    }

    public void setStructure(PlacedForgeryStructure structure) {
        Preconditions.checkState(this.structure == null, "Can not change structure after being set");
        this.structure = structure;
        for (Map.Entry<String, ForgeryInventory.Behavior> entry : structure.metaValue(StructureMeta.INVENTORIES).entrySet()) {
            entry.getValue().interfaceBlocks()
                    .forEach(blockType -> blockTypeInventoryTypeMap.put(blockType, entry.getKey()));
        }
    }

    @Override
    public void interact(Player actor, BlockLocation location) {
        Block block = location.toBlock();
        BlockType blockType = block.getType().asBlockType();
        String inventoryTypeName = blockTypeInventoryTypeMap.get(blockType);
        if (inventoryTypeName == null) {
            actor.sendMessage(Component.text("Not an inventory accessible block"));
        }
        ForgeryInventory inventory = inventory(inventoryTypeName);
        actor.sendMessage(Component.text("name=" + inventoryTypeName + ",behavior=" + inventory.behavior()));
    }

    public @NotNull ForgeryInventory inventory(String inventoryType) {
        Map<String, ForgeryInventory.Behavior> possibleInventories = placedStructure().metaValue(StructureMeta.INVENTORIES);
        ForgeryInventory.Behavior behavior = possibleInventories.get(inventoryType);
        Preconditions.checkArgument(behavior != null, "Inventory does not have inventory type: " + inventoryType);
        ForgeryInventory forgeryInventory = inventories.get(inventoryType);
        if (forgeryInventory == null) {
            forgeryInventory = new ForgeryInventory(behavior, inventoryType);
            persistencyAccess.database().insert(
                    persistencyAccess.inventoryStoredData(),
                    new InventoryStoredData.InventoryInfo(uuid(), forgeryInventory)
            );
        }
        return forgeryInventory;
    }

    public UUID uuid() {
        return this.uuid;
    }

    public void setInventories(List<ForgeryInventory> forgeryInventories) {
        forgeryInventories.forEach(
                inventory -> inventories.put(inventory.typeName(), inventory)
        );
    }
}
