package cca.exceptions;

import cca.ast.Position;

public class IllFormedException extends FaaSChalCoreException {

    private final Position position;

    public IllFormedException(Position position, String message) {
        super(message);
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

}
