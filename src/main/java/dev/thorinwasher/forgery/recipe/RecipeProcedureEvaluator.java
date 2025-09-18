package dev.thorinwasher.forgery.recipe;

import dev.thorinwasher.forgery.forgeries.StructureStateChange;
import dev.thorinwasher.forgery.forging.ForgingStep;
import dev.thorinwasher.forgery.forging.ForgingStepProperty;
import dev.thorinwasher.forgery.inventory.ForgingItem;
import dev.thorinwasher.forgery.util.Duration;

import java.util.*;
import java.util.stream.Stream;

public class RecipeProcedureEvaluator {

    public static Stream<ForgingItem> findRecipeResult(Map<String, List<StructureStateChange>> change, List<ForgingItem> itemInput,
                                                       long processStart, int alreadyTaken, Collection<Recipe> recipes) {
        List<Recipe> evaluated = recipes.stream()
                .sorted(Comparator.comparing(recipe -> evaluateRecipe(change, itemInput, recipe, processStart)))
                .toList();
    }

    private static double evaluateRecipe(Map<String, List<StructureStateChange>> change, List<ForgingItem> itemInput,
                                         Recipe recipe, long processStart) {

    }

    private static double evaluateStructureStateChanges(Map<String, List<StructureStateChange>> changes, long processStart,
                                                        Recipe recipe) {
        Map<String, List<StructureStateTimePoint>> expectedChanges = new HashMap<>();
        long timePoint = processStart;
        for (ForgingStep step : recipe.steps().steps()) {
            if (!step.properties().containsKey(ForgingStepProperty.IS_STATE) || step.properties().containsKey(ForgingStepProperty.NOT_STATE)) {
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
                expectedChanges.computeIfAbsent(step.getOrThrow(ForgingStepProperty.IS_STATE), ignore -> new ArrayList<>())
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
            precisionScore *= calculateStateChangeMatch(expectedChanges.get(state), actualChanges, timePoint, processStart);
        }
        return Math.pow(precisionScore, expectedChanges.size());
    }

    private static double calculateStateChangeMatch(List<StructureStateTimePoint> expectedStates, List<StructureStateChange> actualChanges, long end, long start) {
        int i = 0;
        long matchingTime = 0L;
        long previousTimePoint = start;
        boolean active = false;
        for (StructureStateTimePoint expected : expectedStates) {
            while (actualChanges.size() > i) {
                StructureStateChange actual = actualChanges.get(i);
                if (actual.timePoint() > expected.end()) {
                    active = actual.action() == StructureStateChange.Action.ADD;
                    previousTimePoint = actual.timePoint();
                    break;
                }
                if (actual.timePoint() < expected.start()) {
                    i++;
                    active = actual.action() == StructureStateChange.Action.ADD;
                    previousTimePoint = actual.timePoint();
                    continue;
                }
                if (expected.active() == active) {
                    long startTime = Math.max(previousTimePoint, expected.start());
                    long stopTime = Math.min(actual.timePoint(), expected.end());
                    matchingTime += Math.max(stopTime - startTime, 0L);
                }
                active = actual.action() == StructureStateChange.Action.ADD;
                previousTimePoint = actual.timePoint();
            }
        }
        if (!expectedStates.isEmpty() && expectedStates.getLast().active() == active) {
            StructureStateTimePoint expected = expectedStates.getLast();
            long startTime = Math.max(previousTimePoint, expected.start());
            long stopTime = Math.min(end, expected.end());
            matchingTime += Math.max(stopTime - startTime, 0L);
        }
        return (double) matchingTime / (end - start);
    }

    private record StructureStateTimePoint(String state, boolean active, long start, long end) {
    }
}
