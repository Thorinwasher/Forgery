package dev.thorinwasher.forgery.recipe;

import dev.thorinwasher.forgery.database.SqlStatements;
import dev.thorinwasher.forgery.database.UpdateableStoredData;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemStoredData implements UpdateableStoredData<ItemReference, Void> {

    private final SqlStatements statements = new SqlStatements("/database/item");

    @Override
    public void update(ItemReference object, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.UPDATE))) {
            preparedStatement.setBytes(1, object.itemStack().serializeAsBytes());
            preparedStatement.setString(2, object.key().value());
            preparedStatement.execute();
        }
    }

    @Override
    public List<ItemReference> find(Void searchObject, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.FIND))) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<ItemReference> output = new ArrayList<>();
            while (resultSet.next()) {
                output.add(
                        new ItemReference(
                                resultSet.getString("item_key"),
                                ItemStack.deserializeBytes(resultSet.getBytes("item_data"))
                        )
                );
            }
            return output;
        }
    }

    @Override
    public void insert(ItemReference object, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.INSERT))) {
            preparedStatement.setString(1, object.key().value());
            preparedStatement.setBytes(2, object.itemStack().serializeAsBytes());
            preparedStatement.execute();
        }
    }

    @Override
    public void remove(ItemReference object, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.DELETE))) {
            preparedStatement.setString(1, object.key().value());
            preparedStatement.execute();
        }
    }
}
