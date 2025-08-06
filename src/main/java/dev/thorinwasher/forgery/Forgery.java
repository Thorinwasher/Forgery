package dev.thorinwasher.forgery;

import dev.thorinwasher.forgery.database.Database;
import dev.thorinwasher.forgery.listener.BlockEventListener;
import dev.thorinwasher.forgery.listener.PlayerEventListener;
import dev.thorinwasher.forgery.structure.PlacedStructureRegistry;
import dev.thorinwasher.forgery.structure.StructureReadException;
import dev.thorinwasher.forgery.structure.StructureReader;
import dev.thorinwasher.forgery.structure.StructureRegistry;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
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
    private Database database;

    @Override
    public void onEnable() {
        saveExposedResources();
        this.structureRegistry = new StructureRegistry();
        this.placedStructureRegistry = new PlacedStructureRegistry();
        this.database = new Database();
        try {
            database.init(this.getDataFolder());
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
        loadStructures();
        Bukkit.getPluginManager().registerEvents(new BlockEventListener(placedStructureRegistry, structureRegistry, database), this);
        Bukkit.getPluginManager().registerEvents(new PlayerEventListener(placedStructureRegistry), this);
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
