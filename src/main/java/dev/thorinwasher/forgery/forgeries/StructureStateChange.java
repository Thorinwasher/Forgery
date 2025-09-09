package dev.thorinwasher.forgery.forgeries;

public record StructureStateChange(String stateName, long timePoint, Action action) {

    public enum Action {
        REMOVE,
        ADD
    }
}
