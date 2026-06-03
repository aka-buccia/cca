package cca.ast.expression;

import cca.ast.Node;
import cca.ast.Position;

public abstract class Expression extends Node {

    public Expression(Position position) {
        super(position);
    };
}
