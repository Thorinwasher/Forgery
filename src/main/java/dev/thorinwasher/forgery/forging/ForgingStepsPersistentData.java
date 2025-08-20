package dev.thorinwasher.forgery.forging;

import com.google.common.base.Preconditions;
import dev.thorinwasher.forgery.serialize.Serialize;
import io.leangen.geantyref.TypeToken;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

public class ForgingStepsPersistentData implements PersistentDataType<String, ForgingSteps> {

    public static ForgingStepsPersistentData INSTANCE = new ForgingStepsPersistentData();


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
        return Serialize.asJson(TypeToken.get(ForgingSteps.class), complex)
                .orElseThrow(() -> new IllegalArgumentException("Invalid persistent data"));
    }

    @Override
    public @NotNull ForgingSteps fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        try {
            ConfigurationNode node = GsonConfigurationLoader.builder().buildAndLoadString(primitive);
            ForgingSteps steps = node.get(ForgingSteps.class);
            Preconditions.checkArgument(steps != null, "Unknown persistent data: " + primitive);
            return steps;
        } catch (ConfigurateException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
