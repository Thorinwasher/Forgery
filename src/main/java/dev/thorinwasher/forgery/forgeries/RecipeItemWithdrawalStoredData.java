package dev.thorinwasher.forgery.forgeries;

import dev.thorinwasher.forgery.database.SqlStatements;
import dev.thorinwasher.forgery.database.StoredData;
import dev.thorinwasher.forgery.util.DecoderUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecipeItemWithdrawalStoredData implements StoredData<RecipeItemWithdrawal, UUID> {

    private final SqlStatements statements = new SqlStatements("/database/recipe_item_withdrawal");

    @Override
    public List<RecipeItemWithdrawal> find(UUID searchObject, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.FIND))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(searchObject));
            List<RecipeItemWithdrawal> output = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                output.add(new RecipeItemWithdrawal(
                        resultSet.getString("inventory_type"),
                        searchObject,
                        resultSet.getInt("withdrawal_amount")
                ));
            }
            return output;
        }
    }

    @Override
    public void insert(RecipeItemWithdrawal object, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.INSERT))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(object.structureUuid()));
            preparedStatement.setInt(2, object.amount());
            preparedStatement.setString(3, object.inventoryType());
            preparedStatement.execute();
        }
    }

    @Override
    public void remove(RecipeItemWithdrawal object, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.DELETE))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(object.structureUuid()));
            preparedStatement.setString(2, object.inventoryType());
            preparedStatement.execute();
        }
    }
}
