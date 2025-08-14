package dev.thorinwasher.forgery;

import dev.thorinwasher.forgery.api.ForgeryApi;
import dev.thorinwasher.forgery.integration.IntegrationRegistry;
import dev.thorinwasher.forgery.structure.PlacedStructureRegistry;

public record ForgeryApiImpl(IntegrationRegistry integrationRegistry,
                             PlacedStructureRegistry placedStructureRegistry) implements ForgeryApi {

}
