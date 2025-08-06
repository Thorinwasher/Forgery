package dev.thorinwasher.forgery.listener;

import dev.thorinwasher.forgery.structure.PlacedForgeryStructure;
import dev.thorinwasher.forgery.structure.PlacedStructureRegistry;
import dev.thorinwasher.forgery.vector.BlockLocation;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public record PlayerEventListener(PlacedStructureRegistry placedStructureRegistry) implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onStructureInteract(PlayerInteractEvent event) {
        if (event.getAction().isLeftClick() || event.getHand() != EquipmentSlot.HAND || event.getPlayer().isSneaking()) {
            return;
        }
        Block clickedBLock = event.getClickedBlock();
        if (clickedBLock == null) {
            return;
        }
        BlockLocation blockLocation = BlockLocation.fromLocation(clickedBLock.getLocation());
        placedStructureRegistry.getStructure(blockLocation)
                .map(PlacedForgeryStructure::holder)
                .ifPresent(interactable -> interactable.interact(event.getPlayer(), blockLocation));
    }
}
