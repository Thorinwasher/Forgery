package dev.thorinwasher.forgery;

import dev.thorinwasher.forgery.listener.BlockEventListener;
import dev.thorinwasher.forgery.structure.PlacedStructureRegistry;
import dev.thorinwasher.forgery.structure.StructureReadException;
import dev.thorinwasher.forgery.structure.StructureReader;
import dev.thorinwasher.forgery.structure.StructureRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

public class Forgery extends JavaPlugin {

    private StructureRegistry structureRegistry;
    private static Forgery instance;
    private PlacedStructureRegistry placedStructureRegistry;

    @Override
    public void onEnable() {
        instance = this;
        this.structureRegistry = new StructureRegistry();
        this.placedStructureRegistry = new PlacedStructureRegistry();
        Stream.of("blast_furnace")
                .map(string -> "structures/" + string)
                .flatMap(name -> Stream.of(name + ".schem", name + ".json"))
                .forEach(this::saveResourceIfNotExists);
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
        Bukkit.getPluginManager().registerEvents(new BlockEventListener(placedStructureRegistry, structureRegistry), this);
    }

    private void saveResourceIfNotExists(String resource) {
        if (new File(getDataFolder(), resource).exists()) {
            return;
        }
        super.saveResource(resource, false);
    }

    public static Forgery instance() {
        return instance;
    }

    public StructureRegistry structureRegistry() {
        return structureRegistry;
    }

    public PlacedStructureRegistry placedStructureRegistry() {
        return placedStructureRegistry;
    }
}
