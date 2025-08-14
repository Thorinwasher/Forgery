package dev.thorinwasher.forgery.inventory;

import com.google.gson.JsonParser;
import dev.thorinwasher.forgery.database.SqlStatements;
import dev.thorinwasher.forgery.database.UpdateableStoredData;
import dev.thorinwasher.forgery.util.DecoderUtil;
import dev.thorinwasher.forgery.util.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InventoryContentStoredData implements UpdateableStoredData<InventoryContentStoredData.ItemInfo, InventoryStoredData.InventoryInfo> {


    private final SqlStatements statements = new SqlStatements("/database/inventory_content");

    @Override
    public void update(InventoryContentStoredData.ItemInfo object, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.UPDATE))) {
            preparedStatement.setString(1, object.item().forgeryItem().asJson().toString());
            preparedStatement.setBytes(2, DecoderUtil.asBytes(object.structure()));
            preparedStatement.setString(3, object.inventoryType());
            preparedStatement.setInt(4, object.item().pos());
            preparedStatement.execute();
        }
    }

    @Override
    public List<InventoryContentStoredData.ItemInfo> find(InventoryStoredData.InventoryInfo searchObject, Connection connection) throws SQLException {
        List<ItemInfo> output = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.FIND))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(searchObject.structureUuid()));
            preparedStatement.setString(2, searchObject.inventory().typeName());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int pos = resultSet.getInt("pos");
                ForgingItem.fromJson(JsonParser.parseString(resultSet.getString("item_content")))
                        .map(forgeryItem -> new ForgeryInventory.ItemRecord(
                                pos,
                                forgeryItem
                        )).ifPresentOrElse(item ->
                                        output.add(new ItemInfo(
                                                item,
                                                searchObject.structureUuid(),
                                                searchObject.inventory().typeName()
                                        )),
                                () -> Logger.logInfo("Invalid item content in: " + searchObject)
                        );
                ;
            }
        }
        return output;
    }

    @Override
    public void insert(InventoryContentStoredData.ItemInfo object, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.INSERT))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(object.structure()));
            preparedStatement.setInt(2, object.item().pos());
            preparedStatement.setString(3, object.item().forgeryItem().asJson().toString());
            preparedStatement.setString(4, object.inventoryType());
            preparedStatement.execute();
        }
    }

    @Override
    public void remove(InventoryContentStoredData.ItemInfo object, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.DELETE))) {
            preparedStatement.setString(1, object.inventoryType());
            preparedStatement.setBytes(2, DecoderUtil.asBytes(object.structure()));
            preparedStatement.setInt(3, object.item().pos());
            preparedStatement.execute();
        }
    }

    public record ItemInfo(ForgeryInventory.ItemRecord item, UUID structure, String inventoryType) {
    }
}
