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

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class EnumArgumentType<E extends Enum<E>> implements CustomArgumentType.Converted<E, String> {

    private final Class<E> eClass;
    private static final DynamicCommandExceptionType UNKNOWN_VALUE = new DynamicCommandExceptionType(value ->
            MessageComponentSerializer.message().serialize(Component.text("Unknown value'" + value + "'"))
    );

    public EnumArgumentType(Class<E> eClass) {
        this.eClass = eClass;
    }

    @Override
    public E convert(String nativeType) throws CommandSyntaxException {
        return Arrays.stream(eClass.getEnumConstants())
                .filter(e -> e.name().equalsIgnoreCase(nativeType))
                .findFirst()
                .orElseThrow(() -> UNKNOWN_VALUE.create(nativeType));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Arrays.stream(eClass.getEnumConstants())
                .map(Enum::name)
                .map(string -> string.toLowerCase(Locale.ROOT))
                .filter(string -> string.startsWith(builder.getInput().toLowerCase(Locale.ROOT)))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
