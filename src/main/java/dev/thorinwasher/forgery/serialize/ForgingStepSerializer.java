package dev.thorinwasher.forgery.serialize;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.ForgeryRegistry;
import dev.thorinwasher.forgery.forging.ForgingStep;
import dev.thorinwasher.forgery.forging.ForgingStepProperty;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;

public class ForgingStepSerializer implements TypeSerializer<ForgingStep> {
    @Override
    public ForgingStep deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        Map<Object, ? extends ConfigurationNode> children = node.childrenMap();
        ImmutableMap.Builder<ForgingStepProperty<?>, Object> builder = new ImmutableMap.Builder<>();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : children.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException("Unknown key: " + entry.getKey());
            }
            ForgingStepProperty<?> forgingStepProperty = ForgeryRegistry.FORGING_STEP_PROPERTY.get(Key.key(Forgery.NAMESPACE, key.toLowerCase(Locale.ROOT)));
            Preconditions.checkArgument(forgingStepProperty != null, "Unknown forging step property: " + key);
            Object value = entry.getValue().get(forgingStepProperty.typeToken());
            Preconditions.checkArgument(value != null, "Unknown value for forging step property: " + key);
            builder.put(forgingStepProperty, value);
        }
        return new ForgingStep(builder.build());
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable ForgingStep obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        for (Map.Entry<ForgingStepProperty<?>, Object> entry : obj.properties().entrySet()) {
            node.node(entry.getKey().key().asString())
                    .set((TypeToken<? super Object>) entry.getKey().typeToken(), entry.getValue());
        }
    }
}
