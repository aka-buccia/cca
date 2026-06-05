package cca.ast.expression;

import cca.ast.Position;
import cca.ast.Name;
import cca.ast.visitors.VisitorInterface;

public class Variable extends Expression {

    private final Name name;

    public Variable(Name name, Position position) {
        this.name = name;
        super(position);
    }

    public Name name() {
        return this.name;
    }

    public boolean equals(Object o) {
        if (o instanceof Variable) {
            return this.name.equals(((Variable) o).name());
        } else {
            return false;
        }
    }

    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
