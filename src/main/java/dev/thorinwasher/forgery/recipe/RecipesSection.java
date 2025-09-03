package dev.thorinwasher.forgery.recipe;

import dev.thorinwasher.forgery.util.ForgeryKey;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class RecipesSection {

    private final Map<ForgeryKey, Recipe> recipes = new HashMap<>();
}
