package dev.thorinwasher.forgery.listener;

import dev.thorinwasher.forgery.forgeries.StructureBehavior;
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onStructureInteract(PlayerInteractEvent event) {
        if (event.getAction().isLeftClick() || event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        BlockLocation blockLocation = BlockLocation.fromLocation(clickedBlock.getLocation());
        placedStructureRegistry.getStructure(blockLocation)
                .map(PlacedForgeryStructure::holder)
                .ifPresent(interactable -> {
                    StructureBehavior.InteractionResult result = interactable.interact(event.getPlayer(), blockLocation);
                    event.setUseInteractedBlock(result.useBlock());
                    event.setUseItemInHand(result.useItem());
                });
    }
}
