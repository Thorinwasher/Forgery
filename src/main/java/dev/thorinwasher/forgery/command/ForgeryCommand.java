package dev.thorinwasher.forgery.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.thorinwasher.forgery.database.PersistencyAccess;
import dev.thorinwasher.forgery.integration.IntegrationRegistry;
import dev.thorinwasher.forgery.recipe.ItemReference;
import dev.thorinwasher.forgery.recipe.Recipe;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;

public record ForgeryCommand(Map<Key, ItemReference> itemReferences, PersistencyAccess persistencyAccess,
                             Map<String, Recipe> recipes, IntegrationRegistry integrationRegistry) {

    private static final SimpleCommandExceptionType UNDEFINED_PLAYER = new SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.text("Expected a defined player"))
    );

    public void register(ReloadableRegistrarEvent<Commands> commands) {
        commands.registrar().register(Commands.literal("forgery")
                .then(new ItemCommand(itemReferences, persistencyAccess).builder())
                .then(new RecipeCommand(recipes, integrationRegistry).builder())
                .build()
        );
    }

    public static Player findExecutor(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity executor = context.getSource().getExecutor();
        if (executor instanceof Player player) {
            return player;
        }
        if (!(context.getSource().getSender() instanceof Player player)) {
            throw UNDEFINED_PLAYER.create();
        }
        return player;
    }
}
