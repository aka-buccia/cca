package cca.ast.expression;

import cca.ast.Position;
import cca.ast.visitors.VisitorInterface;

public class Variable extends Expression {

    private final String id;

    public Variable(String id, Position position) {
        this.id = id;
        super(position);
    }

    public String id() {
        return this.id;
    }

    public boolean equals(Object o) {
        if (o instanceof Variable) {
            return this.id.equals(((Variable) o).id());
        } else {
            return false;
        }
    }

    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
