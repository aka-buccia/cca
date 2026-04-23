package cca.expression;

import cca.Position;
import cca.visitors.VisitorInterface;

public class Variable extends Expression {

    private final String id;

    public Variable(String id, Position position) {
        this.id = id;
        super(position);
    }

    public String id() {
        return this.id;
    }

    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
