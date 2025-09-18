package dev.thorinwasher.forgery;

import com.google.common.base.Preconditions;
import dev.thorinwasher.forgery.api.ForgeryApi;
import dev.thorinwasher.forgery.command.ForgeryCommand;
import dev.thorinwasher.forgery.database.Database;
import dev.thorinwasher.forgery.database.PersistencyAccess;
import dev.thorinwasher.forgery.forgeries.StructureBehavior;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.integration.IntegrationRegistry;
import dev.thorinwasher.forgery.listener.BlockEventListener;
import dev.thorinwasher.forgery.listener.PlayerEventListener;
import dev.thorinwasher.forgery.listener.WorldEventListener;
import dev.thorinwasher.forgery.recipe.ItemReference;
import dev.thorinwasher.forgery.recipe.Recipe;
import dev.thorinwasher.forgery.recipe.RecipeResult;
import dev.thorinwasher.forgery.serialize.RecipeResultSerializer;
import dev.thorinwasher.forgery.serialize.Serialize;
import dev.thorinwasher.forgery.structure.*;
import io.leangen.geantyref.TypeFactory;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.*;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;
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
    private Map<Key, ItemReference> itemReferences;
    private boolean loadSuccess = false;
    private Map<String, Recipe> recipes;

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
        this.persistencyAccess = new PersistencyAccess(database, structureRegistry, itemAdapter, () -> Preconditions.checkNotNull(recipes));
        persistencyAccess.initialize();
        loadStructures();
        itemReferences = database.find(persistencyAccess.itemStoredData(), null).join()
                .stream()
                .collect(Collectors.toMap(ItemReference::getKey, itemReference -> itemReference));
        saveItemReferenceIfNotExists(new ItemReference("diamond_sword", new ItemStack(Material.DIAMOND_SWORD)));
        ItemStack itemStack = new ItemStack(Material.IRON_INGOT);
        itemStack.setData(DataComponentTypes.CUSTOM_NAME, Component.text("Pig Iron Ingot"));
        saveItemReferenceIfNotExists(new ItemReference("pig_iron", itemStack));
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, new ForgeryCommand(itemReferences, persistencyAccess)::register);
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, this::tickStructures, 1, 1);
        this.recipes = loadRecipes();
        Preconditions.checkState(recipes != null, "Could not deserialize recipes section");
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

        Bukkit.getPluginManager().registerEvents(new BlockEventListener(placedStructureRegistry, structureRegistry, persistencyAccess, itemAdapter, recipes), this);
        Bukkit.getPluginManager().registerEvents(new PlayerEventListener(placedStructureRegistry), this);
        Bukkit.getPluginManager().registerEvents(new WorldEventListener(persistencyAccess, placedStructureRegistry), this);
    }

    private void saveItemReferenceIfNotExists(ItemReference itemReference) {
        if (itemReferences.containsKey(itemReference.getKey())) {
            itemReferences.put(itemReference.getKey(), itemReference);
            persistencyAccess.database().insert(persistencyAccess.itemStoredData(), itemReference);
        }
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

    public static NamespacedKey key(String key) {
        return new NamespacedKey(NAMESPACE, key);
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

    private void tickStructures(ScheduledTask task) {
        placedStructureRegistry.getAllStream()
                .map(PlacedForgeryStructure::behavior)
                .forEach(StructureBehavior::tickStructure);
    }

    private Map<String, Recipe> loadRecipes() {
        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .defaultOptions(opts -> opts.serializers(builder -> {
                                Serialize.registerSerializers(builder);
                                builder.register(RecipeResult.class, new RecipeResultSerializer(itemReferences));
                            }
                    ))
                    .file(new File(getDataFolder(), "recipes.yml"))
                    .build();
            return (Map<String, Recipe>) loader.load().get(TypeFactory.parameterizedClass(Map.class, String.class, Recipe.class));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
