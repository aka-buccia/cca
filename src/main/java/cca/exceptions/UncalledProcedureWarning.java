package cca.exceptions;

import cca.ast.Position;

public class UncalledProcedureWarning extends WarningException implements AstPositioned {
    private final Position position;

    public UncalledProcedureWarning(Position position, String procedureName) {
        super("Procedure '" + procedureName + "' is defined but never called");
        this.position = position;
    }

    @Override
    public Position getPosition() {
        return this.position;
    }
}
