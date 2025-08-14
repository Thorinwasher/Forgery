package dev.thorinwasher.forgery.forging;

import com.google.gson.JsonParser;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ForgingStepPersistentData implements PersistentDataType<String, ForgingSteps> {

    public static ForgingStepPersistentData INSTANCE = new ForgingStepPersistentData();


    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<ForgingSteps> getComplexType() {
        return ForgingSteps.class;
    }

    @Override
    public @NotNull String toPrimitive(@NotNull ForgingSteps complex, @NotNull PersistentDataAdapterContext context) {
        return complex.asJson().toString();
    }

    @Override
    public @NotNull ForgingSteps fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        return ForgingSteps.fromJson(JsonParser.parseString(primitive)).get();
    }
}
