package dev.thorinwasher.forgery.integration;

import dev.thorinwasher.forgery.integration.item.ForgeryItemIntegration;
import dev.thorinwasher.forgery.integration.item.MinecraftItemIntegration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class IntegrationRegistry {


    Map<String, ItemIntegration> itemIntegrations = new HashMap<>();

    public void initialize() {
        Stream.of(new ForgeryItemIntegration(), new MinecraftItemIntegration())
                .filter(ItemIntegration::isEnabled)
                .forEach(itemIntegration -> itemIntegrations.put(itemIntegration.id(), itemIntegration));
    }

    public Optional<ItemIntegration> itemIntegration(String id) {
        return Optional.ofNullable(itemIntegrations.get(id));
    }

    public Stream<ItemIntegration> itemIntegrations() {
        return itemIntegrations.values().stream();
    }
}
