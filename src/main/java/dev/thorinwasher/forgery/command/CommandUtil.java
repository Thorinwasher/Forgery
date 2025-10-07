package dev.thorinwasher.forgery.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class CommandUtil {

    private static final SimpleCommandExceptionType UNDEFINED_ENTITY = new SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.text("Has to be run from an entity or player"))
    );


    public static void giveItem(CommandSourceStack commandSourceStack, ItemStack itemStack) throws CommandSyntaxException {
        Inventory inventory;
        Location location;
        if (commandSourceStack.getExecutor() instanceof InventoryHolder holder) {
            inventory = holder.getInventory();
            location = commandSourceStack.getExecutor().getLocation();
        } else if (commandSourceStack.getSender() instanceof Player player) {
            inventory = player.getInventory();
            location = player.getLocation();
        } else {
            throw UNDEFINED_ENTITY.create();
        }
        if (!inventory.addItem(itemStack).isEmpty()) {
            location.getWorld().dropItemNaturally(location, itemStack);
        }
    }
}
