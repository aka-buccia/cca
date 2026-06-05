package cca.ast;

import cca.ast.visitors.VisitorInterface;

public class Name extends Node {

    private final String id;

    public Name(String id, Position position) {
        this.id = id;
        super(position);
    }

    public String id() {
        return this.id;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof Name) {
            return this.id.equals(((Name) o).id());
        } else {
            return false;
        }
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
