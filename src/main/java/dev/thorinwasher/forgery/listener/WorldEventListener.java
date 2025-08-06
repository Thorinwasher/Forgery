package dev.thorinwasher.forgery.listener;

import dev.thorinwasher.forgery.database.PersistencyAccess;
import dev.thorinwasher.forgery.structure.PlacedStructureRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public record WorldEventListener(PersistencyAccess persistencyAccess,
                                 PlacedStructureRegistry placedStructureRegistry) implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        persistencyAccess.database().find(persistencyAccess().behaviorStoredData(), event.getWorld().getUID())
                .thenAcceptAsync(behaviors ->
                        behaviors.forEach(behavior ->
                                placedStructureRegistry.registerStructure(behavior.placedStructure())
                        )
                );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent event) {
        persistencyAccess.database().find(persistencyAccess().behaviorStoredData(), event.getWorld().getUID())
                .thenAcceptAsync(behaviors ->
                        behaviors.forEach(behavior ->
                                placedStructureRegistry.unregisterStructure(behavior.placedStructure())
                        )
                );
    }
}
