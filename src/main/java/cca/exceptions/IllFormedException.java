package cca.exceptions;

import cca.ast.Position;

public class IllFormedException extends AstException {
    public IllFormedException(Position position, String message) {
        super(position, message);
    }
}
