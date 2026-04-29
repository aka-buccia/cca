package cca;

import cca.visitors.VisitorInterface;

public class Media extends Node {

    private final String id;

    public Media(String id, Position position) {
        this.id = id;
        super(position);
    }

    public String id() {
        return this.id;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof Media) {
            return this.id.equals(((Media) o).id());
        } else {
            return false;
        }
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
