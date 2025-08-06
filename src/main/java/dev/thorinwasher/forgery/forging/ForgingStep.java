package dev.thorinwasher.forgery.forging;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import dev.thorinwasher.forgery.Forgery;
import dev.thorinwasher.forgery.ForgeryRegistry;
import net.kyori.adventure.key.Key;

import java.util.Map;

public record ForgingStep(Map<ForgingStepProperty<?>, Object> properties) {


    public static ForgingStep fromJson(JsonObject json) {
        ImmutableMap.Builder<ForgingStepProperty<?>, Object> builder = new ImmutableMap.Builder<>();
        for (String key : json.keySet()) {
            ForgingStepProperty<?> forgingStepProperty = ForgeryRegistry.FORGING_STEP_PROPERTY.get(Key.key(Forgery.NAMESPACE, key));
            Preconditions.checkArgument(forgingStepProperty != null, "Unknown forging step property: " + key);
            Object value = forgingStepProperty.deserializer().apply(json.get(key));
            builder.put(forgingStepProperty, value);
        }
        return new ForgingStep(builder.build());
    }

    public JsonObject asJson() {
        JsonObject jsonObject = new JsonObject();
        for (ForgingStepProperty<?> forgingStepProperty : properties.keySet()) {
            jsonObject.add(forgingStepProperty.name(), forgingStepProperty.serialize(properties.get(forgingStepProperty)));
        }
        return jsonObject;
    }
}
