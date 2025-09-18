package dev.thorinwasher.forgery.forging;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.util.Duration;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

public record ForgingStepProperty<T>(String name, TypeToken<T> typeToken) implements Keyed {

    public static final ForgingStepProperty<List<String>> ALLOWED_STRUCTURE_TYPES = new ForgingStepProperty<>(
            "allowed_structure_types",
            TypeFactory.parameterizedClass(List.class, String.class)
    );
    public static final ForgingStepProperty<Duration> PROCESS_TIME = new ForgingStepProperty<>(
            "process_time",
            Duration.class
    );
    public static final ForgingStepProperty<Integer> PROCESS_AMOUNT = new ForgingStepProperty<>(
            "repeat",
            Integer.class
    );
    public static final ForgingStepProperty<ForgingIngredients> INPUT_CONTENT = new ForgingStepProperty<>(
            "ingredients",
            ForgingIngredients.class
    );
    public static final ForgingStepProperty<ToolInput> TOOL_INPUT = new ForgingStepProperty<>(
            "tool_input",
            ToolInput.class
    );
    public static final ForgingStepProperty<String> TARGET_INVENTORY = new ForgingStepProperty<>(
            "target_inventory",
            String.class
    );
    public static final ForgingStepProperty<String> IS_STATE = new ForgingStepProperty<>(
            "is_state",
            String.class
    );
    public static final ForgingStepProperty<String> NOT_STATE = new ForgingStepProperty<>(
            "not_state",
            String.class
    );

    public ForgingStepProperty(String allowedStructureTypes, Type type) {
        this(allowedStructureTypes, (TypeToken<T>) TypeToken.get(type));
    }

    public ForgingStepProperty(String allowedStructureTypes, Class<T> tClass) {
        this(allowedStructureTypes, TypeToken.get(tClass));
    }

    private static JsonElement toJsonArray(List<String> strings) {
        JsonArray jsonElements = new JsonArray();
        strings.forEach(jsonElements::add);
        return jsonElements;
    }

    @Override
    public @NotNull Key key() {
        return Key.key(Forgery.NAMESPACE, name());
    }
}
