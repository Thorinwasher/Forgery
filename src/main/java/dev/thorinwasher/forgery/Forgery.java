package dev.thorinwasher.forgery;

import com.google.common.base.Preconditions;
import dev.thorinwasher.forgery.api.ForgeryApi;
import dev.thorinwasher.forgery.database.Database;
import dev.thorinwasher.forgery.database.PersistencyAccess;
import dev.thorinwasher.forgery.forgeries.StructureBehavior;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.integration.IntegrationRegistry;
import dev.thorinwasher.forgery.listener.BlockEventListener;
import dev.thorinwasher.forgery.listener.PlayerEventListener;
import dev.thorinwasher.forgery.listener.WorldEventListener;
import dev.thorinwasher.forgery.structure.PlacedStructureRegistry;
import dev.thorinwasher.forgery.structure.StructureReadException;
import dev.thorinwasher.forgery.structure.StructureReader;
import dev.thorinwasher.forgery.structure.StructureRegistry;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.sql.SQLException;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Forgery extends JavaPlugin {
    public static final String NAMESPACE = "forgery";

    private StructureRegistry structureRegistry;
    private PlacedStructureRegistry placedStructureRegistry;
    private PersistencyAccess persistencyAccess;
    private IntegrationRegistry integrationRegistry;
    private ItemAdapter itemAdapter;
    private boolean loadSuccess = false;

    @Override
    public void onLoad() {
        this.integrationRegistry = new IntegrationRegistry();
        this.itemAdapter = new ItemAdapter(integrationRegistry);
        this.structureRegistry = new StructureRegistry();
        this.placedStructureRegistry = new PlacedStructureRegistry();
        Bukkit.getServicesManager().register(ForgeryApi.class, new ForgeryApiImpl(integrationRegistry, placedStructureRegistry),
                this, ServicePriority.Normal);
        this.loadSuccess = true; // Plugins always continues to enable even if it failed on load
    }

    @Override
    public void onEnable() {
        Preconditions.checkState(loadSuccess, "Failed on load");
        saveExposedResources();
        integrationRegistry.initialize();
        Database database = new Database();
        try {
            database.init(this.getDataFolder());
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
        this.persistencyAccess = new PersistencyAccess(database, structureRegistry, itemAdapter);
        persistencyAccess.initialize();
        loadStructures();
        Bukkit.getPluginManager().registerEvents(new BlockEventListener(placedStructureRegistry, structureRegistry, persistencyAccess, itemAdapter), this);
        Bukkit.getPluginManager().registerEvents(new PlayerEventListener(placedStructureRegistry), this);
        Bukkit.getPluginManager().registerEvents(new WorldEventListener(persistencyAccess, placedStructureRegistry), this);
        Bukkit.getWorlds()
                .stream()
                .map(World::getUID)
                .map(uuid -> database.find(persistencyAccess.behaviorStoredData(), uuid))
                .forEach(future ->
                        future.thenAcceptAsync(behaviors -> behaviors.stream()
                                .map(StructureBehavior::placedStructure)
                                .forEach(placedStructureRegistry::registerStructure)
                        )
                );
    }

    private void loadStructures() {
        Stream.of(new File(this.getDataFolder(), "structures").listFiles())
                .filter(file -> file.getName().endsWith(".json"))
                .map(File::toPath)
                .flatMap(path -> {
                    try {
                        return Stream.of(StructureReader.fromJson(path));
                    } catch (IOException | StructureReadException e) {
                        getLogger().severe("Could not load structure: " + path);
                        e.printStackTrace();
                        return Stream.empty();
                    }
                })
                .forEach(structureRegistry::addStructure);
    }

    private void saveResourceIfNotExists(String resource) {
        if (new File(getDataFolder(), resource).exists()) {
            return;
        }
        super.saveResource(resource, false);
    }

    public static Key key(String key) {
        return Key.key(NAMESPACE, key);
    }

    private void saveExposedResources() {
        try (InputStream inputStream = Forgery.class.getResourceAsStream("/exposed_resources.zip")) {
            if (inputStream == null) {
                throw new IOException("Could not find internal resource: /exposed_resources.zip");
            }
            try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                ZipEntry entry = zipInputStream.getNextEntry();
                while (entry != null) {
                    ZipEntry current = entry;
                    if (current.isDirectory()) {
                        entry = zipInputStream.getNextEntry();
                        continue;
                    }
                    File destination = new File(this.getDataFolder(), current.getName());
                    if (destination.exists()) {
                        entry = zipInputStream.getNextEntry();
                        continue;
                    }
                    File destinationFolder = destination.getParentFile();
                    if (!destinationFolder.exists() && !destination.getParentFile().mkdirs()) {
                        throw new IOException("Could not make dirs at: " + destinationFolder);
                    }
                    if (!destination.createNewFile()) {
                        throw new IOException("could not make file: " + destination);
                    }
                    try (OutputStream outputStream = new FileOutputStream(destination)) {
                        zipInputStream.transferTo(outputStream);
                    }
                    entry = zipInputStream.getNextEntry();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
