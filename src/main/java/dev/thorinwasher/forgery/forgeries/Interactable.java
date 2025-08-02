package dev.thorinwasher.forgery.forgeries;

import dev.thorinwasher.forgery.vector.BlockLocation;
import org.bukkit.entity.Player;

public interface Interactable {

    void interact(Player actor, BlockLocation location);
}
