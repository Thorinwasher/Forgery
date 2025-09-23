package dev.thorinwasher.forgery.inventory;

import dev.thorinwasher.forgery.database.PersistencyAccess;
import dev.thorinwasher.forgery.database.SqlStatements;
import dev.thorinwasher.forgery.database.StoredData;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.structure.ForgeryStructure;
import dev.thorinwasher.forgery.structure.StructureMeta;
import dev.thorinwasher.forgery.util.DecoderUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InventoryStoredData implements StoredData<InventoryStoredData.InventoryInfo, InventoryStoredData.StructureInfo> {

    private final PersistencyAccess persistencyAccess;
    private final SqlStatements statements = new SqlStatements("/database/inventory");
    private final ItemAdapter itemAdapter;

    public InventoryStoredData(PersistencyAccess persistencyAccess, ItemAdapter itemAdapter) {
        this.persistencyAccess = persistencyAccess;
        this.itemAdapter = itemAdapter;
    }

    @Override
    public List<InventoryStoredData.InventoryInfo> find(StructureInfo searchObject, Connection connection) throws SQLException {
        List<InventoryStoredData.InventoryInfo> output = new ArrayList<>();
        List<InventoryTag> invalidInventories = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.FIND))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(searchObject.structureUuid()));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int size = resultSet.getInt("inventory_size");
                String inventoryType = resultSet.getString("inventory_type");
                Map<String, ForgeryInventory.Behavior> behaviors = searchObject.structure().metaValue(StructureMeta.INVENTORIES);
                if (behaviors == null) {
                    invalidInventories.add(new InventoryTag(searchObject.structureUuid(), inventoryType));
                    continue;
                }
                ForgeryInventory.Behavior behavior = behaviors.get(inventoryType);
                if (behavior == null) {
                    invalidInventories.add(new InventoryTag(searchObject.structureUuid(), inventoryType));
                    continue;
                }
                output.add(new InventoryInfo(searchObject.structureUuid(),
                        new ForgeryInventory(
                                new ForgeryInventory.Behavior(behavior.access(), behavior.itemDisplay(), size, behavior.interfaceBlocks(), behavior.allows(), behavior.transformation()),
                                inventoryType,
                                persistencyAccess,
                                searchObject.structureUuid(),
                                itemAdapter
                        ))
                );
            }
        }
        for (InventoryInfo inventory : output) {
            persistencyAccess.inventoryContentStoredData().find(inventory, connection)
                    .stream()
                    .map(InventoryContentStoredData.ItemInfo::item)
                    .forEach(inventory.inventory()::setItem);
        }
        return output;
    }

    @Override
    public void insert(InventoryStoredData.InventoryInfo object, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.INSERT))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(object.structureUuid));
            ForgeryInventory inventory = object.inventory;
            preparedStatement.setString(2, inventory.typeName());
            preparedStatement.setInt(3, inventory.behavior().size());
            preparedStatement.execute();
        }
    }

    @Override
    public void remove(InventoryStoredData.InventoryInfo object, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.DELETE))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(object.structureUuid));
            ForgeryInventory inventory = object.inventory;
            preparedStatement.setString(2, inventory.typeName());
            preparedStatement.execute();
        }
    }


    public record InventoryInfo(UUID structureUuid, ForgeryInventory inventory) {
    }

    public record StructureInfo(UUID structureUuid, ForgeryStructure structure) {
    }

    public record InventoryTag(UUID structureUuid, String typeName) {
    }
}
