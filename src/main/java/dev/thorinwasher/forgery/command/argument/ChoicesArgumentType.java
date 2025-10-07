package dev.thorinwasher.forgery.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChoicesArgumentType<T> implements CustomArgumentType.Converted<T, String> {

    private static final DynamicCommandExceptionType UNKNOWN_VALUE = new DynamicCommandExceptionType(value ->
            MessageComponentSerializer.message().serialize(Component.text("Unknown value'" + value + "'"))
    );

    private final Map<String, T> backing;

    public ChoicesArgumentType(Map<String, T> backing) {
        this.backing = backing;
    }


    @Override
    public @NotNull T convert(String nativeType) throws CommandSyntaxException {
        T t = backing.get(nativeType.toLowerCase(Locale.ROOT));
        if (t == null) {
            throw UNKNOWN_VALUE.create(nativeType);
        }
        return t;
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        backing.keySet().stream()
                .filter(string -> string.startsWith(builder.getInput().toLowerCase(Locale.ROOT)))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
