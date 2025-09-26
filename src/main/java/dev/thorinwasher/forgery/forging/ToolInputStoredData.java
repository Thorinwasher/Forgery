package dev.thorinwasher.forgery.forging;

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

public class ToolInputStoredData implements StoredData<ToolInputStoredData.LinkedToolInput, UUID> {

    private SqlStatements statements = new SqlStatements("/database/tool_input");

    @Override
    public List<LinkedToolInput> find(UUID searchObject, Connection connection) throws SQLException {
        List<LinkedToolInput> toolInputs = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.FIND))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(searchObject));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                toolInputs.add(
                        new LinkedToolInput(searchObject, new ToolInput(
                                resultSet.getString("tool"),
                                resultSet.getLong("time_stamp")
                        ))
                );
            }
        }
        return toolInputs;
    }

    @Override
    public void insert(LinkedToolInput object, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.INSERT))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(object.structureUuid()));
            preparedStatement.setString(2, object.toolInput().tool());
            preparedStatement.setLong(3, object.toolInput().timePoint());
            preparedStatement.execute();
        }
    }

    @Override
    public void remove(LinkedToolInput object, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.DELETE))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(object.structureUuid()));
            preparedStatement.execute();
        }
    }

    public record LinkedToolInput(UUID structureUuid, ToolInput toolInput) {
    }
}
