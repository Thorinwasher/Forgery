package dev.thorinwasher.forgery.database;

import dev.thorinwasher.forgery.forgeries.StructureBehaviorStoredData;
import dev.thorinwasher.forgery.forgeries.StructureStateStoredData;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.forging.ToolInputStoredData;
import dev.thorinwasher.forgery.inventory.InventoryContentStoredData;
import dev.thorinwasher.forgery.inventory.InventoryStoredData;
import dev.thorinwasher.forgery.recipe.ItemStoredData;
import dev.thorinwasher.forgery.recipe.Recipe;
import dev.thorinwasher.forgery.structure.StructureRegistry;

import java.util.Map;
import java.util.function.Supplier;

public final class PersistencyAccess {
    private final Database database;
    private final StructureRegistry structureRegistry;
    private final ItemAdapter itemAdapter;
    private final Supplier<Map<String, Recipe>> recipes;
    private StructureBehaviorStoredData behaviorStoredData;
    private InventoryStoredData inventoryStoredData;
    private InventoryContentStoredData inventoryContentStoredData;
    private StructureStateStoredData structureStateStoredData;
    private ItemStoredData itemStoredData;
    private ToolInputStoredData toolInputStoredData;

    public PersistencyAccess(Database database, StructureRegistry structureRegistry, ItemAdapter itemAdapter, Supplier<Map<String, Recipe>> recipes) {
        this.database = database;
        this.structureRegistry = structureRegistry;
        this.itemAdapter = itemAdapter;
        this.recipes = recipes;
    }

    public void initialize() {
        this.inventoryContentStoredData = new InventoryContentStoredData();
        this.inventoryStoredData = new InventoryStoredData(this, itemAdapter);
        this.structureStateStoredData = new StructureStateStoredData();
        this.itemStoredData = new ItemStoredData();
        this.toolInputStoredData = new ToolInputStoredData();
    }

    public Database database() {
        return database;
    }

    public StructureBehaviorStoredData behaviorStoredData() {
        if (behaviorStoredData == null) {
            this.behaviorStoredData = new StructureBehaviorStoredData(structureRegistry, this, itemAdapter, recipes.get());
        }
        return behaviorStoredData;
    }

    public InventoryStoredData inventoryStoredData() {
        return inventoryStoredData;
    }

    public InventoryContentStoredData inventoryContentStoredData() {
        return inventoryContentStoredData;
    }

    public StructureStateStoredData structureStateStoredData() {
        return structureStateStoredData;
    }

    public ItemStoredData itemStoredData() {
        return itemStoredData;
    }

    public ToolInputStoredData toolInputStoredData() {
        return toolInputStoredData;
    }

}
