package cca.exceptions;

import cca.ast.Position;

public class AstPositionedException extends FaaSChalCoreException {

    private final Position position;

    public AstPositionedException(Position position, String message) {
        super(message);
        this.position = position;
    }

    public Position getPosition() {
        return this.position;
    }
}
