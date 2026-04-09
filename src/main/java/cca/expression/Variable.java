package cca;

import cca.visitors.VisitorInterface;

public class Variable extends Node {

    private final String id;

    public Variable(String id, Position position) {
        this.id = id;
        super(position);
    }

    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
