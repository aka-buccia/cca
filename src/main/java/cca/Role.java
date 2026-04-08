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
    public String toString() {
        return id();
    }

}
