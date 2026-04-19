package cca.exceptions;

import cca.Position;

public class SyntaxException extends RuntimeException {

    private final Position position;

    public SyntaxException(Position position, String message) {
        super(String.format("Syntax error at %s: %s", position, message));
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

}
