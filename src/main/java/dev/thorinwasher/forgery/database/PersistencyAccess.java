package dev.thorinwasher.forgery.database;

import dev.thorinwasher.forgery.forgeries.StructureBehaviorStoredData;
import dev.thorinwasher.forgery.forgeries.StructureStateStoredData;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.inventory.InventoryContentStoredData;
import dev.thorinwasher.forgery.inventory.InventoryStoredData;
import dev.thorinwasher.forgery.recipe.ItemStoredData;
import dev.thorinwasher.forgery.structure.StructureRegistry;

public final class PersistencyAccess {
    private final Database database;
    private final StructureRegistry structureRegistry;
    private final ItemAdapter itemAdapter;
    private StructureBehaviorStoredData behaviorStoredData;
    private InventoryStoredData inventoryStoredData;
    private InventoryContentStoredData inventoryContentStoredData;
    private StructureStateStoredData structureStateStoredData;
    private ItemStoredData itemStoredData;

    public PersistencyAccess(Database database, StructureRegistry structureRegistry, ItemAdapter itemAdapter) {
        this.database = database;
        this.structureRegistry = structureRegistry;
        this.itemAdapter = itemAdapter;
    }

    public void initialize() {
        this.inventoryContentStoredData = new InventoryContentStoredData();
        this.inventoryStoredData = new InventoryStoredData(this, itemAdapter);
        this.behaviorStoredData = new StructureBehaviorStoredData(structureRegistry, this, itemAdapter);
        this.structureStateStoredData = new StructureStateStoredData();
        this.itemStoredData = new ItemStoredData();
    }

    public Database database() {
        return database;
    }

    public StructureBehaviorStoredData behaviorStoredData() {
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

}
