package dev.thorinwasher.forgery.forgeries;

import com.google.common.base.Preconditions;
import dev.thorinwasher.forgery.database.PersistencyAccess;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.inventory.ForgeryInventory;
import dev.thorinwasher.forgery.inventory.InventoryStoredData;
import dev.thorinwasher.forgery.structure.PlacedForgeryStructure;
import dev.thorinwasher.forgery.structure.StructureMeta;
import dev.thorinwasher.forgery.vector.BlockLocation;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StructureBehavior {
    private final UUID uuid;
    private final PersistencyAccess persistencyAccess;
    private final ItemAdapter itemAdapter;
    private PlacedForgeryStructure structure;
    private Map<String, ForgeryInventory> inventories = new HashMap<>();
    private Map<BlockType, String> blockTypeInventoryTypeMap = new HashMap<>();

    public StructureBehavior(UUID blastFurnaceId, PersistencyAccess persistencyAccess, ItemAdapter itemAdapter) {
        this.uuid = blastFurnaceId;
        this.persistencyAccess = persistencyAccess;
        this.itemAdapter = itemAdapter;
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

    public InteractionResult interact(Player actor, BlockLocation location) {
        Block block = location.toBlock();
        BlockType blockType = block.getType().asBlockType();
        String inventoryTypeName = blockTypeInventoryTypeMap.get(blockType);
        if (inventoryTypeName == null) {
            actor.sendMessage(Component.text("Not an inventory accessible block"));
            return InteractionResult.DEFAULT;
        }
        ForgeryInventory forgeryInventory = inventory(inventoryTypeName);
        return switch (forgeryInventory.behavior().access()) {
            case INSERTABLE -> accessInsertableInventory(actor, forgeryInventory);
            case OPENABLE -> openInventory(actor, forgeryInventory);
        };
    }

    private InteractionResult accessInsertableInventory(Player actor, ForgeryInventory forgeryInventory) {
        if (actor.isSneaking()) {
            PlayerInventory actorInventory = actor.getInventory();
            forgeryInventory.retrieveFirstAndSave()
                    .map(itemAdapter::toBukkit)
                    .ifPresent(itemStack -> {
                        if (actorInventory.addItem(itemStack).isEmpty()) {
                            actor.getWorld().dropItemNaturally(actor.getLocation(), itemStack);
                        }
                    });
            return InteractionResult.DENY;
        }
        List<EquipmentSlot> equipmentSlots = List.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
        PlayerInventory inventory = actor.getInventory();
        for (EquipmentSlot equipmentSlot : equipmentSlots) {
            ItemStack itemStack = inventory.getItem(equipmentSlot);
            if (itemStack.isEmpty()) {
                continue;
            }
            itemAdapter.toForgery(itemStack)
                    .ifPresent(forgeryInventory::addItem);
            break;
        }
        return InteractionResult.DENY;
    }

    private InteractionResult openInventory(Player actor, ForgeryInventory forgeryInventory) {
        if (actor.isSneaking()) {
            return InteractionResult.DEFAULT;
        }
        actor.openInventory(forgeryInventory.getInventory());
        return InteractionResult.DENY;
    }

    public @NotNull ForgeryInventory inventory(String inventoryType) {
        Map<String, ForgeryInventory.Behavior> possibleInventories = placedStructure().metaValue(StructureMeta.INVENTORIES);
        ForgeryInventory.Behavior behavior = possibleInventories.get(inventoryType);
        Preconditions.checkArgument(behavior != null, "Inventory does not have inventory type: " + inventoryType);
        ForgeryInventory forgeryInventory = inventories.get(inventoryType);
        if (forgeryInventory == null) {
            forgeryInventory = new ForgeryInventory(behavior, inventoryType, persistencyAccess, uuid, itemAdapter);
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

    public record InteractionResult(Event.Result useBlock, Event.Result useItem) {
        public static final InteractionResult DEFAULT = new InteractionResult(Event.Result.DEFAULT, Event.Result.DEFAULT);
        public static final InteractionResult DENY = new InteractionResult(Event.Result.DENY, Event.Result.DENY);
        public static final InteractionResult ALLOW = new InteractionResult(Event.Result.ALLOW, Event.Result.ALLOW);
    }
}
