package dev.thorinwasher.forgery.forgeries;

import com.google.common.base.Preconditions;
import dev.thorinwasher.forgery.TimeProvider;
import dev.thorinwasher.forgery.database.PersistencyAccess;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.inventory.ForgeryInventory;
import dev.thorinwasher.forgery.inventory.InventoryStoredData;
import dev.thorinwasher.forgery.recipe.Recipe;
import dev.thorinwasher.forgery.recipe.RecipeProcedureEvaluator;
import dev.thorinwasher.forgery.structure.BlockTransform;
import dev.thorinwasher.forgery.structure.Condition;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class StructureBehavior {
    private final UUID uuid;
    private final PersistencyAccess persistencyAccess;
    private final ItemAdapter itemAdapter;
    private final long creationDate;
    private final Map<String, Recipe> recipes;
    private PlacedForgeryStructure structure;
    private final Map<String, ForgeryInventory> inventories = new HashMap<>();
    private final Map<BlockType, String> blockTypeInventoryTypeMap = new HashMap<>();
    private Set<String> states;
    private Map<String, List<StructureStateChange>> stateHistory;
    private @Nullable ItemStack recipeOutput = null;
    private long lastEvaluated = -1;
    private static final long EVALUATION_DELAY = 200;
    private long processStart;

    public StructureBehavior(UUID blastFurnaceId, PersistencyAccess persistencyAccess, ItemAdapter itemAdapter, long creationDate, Map<String, Recipe> recipes, long processStart) {
        this.uuid = blastFurnaceId;
        this.persistencyAccess = persistencyAccess;
        this.itemAdapter = itemAdapter;
        this.creationDate = creationDate;
        this.states = Set.of();
        this.stateHistory = new HashMap<>();
        this.recipes = recipes;
        this.processStart = processStart;
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
        return switch (forgeryInventory.behavior().access()) {
            case INSERTABLE -> accessInsertableInventory(actor, forgeryInventory, hand);
            case OPENABLE -> openInventory(actor, forgeryInventory, hand);
        };
    }

    private InteractionResult accessInsertableInventory(Player actor, ForgeryInventory forgeryInventory, EquipmentSlot hand) {
        if (actor.isSneaking()) {
            if (hand == EquipmentSlot.OFF_HAND) {
                return InteractionResult.DENY;
            }
            if (lastEvaluated + EVALUATION_DELAY < TimeProvider.time()
                    && forgeryInventory.typeName().equalsIgnoreCase(structure.metaValue(StructureMeta.OUTPUT_INVENTORY))
                    && !forgeryInventory.items().isEmpty()) {
                recipeOutput = RecipeProcedureEvaluator.findRecipeResult(
                        stateHistory,
                        forgeryInventory.items().stream()
                                .map(ForgeryInventory.ItemRecord::forgeryItem)
                                .toList(),
                        processStart,
                        recipes.values(),
                        itemAdapter
                );
            }
            if (recipeOutput != null && forgeryInventory.typeName().equalsIgnoreCase(structure.metaValue(StructureMeta.OUTPUT_INVENTORY))) {
                inventories.values().forEach(ForgeryInventory::clear);
                if (!actor.getInventory().addItem(recipeOutput).isEmpty()) {
                    actor.getWorld().dropItemNaturally(actor.getLocation(), recipeOutput);
                }
                recipeOutput = null;
                stateHistory.clear();
                persistencyAccess.database().remove(
                        persistencyAccess.structureStateStoredData(),
                        new StructureStateStoredData.StructureStateData(this.uuid, null)
                );
                resetProcessStart();
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
            if (forgeryInventory.items().isEmpty() && forgeryInventory.typeName().equalsIgnoreCase(structure.metaValue(StructureMeta.OUTPUT_INVENTORY))) {
                resetProcessStart();
            }
            return InteractionResult.DENY;
        }
        PlayerInventory inventory = actor.getInventory();
        ItemStack itemStack = inventory.getItem(hand);
        if (itemStack.isEmpty()) {
            return InteractionResult.DENY;
        }
        itemStack.setAmount(itemStack.getAmount() - 1);
        itemAdapter.toForgery(itemStack)
                .ifPresent(item -> {
                    if (forgeryInventory.items().isEmpty()) {
                        startProcess();
                    }
                    forgeryInventory.addItem(item);
                });
        return InteractionResult.DENY;
    }

    private void resetProcessStart() {
        processStart = -1;
        persistencyAccess.database().update(persistencyAccess.behaviorStoredData(), this);
    }

    private void startProcess() {
        processStart = TimeProvider.time();
        persistencyAccess.database().update(persistencyAccess.behaviorStoredData(), this);
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
        Set<String> previousStates = states;
        this.states = readStates();
        if (!states.containsAll(previousStates) || !previousStates.containsAll(states)) {
            List<StructureStateChange> difference = computeStateDifference(previousStates, states);
            difference.forEach(structureStateRecord ->
                    stateHistory.computeIfAbsent(structureStateRecord.stateName(), ignored -> new ArrayList<>())
                            .add(structureStateRecord)
            );
            difference.forEach(change ->
                    persistencyAccess.database().insert(
                            persistencyAccess.structureStateStoredData(),
                            new StructureStateStoredData.StructureStateData(this.uuid(), change)
                    ));
        }
        tickBlocks();
        tickEntities();
        tickInventories();
    }

    private List<StructureStateChange> computeStateDifference(Set<String> previousState, Set<String> currentState) {
        Set<String> all = new HashSet<>(previousState);
        all.addAll(currentState);
        List<StructureStateChange> output = new ArrayList<>();
        for (String value : all) {
            if (previousState.contains(value) && currentState.contains(value)) {
                continue;
            }
            if (previousState.contains(value)) {
                output.add(new StructureStateChange(value, TimeProvider.time(), StructureStateChange.Action.REMOVE));
            } else {
                output.add(new StructureStateChange(value, TimeProvider.time(), StructureStateChange.Action.ADD));
            }
        }
        return output;
    }

    private Set<String> readStates() {
        Map<String, List<Condition>> states = structure.metaValue(StructureMeta.STATE);
        if (states == null) {
            return Set.of();
        }
        return states.entrySet().stream()
                .filter(entry -> conditionsAreFilled(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    private boolean conditionsAreFilled(List<Condition> conditions) {
        return conditions.stream().allMatch(condition -> switch (condition) {
            case Condition.StructureAgeCondition structureAgeCondition -> structureAgeCondition.age() > this.age();
            case Condition.InventoryEmptyCondition inventoryEmptyCondition -> {
                ForgeryInventory inventory = this.inventories.get(inventoryEmptyCondition.inventoryTypeName());
                yield inventory == null || inventory.items().isEmpty();
            }
            case Condition.InvertedCondition invertedCondition ->
                    !conditionsAreFilled(List.of(invertedCondition.condition()));
        });
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
            if (states.contains(blockTransform.toState())) {
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

    public void setStateHistory(List<StructureStateChange> changes) {
        this.stateHistory = new HashMap<>();
        for (StructureStateChange change : changes) {
            stateHistory.computeIfAbsent(change.stateName(), ignored -> new ArrayList<>())
                    .add(change);
        }
        this.states = stateHistory.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty() && entry.getValue().getLast().action() == StructureStateChange.Action.ADD)
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    public long processStart() {
        return this.processStart;
    }

    public record InteractionResult(Event.Result useBlock, Event.Result useItem) {
        public static final InteractionResult DEFAULT = new InteractionResult(Event.Result.DEFAULT, Event.Result.DEFAULT);
        public static final InteractionResult DENY = new InteractionResult(Event.Result.DENY, Event.Result.DENY);
        public static final InteractionResult ALLOW = new InteractionResult(Event.Result.ALLOW, Event.Result.ALLOW);
    }
}
