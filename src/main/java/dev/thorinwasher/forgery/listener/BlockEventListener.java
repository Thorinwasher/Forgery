package dev.thorinwasher.forgery.listener;

import dev.thorinwasher.forgery.ForgeryRegistry;
import dev.thorinwasher.forgery.forgeries.StructureBehavior;
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

import java.util.Optional;
import java.util.UUID;

public record BlockEventListener(PlacedStructureRegistry placedStructureRegistry,
                                 StructureRegistry structureRegistry,
                                 dev.thorinwasher.forgery.database.Database database) implements Listener {


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        for (ForgeryStructure forgeryStructure : structureRegistry.getPossibleStructures(event.getBlockPlaced().getType(), ForgeryRegistry.STRUCTURE_TYPES.get("blast_furnace"))) {
            Optional<PlacedForgeryStructure<StructureBehavior>> placedStructure = PlacedForgeryStructure.findValid(forgeryStructure, event.getBlockPlaced().getLocation(), () -> new StructureBehavior(UUID.randomUUID()));
            placedStructure.ifPresent(structure -> {
                event.getPlayer().sendMessage(Component.translatable("Successfully built blast furnace"));
                placedStructureRegistry.registerStructure(structure);
            });
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        placedStructureRegistry.getStructure(BlockLocation.fromLocation(event.getBlock().getLocation()))
                .ifPresent(structure -> {
                    event.getPlayer().sendMessage(Component.translatable("Successfully destroyed " + structure.holder().structureType().key()));
                    placedStructureRegistry.unregisterStructure(structure);
                });
    }
}
