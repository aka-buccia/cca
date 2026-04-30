package cca;

import cca.visitors.VisitorInterface;

public class Label extends Node {

    private final String id;

    public Label(String id, Position position) {
        this.id = id;
        super(position);
    }

    public String id() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Label) {
            return this.id.equals(((Label) o).id());
        } else {
            return false;
        }
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
