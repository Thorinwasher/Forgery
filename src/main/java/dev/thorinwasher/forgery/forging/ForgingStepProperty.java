package dev.thorinwasher.forgery.forging;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.thorinwasher.forgery.Forgery;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public record ForgingStepProperty<T>(String name, Function<JsonElement, T> deserializer,
                                     Function<T, JsonElement> serializer) implements Keyed {
    
    public static final ForgingStepProperty<List<String>> ALLOWED_STRUCTURE_TYPES = new ForgingStepProperty<>(
            "allowed_structure_types",
            jsonObject -> jsonObject.getAsJsonArray().asList().stream()
                    .map(JsonElement::getAsString)
                    .toList(),
            ForgingStepProperty::toJsonArray
    );
    public static final ForgingStepProperty<Long> PROCESS_TIME = new ForgingStepProperty<>(
            "time",
            JsonElement::getAsLong,
            JsonPrimitive::new
    );
    public static final ForgingStepProperty<Integer> PROCESS_AMOUNT = new ForgingStepProperty<>(
            "repeat",
            JsonElement::getAsInt,
            JsonPrimitive::new
    );
    public static final ForgingStepProperty<ForgingIngredients> INPUT_CONTENT = new ForgingStepProperty<>(
            "ingredients",
            ForgingIngredients::fromJson,
            ForgingIngredients::toJson
    );
    public static final ForgingStepProperty<ToolInput> TOOL_INPUT = new ForgingStepProperty<>(
            "tool_input",
            jsonElement -> ToolInput.fromString(jsonElement.getAsString().toUpperCase(Locale.ROOT)),
            toolInput -> new JsonPrimitive(toolInput.asString())
    );
    public static final ForgingStepProperty<String> TARGET_INVENTORY = new ForgingStepProperty<>(
            "target_inventory",
            JsonElement::getAsString,
            JsonPrimitive::new
    );

    private static JsonElement toJsonArray(List<String> strings) {
        JsonArray jsonElements = new JsonArray();
        strings.forEach(jsonElements::add);
        return jsonElements;
    }

    @Override
    public @NotNull Key key() {
        return Key.key(Forgery.NAMESPACE, name());
    }

    public JsonElement serialize(Object object) {
        return serializer.apply((T) object);
    }
}
