package dev.thorinwasher.forgery.serialize;

import dev.thorinwasher.forgery.forging.ForgingStep;
import dev.thorinwasher.forgery.forging.ForgingSteps;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;

public class ForgingStepsSerializer implements TypeSerializer<ForgingSteps> {
    @Override
    public ForgingSteps deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        List<ForgingStep> forgingSteps = node.getList(ForgingStep.class);
        return forgingSteps == null ? null : new ForgingSteps(forgingSteps);
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable ForgingSteps obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.set(obj.steps());
    }
}
