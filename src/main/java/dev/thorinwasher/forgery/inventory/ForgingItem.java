package dev.thorinwasher.forgery.inventory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.thorinwasher.forgery.forging.ForgingSteps;
import dev.thorinwasher.forgery.util.ForgeryKey;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record ForgingItem(@Nullable ForgingMaterial material, ForgingSteps steps) {


    public static Optional<ForgingItem> fromJson(JsonElement jsonElement) {
        if (!(jsonElement instanceof JsonObject jsonObject)) {
            return Optional.empty();
        }
        Optional<ForgingMaterial> forgingMaterialOptional = ForgingMaterial.fromJson(jsonObject);
        if (jsonObject.has("steps")) {
            return ForgingSteps.fromJson(jsonObject.get("steps"))
                    .map(forgingSteps -> new ForgingItem(forgingMaterialOptional.orElse(null), forgingSteps));
        }
        return forgingMaterialOptional.map(ForgingItem::materialBased);
    }

    public static ForgingItem materialBased(ForgingMaterial material) {
        return new ForgingItem(material, new ForgingSteps(List.of()));
    }

    public static ForgingItem materialBased(ForgeryKey key) {
        return materialBased(new ForgingMaterial(key));
    }

    public JsonObject asJson() {
        JsonObject output = material != null ? material.asJson() : new JsonObject();
        if (!steps.steps().isEmpty()) {
            output.add("steps", steps.asJson());
        }
        return output;
    }
}
