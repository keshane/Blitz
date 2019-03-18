package com.keshane.blitz;

import java.util.ArrayList;
import java.util.List;

public class MoveResult {
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    MoveResult() {
    }

    public boolean isSuccess() {
        return errors.isEmpty() && warnings.isEmpty();
    }

    void addError(String errorMessage) {
        checkInputMessage(errorMessage);
        errors.add(errorMessage);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }

    void addWarning(String warningMessage) {
        checkInputMessage(warningMessage);
        warnings.add(warningMessage);
    }

    private void checkInputMessage(String message) {
        if (message == null) {
            throw new IllegalArgumentException("Error or warning message can't be null");
        }
    }
}
