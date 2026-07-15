package cca.exceptions;

import cca.ast.Position;

public class SyntaxException extends FaaSChalCoreException {

    private final Position position;

    public SyntaxException(Position position, String message) {
        super(message);
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

}
