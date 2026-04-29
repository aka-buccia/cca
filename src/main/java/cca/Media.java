package cca;

import cca.visitors.VisitorInterface;

public class Media extends Node {

    private final String id;

    public Media(String id, Position position) {
        this.id = id;
        super(position);
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
