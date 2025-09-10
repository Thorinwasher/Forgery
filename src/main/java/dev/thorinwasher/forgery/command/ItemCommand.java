package dev.thorinwasher.forgery.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.database.PersistencyAccess;
import dev.thorinwasher.forgery.recipe.ItemReference;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public record ItemCommand(Map<Key, ItemReference> itemReferences, PersistencyAccess persistencyAccess) {
    private static final DynamicCommandExceptionType NOT_VALID_KEY = new DynamicCommandExceptionType(value ->
            MessageComponentSerializer.message().serialize(Component.text("Expected a valid key, got '" + value + "'"))
    );
    private static final SimpleCommandExceptionType NOT_HOLDING_AN_ITEM = new SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.text("Player is not holding an item"))
    );

    public LiteralArgumentBuilder<CommandSourceStack> builder() {
        return Commands.literal("item")
                .then(Commands.literal("store")
                        .then(Commands.argument("item-name", StringArgumentType.word())
                                .executes(context -> {
                                    String itemName = context.getArgument("item-name", String.class);
                                    if (!Key.parseableValue(itemName)) {
                                        throw NOT_VALID_KEY.create(itemName);
                                    }
                                    NamespacedKey key = Forgery.key(itemName);
                                    Player player = ForgeryCommand.findExecutor(context);
                                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                                    if (itemStack.isEmpty()) {
                                        throw NOT_HOLDING_AN_ITEM.create();
                                    }
                                    ItemReference itemReference = new ItemReference(itemName, itemStack);
                                    if (itemReferences.containsKey(key)) {
                                        persistencyAccess.database().update(persistencyAccess.itemStoredData(), itemReference);
                                    } else {
                                        persistencyAccess.database().insert(persistencyAccess.itemStoredData(), itemReference);
                                    }
                                    itemReferences.put(key, itemReference);
                                    context.getSource().getSender().sendMessage(Component.text("Successfully stored the item ").color(NamedTextColor.GREEN).append(itemStack.effectiveName()));
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("item-name", StringArgumentType.word()).executes(context -> {
                                    String itemName = context.getArgument("item-name", String.class);
                                    if (!Key.parseableValue(itemName)) {
                                        throw NOT_VALID_KEY.create(itemName);
                                    }
                                    NamespacedKey key = Forgery.key(itemName);
                                    if (itemReferences.containsKey(key)) {
                                        persistencyAccess.database().remove(persistencyAccess.itemStoredData(), itemReferences().get(key));
                                        itemReferences.remove(key);
                                        context.getSource().getSender().sendMessage(Component.text("Successfully removed the item reference " + itemName).color(NamedTextColor.GREEN));
                                    } else {
                                        context.getSource().getSender().sendMessage(Component.text("Could not find item reference " + itemName).color(NamedTextColor.RED));
                                    }
                                    return 1;
                                })
                        )
                )
                .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("forgery.command.item"));
    }
}
