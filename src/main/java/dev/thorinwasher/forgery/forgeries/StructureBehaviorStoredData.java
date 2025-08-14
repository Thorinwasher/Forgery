package dev.thorinwasher.forgery.forgeries;

import dev.thorinwasher.forgery.database.PersistencyAccess;
import dev.thorinwasher.forgery.database.SqlStatements;
import dev.thorinwasher.forgery.database.StoredData;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.inventory.InventoryStoredData;
import dev.thorinwasher.forgery.structure.PlacedForgeryStructure;
import dev.thorinwasher.forgery.structure.StructureRegistry;
import dev.thorinwasher.forgery.util.DecoderUtil;
import dev.thorinwasher.forgery.util.Logger;
import dev.thorinwasher.forgery.vector.BlockLocation;
import org.joml.Matrix3d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StructureBehaviorStoredData implements StoredData<StructureBehavior, UUID> {

    private final SqlStatements statements = new SqlStatements("/database/structure");
    private static final String BLAST_FURNACE = "blast_furnace";
    private final StructureRegistry registry;
    private final PersistencyAccess persistencyAccess;
    private final ItemAdapter itemAdapter;

    public StructureBehaviorStoredData(StructureRegistry registry, PersistencyAccess persistencyAccess, ItemAdapter itemAdapter) {
        this.registry = registry;
        this.persistencyAccess = persistencyAccess;
        this.itemAdapter = itemAdapter;
    }

    @Override
    public List<StructureBehavior> find(UUID searchObject, Connection connection) throws SQLException {
        List<StructureBehavior> output = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.FIND))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(searchObject));
            preparedStatement.setString(2, BLAST_FURNACE);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int x = resultSet.getInt("origin_x");
                int y = resultSet.getInt("origin_y");
                int z = resultSet.getInt("origin_z");
                BlockLocation location = new BlockLocation(x, y, z, searchObject);
                Matrix3d transformation = DecoderUtil.asTransformation(resultSet.getInt("transformation"));
                String schematic = resultSet.getString("schematic");
                UUID blastFurnaceId = DecoderUtil.asUuid(resultSet.getBytes("uuid"));
                StructureBehavior blastFurnace = new StructureBehavior(blastFurnaceId, persistencyAccess, itemAdapter);
                registry.getStructure(schematic)
                        .map(structure -> new PlacedForgeryStructure(structure, transformation, location, blastFurnace))
                        .ifPresentOrElse(structure -> {
                            blastFurnace.setStructure(structure);
                            output.add(blastFurnace);
                        }, () -> Logger.logWarn("Could not find structure: " + schematic));
            }
        }
        for (StructureBehavior blastFurnace : output) {
            blastFurnace.setInventories(persistencyAccess.inventoryStoredData().find(
                                    new InventoryStoredData.StructureInfo(blastFurnace.uuid(), blastFurnace.placedStructure().structure()), connection
                            )
                            .stream()
                            .map(InventoryStoredData.InventoryInfo::inventory)
                            .toList()
            );
        }
        return output;
    }

    @Override
    public void insert(StructureBehavior object, Connection connection) throws SQLException {
        PlacedForgeryStructure structure = object.placedStructure();
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.INSERT))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(object.uuid()));
            BlockLocation origin = structure.origin();
            preparedStatement.setBytes(2, DecoderUtil.asBytes(origin.worldUuid()));
            preparedStatement.setInt(3, origin.x());
            preparedStatement.setInt(4, origin.y());
            preparedStatement.setInt(5, origin.z());
            preparedStatement.setInt(6, DecoderUtil.asInteger(structure.transformation()));
            preparedStatement.setString(7, structure.structure().getName());
            preparedStatement.setString(8, BLAST_FURNACE);
            preparedStatement.execute();
        }
    }

    @Override
    public void remove(StructureBehavior object, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.DELETE))) {
            preparedStatement.setBytes(1, DecoderUtil.asBytes(object.uuid()));
            preparedStatement.execute();
        }
    }
}
