package cca.checker.model;

import java.util.List;
import java.util.Set;

import cca.exceptions.IllFormedException;

public class LocalCheckResult {
    private final List<IllFormedException> errors;
    private final Set<String> discoveredCalls;

    public LocalCheckResult(List<IllFormedException> errors, Set<String> discoveredCalls) {
        this.errors = errors;
        this.discoveredCalls = discoveredCalls;
    }

    public List<IllFormedException> getErrors() {
        return errors;
    }

    public Set<String> getDiscoveredCalls() {
        return discoveredCalls;
    }
}
