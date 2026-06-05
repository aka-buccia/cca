package cca.ast;

import cca.ast.visitors.VisitorInterface;

public class Label extends Node {

    private final Name name;

    public Label(Name name, Position position) {
        this.name = name;
        super(position);
    }

    public Name name() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Label) {
            return this.name.equals(((Label) o).name());
        } else {
            return false;
        }
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
