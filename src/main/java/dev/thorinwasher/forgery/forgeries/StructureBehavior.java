package dev.thorinwasher.forgery.forgeries;

import com.google.common.base.Preconditions;
import dev.thorinwasher.forgery.TimeProvider;
import dev.thorinwasher.forgery.database.PersistencyAccess;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.inventory.ForgeryInventory;
import dev.thorinwasher.forgery.inventory.InventoryStoredData;
import dev.thorinwasher.forgery.structure.BlockTransform;
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
    private final long creationDate;
    private PlacedForgeryStructure structure;
    private final Map<String, ForgeryInventory> inventories = new HashMap<>();
    private final Map<BlockType, String> blockTypeInventoryTypeMap = new HashMap<>();

    public StructureBehavior(UUID blastFurnaceId, PersistencyAccess persistencyAccess, ItemAdapter itemAdapter, long creationDate) {
        this.uuid = blastFurnaceId;
        this.persistencyAccess = persistencyAccess;
        this.itemAdapter = itemAdapter;
        this.creationDate = creationDate;
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

    public InteractionResult interact(Player actor, BlockLocation location, EquipmentSlot hand) {
        Block block = location.toBlock();
        BlockType blockType = block.getType().asBlockType();
        String inventoryTypeName = blockTypeInventoryTypeMap.get(blockType);
        if (inventoryTypeName == null) {
            actor.sendMessage(Component.text("Not an inventory accessible block"));
            return InteractionResult.DEFAULT;
        }
        ForgeryInventory forgeryInventory = inventory(inventoryTypeName);
        switch (forgeryInventory.behavior().access()) {
            case INSERTABLE -> accessInsertableInventory(actor, forgeryInventory, hand);
            case OPENABLE -> openInventory(actor, forgeryInventory, hand);
        }
        return InteractionResult.DENY;
    }

    private InteractionResult accessInsertableInventory(Player actor, ForgeryInventory forgeryInventory, EquipmentSlot hand) {
        if (actor.isSneaking()) {
            if (hand == EquipmentSlot.OFF_HAND) {
                return InteractionResult.DENY;
            }
            PlayerInventory actorInventory = actor.getInventory();
            forgeryInventory.retrieveFirstAndSave()
                    .map(itemAdapter::toBukkit)
                    .ifPresent(itemStack -> {
                        if (!actorInventory.addItem(itemStack).isEmpty()) {
                            actor.getWorld().dropItemNaturally(actor.getLocation(), itemStack);
                        }
                    });
            return InteractionResult.DENY;
        }
        PlayerInventory inventory = actor.getInventory();
        ItemStack itemStack = inventory.getItem(hand);
        if (itemStack.isEmpty()) {
            return InteractionResult.DENY;
        }
        itemAdapter.toForgery(itemStack)
                .ifPresent(forgeryInventory::addItem);
        return InteractionResult.DENY;
    }

    private InteractionResult openInventory(Player actor, ForgeryInventory forgeryInventory, EquipmentSlot hand) {
        if (hand == EquipmentSlot.OFF_HAND) {
            return InteractionResult.DENY;
        }
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
            inventories.put(inventoryType, forgeryInventory);
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

    public void tickStructure() {
        tickBlocks();
        tickEntities();
        tickInventories();
    }

    private void tickEntities() {
        // TODO: work with item displays and such
    }

    private void tickInventories() {
        inventories.values()
                .stream()
                .filter(forgeryInventory -> forgeryInventory.behavior().access() == ForgeryInventory.AccessBehavior.OPENABLE)
                .filter(forgeryInventory -> !forgeryInventory.getInventory().getViewers().isEmpty())
                .forEach(inventory -> {
                    inventory.updateContentsFromInterface();
                    // TODO: Implement item transformations
                    inventory.updateInterfaceFromContents();
                });
    }

    private void tickBlocks() {
        if (!structure.origin().toLocation().isChunkLoaded()) {
            return;
        }
        List<BlockTransform> transforms = structure.structure().metaValue(StructureMeta.BLOCK_TRANSFORMS);
        if (transforms == null) {
            return;
        }
        for (BlockTransform blockTransform : transforms) {
            boolean allMatch = blockTransform.conditions().stream().allMatch(condition -> switch (condition) {
                case BlockTransform.StructureAgeCondition structureAgeCondition ->
                        structureAgeCondition.age() > this.age();
                case BlockTransform.InventoryEmptyCondition inventoryEmptyCondition -> {
                    ForgeryInventory inventory = this.inventories.get(inventoryEmptyCondition.inventoryTypeName());
                    yield inventory == null || inventory.items().isEmpty();
                }
            });
            if (allMatch) {
                placedStructure().positions()
                        .forEach(blockTransform::applyToBlock);
            } else {
                placedStructure().positions()
                        .forEach(blockTransform::deapplyToBlock);
            }
        }
    }

    private long age() {
        return Math.max(0, TimeProvider.time() - creationDate);
    }

    public long creationDate() {
        return this.creationDate;
    }

    public record InteractionResult(Event.Result useBlock, Event.Result useItem) {
        public static final InteractionResult DEFAULT = new InteractionResult(Event.Result.DEFAULT, Event.Result.DEFAULT);
        public static final InteractionResult DENY = new InteractionResult(Event.Result.DENY, Event.Result.DENY);
        public static final InteractionResult ALLOW = new InteractionResult(Event.Result.ALLOW, Event.Result.ALLOW);
    }
}
