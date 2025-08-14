package dev.thorinwasher.forgery.api;

import dev.thorinwasher.forgery.integration.IntegrationRegistry;
import dev.thorinwasher.forgery.structure.PlacedStructureRegistry;

public interface ForgeryApi {

    IntegrationRegistry integrationRegistry();

    PlacedStructureRegistry placedStructureRegistry();
}
