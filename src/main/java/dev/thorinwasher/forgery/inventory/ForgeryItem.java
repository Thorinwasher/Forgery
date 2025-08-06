package dev.thorinwasher.forgery.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.thorinwasher.forgery.forging.ForgingStep;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record ForgeryItem(List<ForgingStep> steps) {


    public static Optional<ForgeryItem> fromString(String itemContent) {
        JsonElement jsonElement = JsonParser.parseString(itemContent);
        if (!(jsonElement instanceof JsonArray jsonArray)) {
            return Optional.empty();
        }
        List<JsonElement> content = jsonArray.asList();
        if (!content.stream().allMatch(JsonElement::isJsonObject)) {
            return Optional.empty();
        }
        return Optional.of(new ForgeryItem(
                content.stream()
                        .map(JsonElement::getAsJsonObject)
                        .map(ForgingStep::fromJson)
                        .toList()
        ));
    }

    public @NotNull String asString() {
        JsonArray output = new JsonArray();
        steps.stream()
                .map(ForgingStep::asJson)
                .forEach(output::add);
        return output.toString();
    }

    @Override
    public @NotNull String toString() {
        return this.asString();
    }
}
