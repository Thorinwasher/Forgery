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

public class StructureStateStoredData implements StoredData<StructureStateStoredData.StructureStateData, UUID> {

    private SqlStatements statements = new SqlStatements("/database/structure_state");

    @Override
    public List<StructureStateData> find(UUID searchObject, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.FIND))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(searchObject));
            ResultSet resultSet = preparedStatement.executeQuery();
            List<StructureStateData> output = new ArrayList<>();
            while (resultSet.next()) {
                output.add(new StructureStateData(searchObject,
                        new StructureStateChange(
                                resultSet.getString("state"),
                                resultSet.getLong("time_stamp"),
                                StructureStateChange.Action.valueOf(resultSet.getString("change"))
                        )
                ));
            }
            return output;
        }
    }

    @Override
    public void insert(StructureStateData object, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.INSERT))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(object.structureUuid()));
            StructureStateChange change = object.stateChange();
            preparedStatement.setString(2, change.stateName());
            preparedStatement.setLong(3, change.timePoint());
            preparedStatement.setString(4, change.action().name());
            preparedStatement.execute();
        }
    }

    @Override
    public void remove(StructureStateData object, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.DELETE))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(object.structureUuid()));
            preparedStatement.execute();
        }
    }

    public record StructureStateData(UUID structureUuid, StructureStateChange stateChange) {

    }
}
