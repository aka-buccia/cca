package cca.utils;

public enum VerbosityLevel {
    ERRORS(-1),
    INFO(0),
    DEBUG(1);

    final int value;

    VerbosityLevel(int value) {
        this.value = value;
    }
}
