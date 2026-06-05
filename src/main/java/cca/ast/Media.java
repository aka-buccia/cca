package cca.ast;

import cca.ast.visitors.VisitorInterface;

public class Media extends Node {

    private final Name name;

    public Media(Name name, Position position) {
        this.name = name;
        super(position);
    }

    public Name name() {
        return this.name;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof Media) {
            return this.name.equals(((Media) o).name());
        } else {
            return false;
        }
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
