package dev.thorinwasher.forgery.forging;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record ForgingSteps(List<ForgingStep> steps) {

    public static Optional<ForgingSteps> fromJson(JsonElement jsonElement) {
        if (!(jsonElement instanceof JsonArray jsonArray)) {
            return Optional.empty();
        }
        List<JsonElement> content = jsonArray.asList();
        if (!content.stream().allMatch(JsonElement::isJsonObject)) {
            return Optional.empty();
        }
        return Optional.of(new ForgingSteps(
                content.stream()
                        .map(JsonElement::getAsJsonObject)
                        .map(ForgingStep::fromJson)
                        .toList()
        ));
    }

    public @NotNull JsonElement asJson() {
        JsonArray output = new JsonArray();
        steps.stream()
                .map(ForgingStep::asJson)
                .forEach(output::add);
        return output;
    }

    @Override
    public @NotNull String toString() {
        return this.asJson().toString();
    }

    public Optional<ForgingMaterial> calculateMaterial() {
        return Optional.empty();
    }
}
