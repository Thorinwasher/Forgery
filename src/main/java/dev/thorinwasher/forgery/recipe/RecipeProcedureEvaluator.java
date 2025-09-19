package dev.thorinwasher.forgery.recipe;

import dev.thorinwasher.forgery.TimeProvider;
import dev.thorinwasher.forgery.forgeries.StructureStateChange;
import dev.thorinwasher.forgery.forging.ForgingStep;
import dev.thorinwasher.forgery.forging.ForgingStepProperty;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.inventory.ForgingItem;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.util.Duration;
import dev.thorinwasher.forgery.util.ForgeryKey;
import dev.thorinwasher.forgery.util.Pair;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class RecipeProcedureEvaluator {

    public static ItemStack findRecipeResult(Map<String, List<StructureStateChange>> change, List<ForgingItem> itemInput,
                                             long processStart, Collection<Recipe> recipes, ItemAdapter adapter) {
        List<Pair<Recipe, Double>> evaluated = recipes.stream()
                .map(recipe -> new Pair<>(recipe, evaluateRecipe(change, itemInput, recipe, processStart)))
                .sorted(Comparator.comparing(Pair::second))
                .toList();
        Pair<Recipe, Double> winner = evaluated.getLast();
        if (winner.second() < 0.3) {
            return ItemAdapter.failedItem();
        }
        RecipeResult recipeResult = winner.first().result();
        ItemStack itemStack = recipeResult.itemWriter().get(adapter.registry());
        itemStack.setAmount(recipeResult.amount());
        if (recipeResult.name() != null) {
            itemStack.setData(DataComponentTypes.CUSTOM_NAME, recipeResult.name().decoration(TextDecoration.ITALIC, false)
                    .colorIfAbsent(NamedTextColor.WHITE));
        }
        return itemStack;
    }

    private static double evaluateRecipe(Map<String, List<StructureStateChange>> change, List<ForgingItem> itemInput,
                                         Recipe recipe, long processStart) {
        double structureChangeScore = evaluateStructureStateChanges(change, processStart, recipe);
        Map<ForgingMaterial, Integer> ingredients = new HashMap<>();
        for (ForgingStep forgingStep : recipe.steps().steps()) {
            if (!forgingStep.properties().containsKey(ForgingStepProperty.INPUT_CONTENT)) {
                continue;
            }
            Map<ForgingMaterial, Integer> stepIngredients = forgingStep.getOrThrow(ForgingStepProperty.INPUT_CONTENT).ingredients();
            stepIngredients.forEach((material, integer) -> {
                ingredients.put(material, ingredients.getOrDefault(material, 0) + integer);
            });
        }
        double ingredientScore;
        if (!ingredients.isEmpty()) {
            Map<ForgingMaterial, Integer> actualIngredients = new HashMap<>();
            itemInput.stream()
                    .filter(item -> item.material() != null)
                    .forEach(item -> actualIngredients.put(item.material(), actualIngredients.getOrDefault(item.material(), 0) + 1));
            ingredientScore = evaluateIngredients(ingredients, actualIngredients);
        } else {
            ingredientScore = 1D;
        }
        return ingredientScore * structureChangeScore;
    }

    public static double nearbyValueScore(long expected, long value) {
        double diff = Math.abs(expected - value);
        return 1 - Math.max(diff / expected, 0D);
    }

    public static double evaluateIngredients(Map<ForgingMaterial, Integer> target, Map<ForgingMaterial, Integer> actual) {
        double ingredientScore = target.entrySet().stream()
                .map(entry -> Math.pow(entry.getKey().normalizedScore(), entry.getValue()))
                .reduce(0D, (aDouble, aDouble2) -> aDouble * aDouble2);
        int ingredientAmount = target.values().stream().reduce(0, Integer::sum);
        // Average out t
        double output = Math.pow(ingredientScore, 1D / ingredientAmount);
        Map<ForgeryKey, Integer> modifiedTarget = target.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().key(), Map.Entry::getValue));
        Map<ForgeryKey, Integer> modifiedActual = actual.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().key(), Map.Entry::getValue));
        if (modifiedTarget.size() != modifiedActual.size()) {
            return 0;
        }
        for (Map.Entry<ForgeryKey, Integer> targetEntry : modifiedTarget.entrySet()) {
            Integer actualAmount = modifiedActual.get(targetEntry.getKey());
            if (actualAmount == null || actualAmount == 0) {
                return 0;
            }
            output *= nearbyValueScore(targetEntry.getValue(), actualAmount);
        }
        return output;
    }

    private static double evaluateStructureStateChanges(Map<String, List<StructureStateChange>> changes, long processStart,
                                                        Recipe recipe) {
        Map<String, List<StructureStateTimePoint>> expectedChanges = new HashMap<>();
        long timePoint = processStart;
        for (ForgingStep step : recipe.steps().steps()) {
            if (!step.properties().containsKey(ForgingStepProperty.IS_STATE) && !step.properties().containsKey(ForgingStepProperty.NOT_STATE)) {
                continue;
            }
            long duration = step.getOrDefault(ForgingStepProperty.PROCESS_TIME, new Duration(1L)).duration();
            if (step.properties().containsKey(ForgingStepProperty.IS_STATE)) {
                expectedChanges.computeIfAbsent(step.getOrThrow(ForgingStepProperty.IS_STATE), ignore -> new ArrayList<>())
                        .add(new StructureStateTimePoint(
                                step.getOrThrow(ForgingStepProperty.IS_STATE),
                                true,
                                timePoint,
                                timePoint + duration
                        ));
            }
            if (step.properties().containsKey(ForgingStepProperty.NOT_STATE)) {
                expectedChanges.computeIfAbsent(step.getOrThrow(ForgingStepProperty.NOT_STATE), ignore -> new ArrayList<>())
                        .add(new StructureStateTimePoint(
                                step.getOrThrow(ForgingStepProperty.NOT_STATE),
                                false,
                                timePoint,
                                timePoint + duration
                        ));
            }
            timePoint += duration;
        }
        double precisionScore = 1D;
        for (String state : expectedChanges.keySet()) {
            List<StructureStateChange> actualChanges = changes.get(state);
            if (actualChanges == null) {
                return 0D;
            }
            precisionScore *= calculateStateChangeMatch(expectedChanges.get(state), actualChanges, TimeProvider.time(), processStart);
        }
        return Math.pow(precisionScore, 1D / expectedChanges.size());
    }

    private static double calculateStateChangeMatch(List<StructureStateTimePoint> expectedStates, List<StructureStateChange> actualChanges, long end, long start) {
        if (expectedStates.isEmpty()) {
            return 1D;
        }
        long matchingTime = 0L;
        long previousTimePoint = start;
        boolean active = false;
        for (StructureStateTimePoint expected : expectedStates) {
            int i = 0;
            while (actualChanges.size() > i) {
                StructureStateChange actual = actualChanges.get(i);
                if (expected.active() == active) {
                    long startTime = Math.max(previousTimePoint, expected.start());
                    long stopTime = Math.min(actual.timePoint(), expected.end());
                    matchingTime += Math.max(stopTime - startTime, 0L);
                }
                active = actual.action() == StructureStateChange.Action.ADD;
                previousTimePoint = actual.timePoint();
                i++;
            }
            if (expected.active() == active) {
                long startTime = Math.max(previousTimePoint, expected.start());
                long stopTime = Math.min(end, expected.end());
                matchingTime += Math.max(stopTime - startTime, 0L);
            }
        }
        return (double) matchingTime / Math.max(end - start, expectedStates.getLast().end() - start);
    }

    private record StructureStateTimePoint(String state, boolean active, long start, long end) {
    }
}
