package cca.exceptions;

import cca.ast.Position;

public class AstException extends FaaSChalCoreException implements AstPositioned {

    private final Position position;

    public AstException(Position position, String message) {
        super(message);
        this.position = position;
    }

    public Position getPosition() {
        return this.position;
    }
}
