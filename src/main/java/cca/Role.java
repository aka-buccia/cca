package cca;

import cca.visitors.VisitorInterface;

public class Role extends Node {

    private final String id;

    public Role(String id, Position position) {
        this.id = id;
        super(position);
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }

    public String id() {
        return id;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof Role) {
            return this.id.equals(((Role) o).id());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return id();
    }

}
