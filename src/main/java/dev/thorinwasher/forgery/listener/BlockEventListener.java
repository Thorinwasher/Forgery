package dev.thorinwasher.forgery.listener;

import dev.thorinwasher.forgery.TimeProvider;
import dev.thorinwasher.forgery.database.PersistencyAccess;
import dev.thorinwasher.forgery.forgeries.StructureBehavior;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.recipe.Recipe;
import dev.thorinwasher.forgery.structure.ForgeryStructure;
import dev.thorinwasher.forgery.structure.PlacedForgeryStructure;
import dev.thorinwasher.forgery.structure.PlacedStructureRegistry;
import dev.thorinwasher.forgery.structure.StructureRegistry;
import dev.thorinwasher.forgery.vector.BlockLocation;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record BlockEventListener(PlacedStructureRegistry placedStructureRegistry,
                                 StructureRegistry structureRegistry,
                                 PersistencyAccess persistencyAccess, ItemAdapter itemAdapter,
                                 Map<String, Recipe> recipes) implements Listener {


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        for (ForgeryStructure forgeryStructure : structureRegistry.getPossibleStructures(event.getBlockPlaced().getType())) {
            Optional<PlacedForgeryStructure> placedStructure = PlacedForgeryStructure.findValid(forgeryStructure, event.getBlockPlaced().getLocation(),
                    () -> new StructureBehavior(UUID.randomUUID(), persistencyAccess, itemAdapter, TimeProvider.time(), recipes, -1L));
            placedStructure.ifPresent(structure -> {
                event.getPlayer().sendMessage(Component.translatable("Successfully built: " + structure.structure().getName()));
                placedStructureRegistry.registerStructure(structure);
                persistencyAccess.database().insert(persistencyAccess.behaviorStoredData(), structure.behavior());
            });
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        placedStructureRegistry.getStructure(BlockLocation.fromLocation(event.getBlock().getLocation()))
                .ifPresent(structure -> {
                    event.getPlayer().sendMessage(Component.translatable("Successfully destroyed " + structure.structure().getName()));
                    placedStructureRegistry.unregisterStructure(structure);
                    persistencyAccess.database().remove(persistencyAccess.behaviorStoredData(), structure.behavior());
                });
    }
}
