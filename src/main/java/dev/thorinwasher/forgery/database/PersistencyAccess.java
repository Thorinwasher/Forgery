package dev.thorinwasher.forgery.database;

import dev.thorinwasher.forgery.forgeries.StructureBehaviorStoredData;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.inventory.InventoryContentStoredData;
import dev.thorinwasher.forgery.inventory.InventoryStoredData;
import dev.thorinwasher.forgery.structure.StructureRegistry;

import java.util.Objects;

public final class PersistencyAccess {
    private final Database database;
    private final StructureRegistry structureRegistry;
    private final ItemAdapter itemAdapter;
    private StructureBehaviorStoredData behaviorStoredData;
    private InventoryStoredData inventoryStoredData;
    private InventoryContentStoredData inventoryContentStoredData;

    public PersistencyAccess(Database database, StructureRegistry structureRegistry, ItemAdapter itemAdapter) {
        this.database = database;
        this.structureRegistry = structureRegistry;
        this.itemAdapter = itemAdapter;
    }

    public void initialize() {
        this.inventoryContentStoredData = new InventoryContentStoredData();
        this.inventoryStoredData = new InventoryStoredData(this, itemAdapter);
        this.behaviorStoredData = new StructureBehaviorStoredData(structureRegistry, this, itemAdapter);
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PersistencyAccess) obj;
        return Objects.equals(this.database, that.database) &&
                Objects.equals(this.behaviorStoredData, that.behaviorStoredData) &&
                Objects.equals(this.inventoryStoredData, that.inventoryStoredData) &&
                Objects.equals(this.inventoryContentStoredData, that.inventoryContentStoredData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(database, behaviorStoredData, inventoryStoredData, inventoryContentStoredData);
    }

    @Override
    public String toString() {
        return "PersistencyAccess[" +
                "database=" + database + ", " +
                "behaviorStoredData=" + behaviorStoredData + ", " +
                "inventoryStoredData=" + inventoryStoredData + ", " +
                "inventoryContentStoredData=" + inventoryContentStoredData + ']';
    }

}
