package dev.thorinwasher.forgery.recipe;

import dev.thorinwasher.forgery.TimeProvider;
import dev.thorinwasher.forgery.forgeries.StructureStateChange;
import dev.thorinwasher.forgery.forging.ForgingStep;
import dev.thorinwasher.forgery.forging.ForgingStepProperty;
import dev.thorinwasher.forgery.forging.ItemAdapter;
import dev.thorinwasher.forgery.forging.ToolInput;
import dev.thorinwasher.forgery.inventory.ForgingItem;
import dev.thorinwasher.forgery.inventory.ForgingMaterial;
import dev.thorinwasher.forgery.util.Duration;
import dev.thorinwasher.forgery.util.ForgeryKey;
import dev.thorinwasher.forgery.util.Pair;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class RecipeProcedureEvaluator {

    public static Optional<ItemStack> findRecipeResult(Map<String, List<StructureStateChange>> change, List<ForgingItem> itemInput,
                                                       long processStart, Collection<Recipe> recipes, ItemAdapter adapter, String structureType,
                                                       List<ToolInput> toolHistory) {
        List<Pair<Pair<Recipe, Integer>, Double>> evaluated = recipes.stream()
                .filter(recipe -> structureType.equals(recipe.structureType()))
                .map(recipe -> new Pair<>(recipe, evaluateRecipe(change, itemInput, recipe, processStart, toolHistory)))
                .filter(RecipeProcedureEvaluator::recipeApplicable)
                .map(pair ->
                        new Pair<>(new Pair<>(pair.first(), pair.second().second()), pair.second().first().values().stream().reduce(1D, ((aDouble, aDouble2) -> aDouble * aDouble2)))
                )
                .sorted(Comparator.comparing(Pair::second))
                .toList();
        if (evaluated.isEmpty()) {
            return Optional.empty();
        }
        Pair<Pair<Recipe, Integer>, Double> winner = evaluated.getLast();
        if (winner.second() < 0.3) {
            return Optional.of(ItemAdapter.failedItem());
        }
        RecipeResult result = winner.first().first().result();
        ItemStack itemStack = result.get(
                (int) Math.ceil(winner.second() * 10D),
                adapter.registry(),
                winner.first().second()
        );
        if (result.temperature() == null && result.heatBehavior() != null) {
            result.heatBehavior().applyTo(itemStack, itemInput.stream()
                    .map(ForgingItem::calculatedTemperature)
                    .reduce(0D, Double::sum) / itemInput.size()
            );
        }
        return Optional.of(itemStack);
    }

    private static boolean recipeApplicable(Pair<Recipe, Pair<Map<String, Double>, Integer>> recipePairPair) {
        Map<String, Double> scores = recipePairPair.second().first();
        Set<String> ignored = Set.of("heat", "ingredients");
        List<Double> scoreList = scores.entrySet().stream()
                .filter(entry -> !ignored.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
        if (scoreList.isEmpty()) {
            return scores.values().stream()
                    .noneMatch(value -> value < 0.3);
        } else {
            return scoreList.stream().noneMatch(value -> value < 0.3);
        }
    }

    private static Pair<Map<String, Double>, Integer> evaluateRecipe(Map<String, List<StructureStateChange>> change, List<ForgingItem> itemInput,
                                                                     Recipe recipe, long processStart, List<ToolInput> toolHistory) {
        List<ForgingStep> processedSteps = processRecipeSteps(recipe);
        long recalculatedProcessStart = calculateProcessStart(processedSteps, change, toolHistory, processStart);
        double structureChangeScore = evaluateStructureStateChanges(change, recalculatedProcessStart, processedSteps);
        Map<ForgingMaterial, Integer> ingredients = new HashMap<>();
        for (ForgingStep forgingStep : processedSteps) {
            if (!forgingStep.properties().containsKey(ForgingStepProperty.INPUT_CONTENT)) {
                continue;
            }
            Map<ForgingMaterial, Integer> stepIngredients = forgingStep.getOrThrow(ForgingStepProperty.INPUT_CONTENT).ingredients();
            stepIngredients.forEach((material, integer) -> {
                ingredients.put(material, ingredients.getOrDefault(material, 0) + integer);
            });
        }
        Pair<Double, Integer> ingredientScore;
        Map<ForgingMaterial, Integer> actualIngredients = new HashMap<>();
        itemInput.stream()
                .filter(item -> item.material() != null)
                .forEach(item -> actualIngredients.put(item.material(), actualIngredients.getOrDefault(item.material(), 0) + 1));
        if (!ingredients.isEmpty()) {
            ingredientScore = evaluateIngredients(ingredients, actualIngredients);
        } else {
            ingredientScore = new Pair<>(actualIngredients.isEmpty() ? 1D : 0D, 1);
        }
        double toolScore;
        List<ToolInput> expectedToolHistory = findExpectedToolHistory(processedSteps, recalculatedProcessStart);
        if (!toolHistory.isEmpty()) {
            toolScore = evaluateToolHistory(expectedToolHistory, toolHistory, recalculatedProcessStart);
        } else {
            toolScore = expectedToolHistory.isEmpty() ? 1D : 0D;
        }
        Map<String, Double> scores = new HashMap<>();
        if (!ingredients.isEmpty()) {
            scores.put("ingredients", ingredientScore.first());
        }
        if (!expectedToolHistory.isEmpty()) {
            scores.put("tool", toolScore);
        }
        if (structureChangeScore != -1D) {
            scores.put("state_changes", structureChangeScore);
        }
        if (recipe.minimalHeat() != null) {
            scores.put("heat", recipe.minimalHeat() > itemInput.stream()
                    .map(ForgingItem::calculatedTemperature)
                    .reduce(0D, Double::sum) / itemInput.size() ? 0D : 1D);
        }
        return new Pair<>(scores, ingredientScore.second());
    }

    private static List<ForgingStep> processRecipeSteps(Recipe recipe) {
        List<ForgingStep> output = new ArrayList<>();
        for (ForgingStep inputStep : recipe.steps().steps()) {
            if (inputStep.properties().containsKey(ForgingStepProperty.REPEAT)) {
                int amount = inputStep.getOrThrow(ForgingStepProperty.REPEAT);
                for (int i = 0; i < amount; i++) {
                    output.add(inputStep);
                }
            } else {
                output.add(inputStep);
            }
        }
        return output;
    }

    private static double evaluateToolHistory(List<ToolInput> expectedToolHistory, List<ToolInput> toolHistory, long processStart) {
        if (expectedToolHistory.isEmpty()) {
            return toolHistory.isEmpty() ? 1D : 0D; //TODO: Is this too strict?
        }
        double wrongInputScoreDecrement = Math.sqrt(Math.min(1D, 1D / expectedToolHistory.size()));
        int actualIndex = -1;
        double score = 1D;
        long previousExpected = processStart;
        long previousActual = processStart;
        for (ToolInput expected : expectedToolHistory) {
            while (toolHistory.size() > ++actualIndex && !expected.tool().equalsIgnoreCase(toolHistory.get(actualIndex).tool())) {
                score -= wrongInputScoreDecrement;
            }
            if (toolHistory.size() <= actualIndex) {
                score -= wrongInputScoreDecrement;
                continue;
            }
            long expectedInterval = expected.timePoint() - previousExpected;
            previousExpected = expected.timePoint();
            long scoringAllowance = expectedInterval + 200; // 10s
            ToolInput actual = toolHistory.get(actualIndex);
            long difference = previousActual + expectedInterval - actual.timePoint();
            previousActual = actual.timePoint();
            score -= Math.min(wrongInputScoreDecrement, Math.pow((double) Math.abs(difference) / scoringAllowance, 2));
        }
        return Math.max(0, score);
    }

    private static List<ToolInput> findExpectedToolHistory(List<ForgingStep> steps, long processStart) {
        List<ToolInput> output = new ArrayList<>();
        long timePoint = processStart;
        for (ForgingStep forgingStep : steps) {
            if (forgingStep.properties().containsKey(ForgingStepProperty.TOOL_INPUT)) {
                final long finalTimePoint = timePoint;
                forgingStep.getOrThrow(ForgingStepProperty.TOOL_INPUT)
                        .forEach(toolInput ->
                                output.add(new ToolInput(
                                        toolInput,
                                        finalTimePoint
                                ))
                        );
            }
            if (forgingStep.properties().containsKey(ForgingStepProperty.PROCESS_TIME)) {
                timePoint += forgingStep.getOrThrow(ForgingStepProperty.PROCESS_TIME).duration();
            }
        }
        return output;
    }

    public static double nearbyValueScore(long expected, long value) {
        double diff = Math.abs(expected - value);
        return 1 - Math.max(diff / expected, 0D);
    }

    public static Pair<Double, Integer> evaluateIngredients(Map<ForgingMaterial, Integer> target, Map<ForgingMaterial, Integer> actual) {
        double ingredientScore = target.entrySet().stream()
                .map(entry -> Math.pow(entry.getKey().normalizedScore(), entry.getValue()))
                .reduce(1D, (aDouble, aDouble2) -> aDouble * aDouble2);
        int ingredientAmount = target.values().stream().reduce(0, Integer::sum);
        // Average out t
        double output = Math.pow(ingredientScore, 1D / ingredientAmount);
        Map<ForgeryKey, Integer> modifiedTarget = target.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().key(), Map.Entry::getValue));
        Map<ForgeryKey, Integer> modifiedActual = actual.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().key(), Map.Entry::getValue));
        if (modifiedTarget.size() != modifiedActual.size()) {
            return new Pair<>(0D, 1);
        }
        int repetitions = findRepetitions(modifiedTarget, modifiedActual);
        for (Map.Entry<ForgeryKey, Integer> targetEntry : modifiedTarget.entrySet()) {
            Integer actualAmount = modifiedActual.get(targetEntry.getKey());
            if (actualAmount == null || actualAmount == 0) {
                return new Pair<>(0D, 1);
            }
            output *= nearbyValueScore((long) targetEntry.getValue() * repetitions, actualAmount);
        }
        return new Pair<>(output, repetitions);
    }

    private static int findRepetitions(Map<ForgeryKey, Integer> modifiedTarget, Map<ForgeryKey, Integer> modifiedActual) {
        int repetitions = -1;
        for (Map.Entry<ForgeryKey, Integer> targetEntry : modifiedTarget.entrySet()) {
            Integer actualAmount = modifiedActual.get(targetEntry.getKey());
            if (actualAmount == null || actualAmount == 0) {
                return 1;
            }
            int ceilDiv = Math.ceilDiv(actualAmount, targetEntry.getValue());
            repetitions = (repetitions == -1) ? ceilDiv : Math.min(repetitions, ceilDiv);
        }
        return repetitions == -1 ? 1 : repetitions;
    }

    private static double evaluateStructureStateChanges(Map<String, List<StructureStateChange>> changes, long processStart,
                                                        List<ForgingStep> steps) {
        Map<String, List<StructureStateTimePoint>> expectedChanges = new HashMap<>();
        long timePoint = processStart;
        for (ForgingStep step : steps) {
            if (!step.properties().containsKey(ForgingStepProperty.IS_STATE) && !step.properties().containsKey(ForgingStepProperty.NOT_STATE)) {
                timePoint += step.getOrDefault(ForgingStepProperty.PROCESS_TIME, new Duration(0L)).duration();
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
        if (expectedChanges.isEmpty()) {
            return -1D;
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

    private static long calculateProcessStart(List<ForgingStep> steps, Map<String, List<StructureStateChange>> stateChanges, List<ToolInput> toolHistory, long defaultValue) {
        long stateProcessStart = -1;
        long stateTimeOffset = 0;
        for (ForgingStep forgingStep : steps) {
            if (!forgingStep.properties().containsKey(ForgingStepProperty.IS_STATE) && !forgingStep.properties().containsKey(ForgingStepProperty.NOT_STATE)) {
                if (forgingStep.properties().containsKey(ForgingStepProperty.PROCESS_TIME)) {
                    stateTimeOffset -= forgingStep.getOrThrow(ForgingStepProperty.PROCESS_TIME).duration();
                }
                continue;
            }
            String state = forgingStep.<String>getOrDefault(
                    ForgingStepProperty.IS_STATE,
                    () -> forgingStep.getOrThrow(ForgingStepProperty.NOT_STATE)
            );
            stateProcessStart = calculateStateProcessStart(stateChanges, defaultValue, state, forgingStep.properties().containsKey(ForgingStepProperty.IS_STATE)) + stateTimeOffset;
            break;
        }
        long toolProcessStart = calculateToolProcessStart(toolHistory, defaultValue);
        if (stateProcessStart == -1) {
            return toolProcessStart == -1 ? defaultValue : toolProcessStart;
        }
        if (toolProcessStart == -1) {
            return stateProcessStart;
        }
        return Math.min(toolProcessStart, stateProcessStart);
    }

    private static long calculateToolProcessStart(List<ToolInput> toolHistory, long defaultValue) {
        for (ToolInput toolInput : toolHistory) {
            if (toolInput.timePoint() < defaultValue) {
                continue;
            }
            return toolInput.timePoint();
        }
        return -1;
    }

    private static long calculateStateProcessStart(Map<String, List<StructureStateChange>> changes, long defaultValue, String state, boolean active) {
        List<StructureStateChange> stateChanges = changes.get(state);
        if (stateChanges == null) {
            return -1;
        }
        for (StructureStateChange stateChange : stateChanges) {
            if ((stateChange.action() == StructureStateChange.Action.ADD) == active && stateChange.timePoint() > defaultValue) {
                return stateChange.timePoint();
            }
        }
        return -1;
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
