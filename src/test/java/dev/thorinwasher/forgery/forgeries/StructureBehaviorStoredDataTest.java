package dev.thorinwasher.forgery.forgeries;

import dev.thorinwasher.forgery.database.Database;
import dev.thorinwasher.forgery.database.PersistencyAccess;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.integration.IntegrationRegistry;
import dev.thorinwasher.forgery.inventory.ForgeryInventory;
import dev.thorinwasher.forgery.inventory.ForgingItem;
import dev.thorinwasher.forgery.inventory.InventoryContentStoredData;
import dev.thorinwasher.forgery.inventory.InventoryStoredData;
import dev.thorinwasher.forgery.structure.ForgeryStructure;
import dev.thorinwasher.forgery.structure.PlacedForgeryStructure;
import dev.thorinwasher.forgery.structure.StructureMeta;
import dev.thorinwasher.forgery.structure.StructureRegistry;
import dev.thorinwasher.forgery.util.ForgeryKey;
import dev.thorinwasher.forgery.vector.BlockLocation;
import dev.thorinwasher.schem.Schematic;
import org.bukkit.block.data.BlockData;
import org.joml.Matrix3d;
import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StructureBehaviorStoredDataTest {

    private Database database;
    private StructureRegistry structureRegistry;
    private ForgeryInventory.Behavior inventoryBehavior1 = new ForgeryInventory.Behavior(
            ForgeryInventory.AccessBehavior.INSERTABLE, ForgeryInventory.ItemDisplayBehavior.ABOVE,
            9, Set.of()
    );
    private ForgeryInventory.Behavior inventoryBehavior2 = new ForgeryInventory.Behavior(
            ForgeryInventory.AccessBehavior.OPENABLE, ForgeryInventory.ItemDisplayBehavior.NONE,
            18, Set.of()
    );
    private PersistencyAccess persistencyAccess;
    private ItemAdapter itemAdapter;

    @BeforeEach
    void setUp() throws IOException, SQLException {
        this.database = new Database();
        database.init(Files.createTempDirectory("forgery").toFile());
        IntegrationRegistry integrationRegistry = new IntegrationRegistry();
        integrationRegistry.initialize();
        this.itemAdapter = new ItemAdapter(integrationRegistry);
        this.structureRegistry = new StructureRegistry();
        this.persistencyAccess = new PersistencyAccess(database, structureRegistry, itemAdapter);
        persistencyAccess.initialize();
        structureRegistry.addStructure(new ForgeryStructure(
                new Schematic(new Vector3i(), new Vector3i(), new BlockData[0], new byte[0]),
                "my_structure", Map.of(
                StructureMeta.INVENTORIES, Map.of(
                        "type_1", inventoryBehavior1,
                        "type_2", inventoryBehavior2
                )
        )));
    }

    @Test
    public void test() {
        StructureBehavior structureBehavior = new StructureBehavior(UUID.randomUUID(), persistencyAccess, itemAdapter, 0);
        ForgeryStructure forgeryStructure = structureRegistry.getStructure("my_structure").get();
        structureBehavior.setStructure(new PlacedForgeryStructure(
                forgeryStructure, new Matrix3d(), new BlockLocation(0, 0, 0, UUID.randomUUID()), structureBehavior
        ));
        database.insert(persistencyAccess.behaviorStoredData(), structureBehavior);
        ForgeryInventory forgeryInventory1 = new ForgeryInventory(inventoryBehavior1, "type_1", persistencyAccess, structureBehavior.uuid(), itemAdapter);
        ForgeryInventory forgeryInventory2 = new ForgeryInventory(inventoryBehavior2, "type_2", persistencyAccess, structureBehavior.uuid(), itemAdapter);
        database.insert(persistencyAccess.inventoryStoredData(), new InventoryStoredData.InventoryInfo(
                structureBehavior.uuid(), forgeryInventory1
        ));
        database.insert(persistencyAccess.inventoryStoredData(), new InventoryStoredData.InventoryInfo(
                structureBehavior.uuid(), forgeryInventory2
        ));
        InventoryContentStoredData.ItemInfo itemInfo1 = new InventoryContentStoredData.ItemInfo(
                new ForgeryInventory.ItemRecord(0, ForgingItem.materialBased(ForgeryKey.forgery("item_1"))),
                structureBehavior.uuid(), "type_1"
        );
        InventoryContentStoredData.ItemInfo itemInfo2 = new InventoryContentStoredData.ItemInfo(
                new ForgeryInventory.ItemRecord(1, ForgingItem.materialBased(ForgeryKey.forgery("item_2"))),
                structureBehavior.uuid(), "type_2"
        );
        database.insert(persistencyAccess.inventoryContentStoredData(), itemInfo1);
        database.insert(persistencyAccess.inventoryContentStoredData(), itemInfo2);
        List<StructureBehavior> behaviors = database.find(persistencyAccess.behaviorStoredData(), structureBehavior.placedStructure().origin().worldUuid()).join();
        assertEquals(1, behaviors.size());
        StructureBehavior readStructureBehavior = behaviors.getFirst();
        assertEquals(forgeryStructure, readStructureBehavior.placedStructure().structure());
        assertEquals(structureBehavior.uuid(), readStructureBehavior.uuid());
        ForgeryInventory readInventory1 = readStructureBehavior.inventory("type_1");
        ForgeryInventory readInventory2 = readStructureBehavior.inventory("type_2");
        assertEquals(inventoryBehavior1, readInventory1.behavior());
        assertEquals(inventoryBehavior2, readInventory2.behavior());
        List<ForgeryInventory.ItemRecord> items1 = readInventory1.items();
        List<ForgeryInventory.ItemRecord> items2 = readInventory2.items();
        assertEquals(1, items1.size());
        assertEquals(1, items2.size());
        assertEquals(itemInfo1.item(), items1.getFirst());
        assertEquals(itemInfo2.item(), items2.getFirst());
    }
}