package cca.exceptions;

import cca.ast.Position;

public class SyntaxException extends AstPositionedException {
    public SyntaxException(Position position, String message) {
        super(position, message);
    }
}
