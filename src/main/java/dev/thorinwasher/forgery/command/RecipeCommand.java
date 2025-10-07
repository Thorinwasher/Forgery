package dev.thorinwasher.forgery.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import dev.thorinwasher.forgery.command.argument.ChoicesArgumentType;
import dev.thorinwasher.forgery.integration.IntegrationRegistry;
import dev.thorinwasher.forgery.recipe.Recipe;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.Map;

public record RecipeCommand(Map<String, Recipe> recipes, IntegrationRegistry integrationRegistry) {


    public ArgumentBuilder<CommandSourceStack, ?> builder() {
        return Commands.literal("recipe")
                .then(Commands.literal("result")
                        .then(Commands.argument("recipe-key", new ChoicesArgumentType<>(recipes))
                                .executes(context -> {
                                    Recipe recipe = context.getArgument("recipe-key", Recipe.class);
                                    CommandUtil.giveItem(context.getSource(), recipe.result().get(10, integrationRegistry));
                                    return 1;
                                })
                        )
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("forgery.command.recipe.result"))
                );
    }
}
